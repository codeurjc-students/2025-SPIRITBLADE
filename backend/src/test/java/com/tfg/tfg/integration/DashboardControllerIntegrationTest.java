package com.tfg.tfg.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.tfg.model.dto.AiAnalysisResponseDto;
import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.RankHistory;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.MatchRepository;
import com.tfg.tfg.repository.RankHistoryRepository;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.AiAnalysisService;
import com.tfg.tfg.service.RiotService;

import org.mockito.Mockito;
import static org.mockito.Mockito.*;

/**
 * Integration Tests for DashboardController
 * 
 * Strategy: Use real database with @Transactional, mock only external API
 * (RiotService)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerIntegrationTest {

    private static final String TEST_PLAYER = "TestPlayer";
    private static final String STATS_URL = "/api/v1/dashboard/me/stats";
    private static final String RANKED_MATCHES_URL = "/api/v1/dashboard/me/ranked-matches";
    private static final String JSON_LP_7_DAYS = "$.lp7days";
    private static final String JSON_MAIN_ROLE = "$.mainRole";
    private static final String JSON_SUCCESS = "$.success";
    private static final String JSON_MESSAGE = "$.message";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserModelRepository userRepository;

    @Autowired
    private SummonerRepository summonerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private RankHistoryRepository rankHistoryRepository;

    @MockitoBean
    private RiotService riotService;

    @MockitoBean
    private AiAnalysisService aiAnalysisService;

    @MockitoBean
    private com.tfg.tfg.service.MatchService matchService;

    private UserModel testUser;
    private Summoner testSummoner;

    @BeforeEach
    void setupRealisticData() {

        userRepository.deleteAll();
        rankHistoryRepository.deleteAll();
        matchRepository.deleteAll();
        summonerRepository.deleteAll();

        Mockito.reset(matchService);
        Mockito.reset(riotService);
        Mockito.reset(aiAnalysisService);

        testSummoner = new Summoner();
        testSummoner.setName(TEST_PLAYER);
        testSummoner.setPuuid("test-puuid-12345");
        testSummoner.setTier("GOLD");
        testSummoner.setRank("III");
        testSummoner.setLp(85);
        testSummoner.setWins(120);
        testSummoner.setLosses(110);
        testSummoner.setLevel(200);
        testSummoner = summonerRepository.save(testSummoner);

        LocalDateTime now = LocalDateTime.now();

        int lpTracker = 65;
        for (int i = 14; i >= 0; i--) {
            MatchEntity match = new MatchEntity();
            match.setMatchId("EUW1_recent_" + i);
            match.setSummoner(testSummoner);
            match.setChampionName(getChampionName(i));
            match.setChampionId(getChampionId(i));
            boolean won = i % 3 != 0;
            match.setWin(won);
            match.setKills(6 + i);
            match.setDeaths(4);
            match.setAssists(8 + i);
            match.setGameDuration(1850L + i * 50L);
            match.setQueueId(420);
            match.setGameMode("RANKED");
            match.setLane(getLane(i));

            match.setTimestamp(now.minusDays(i));
            MatchEntity savedMatch = matchRepository.save(match);

            RankHistory rankHistory = new RankHistory();
            rankHistory.setSummoner(testSummoner);
            rankHistory.setTriggeringMatch(savedMatch);
            rankHistory.setTimestamp(savedMatch.getTimestamp());
            rankHistory.setTier("GOLD");
            rankHistory.setRank("III");
            rankHistory.setLeaguePoints(lpTracker);
            rankHistory.setQueueType("RANKED_SOLO_5x5");
            rankHistory.setLpChange(won ? 20 : -15);
            rankHistoryRepository.save(rankHistory);

            lpTracker += won ? 20 : -15;
            if (lpTracker > 100) {
                lpTracker = 100;
            } else if (lpTracker < 0) {
                lpTracker = 0;
            }
        }

        for (int i = 15; i < 30; i++) {
            MatchEntity match = new MatchEntity();
            match.setMatchId("EUW1_old_" + i);
            match.setSummoner(testSummoner);
            match.setChampionName("Ahri");
            match.setChampionId(103);
            match.setWin(i % 2 == 0);
            match.setKills(5 + i);
            match.setDeaths(5);
            match.setAssists(7 + i);
            match.setGameDuration(1900L);
            match.setQueueId(420);
            match.setGameMode("RANKED");
            match.setLane("MIDDLE");
            match.setTimestamp(now.minusDays(7 + (long) (i - 15)));
            matchRepository.save(match);
        }

        testUser = new UserModel("testplayer", "password123", "USER");
        testUser.setEmail("testplayer@example.com");
        testUser.setActive(true);
        testUser.setLinkedSummonerName(TEST_PLAYER);
        testUser = userRepository.save(testUser);

        RiotChampionMasteryDTO mastery = new RiotChampionMasteryDTO();
        mastery.setChampionName("Ahri");
        mastery.setChampionLevel(7);
        mastery.setChampionPoints(250000);
        when(riotService.getTopChampionMasteries(anyString(), anyInt()))
                .thenReturn(List.of(mastery));

        List<MatchEntity> allMatches = matchRepository.findBySummonerOrderByTimestampDesc(testSummoner);
        List<MatchEntity> rankedMatches = allMatches.stream()
                .filter(m -> m.getQueueId() != null && (m.getQueueId() == 420 || m.getQueueId() == 440))
                .toList();
        doReturn(rankedMatches).when(matchService).findRecentMatches(any(), any());
        doReturn(rankedMatches).when(matchService).findRecentMatchesForRoleAnalysis(any(), anyInt());
        doReturn(rankedMatches).when(matchService).findRankedMatchesBySummonerOrderByTimestampDesc(any());
        doReturn(rankedMatches).when(matchService).findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(any(),
                any());
    }

    private String getChampionName(int index) {
        String[] champions = { "Ahri", "Zed", "Lux", "Yasuo", "Jinx", "Thresh", "Lee Sin" };
        return champions[index % champions.length];
    }

    private Integer getChampionId(int index) {
        Integer[] ids = { 103, 238, 99, 157, 222, 412, 64 };
        return ids[index % ids.length];
    }

    private String getLane(int index) {

        if (index % 5 == 0 || index % 5 == 1)
            return "MIDDLE";
        if (index % 5 == 2)
            return "TOP";
        if (index % 5 == 3)
            return "JUNGLE";
        return "BOTTOM";
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGetPersonalStatsWithLinkedSummonerExecutesAllPrivateMethods() throws Exception {
        mockMvc.perform(get(STATS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRank", equalTo("GOLD III")))
                .andExpect(jsonPath(JSON_LP_7_DAYS, notNullValue()))
                .andExpect(jsonPath(JSON_MAIN_ROLE, not(equalTo("Unknown"))))
                .andExpect(jsonPath("$.favoriteChampion", equalTo("Ahri")))
                .andExpect(jsonPath("$.linkedSummoner", equalTo(TEST_PLAYER)))
                .andExpect(jsonPath("$.username", equalTo("testplayer")));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGetPersonalStatsCalculatesLp7Days() throws Exception {

        mockMvc.perform(get(STATS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_LP_7_DAYS, notNullValue()));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGetPersonalStatsCalculatesMainRole() throws Exception {

        mockMvc.perform(get(STATS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_MAIN_ROLE, anyOf(
                        equalTo("Mid Lane"),
                        equalTo("Top Lane"),
                        equalTo("Jungle"),
                        equalTo("Bot Lane"))));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGetRankedMatchesExecutesSaveMatchesToDatabase() throws Exception {
        mockMvc.perform(get(RANKED_MATCHES_URL)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].matchId", notNullValue()))
                .andExpect(jsonPath("$[0].championName", notNullValue()))
                .andExpect(jsonPath("$[0].queueId", equalTo(420)));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGetRankedMatchesWithQueueFilter() throws Exception {
        mockMvc.perform(get(RANKED_MATCHES_URL)
                .param("queueId", "420")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].queueId", equalTo(420)));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testFavoritesCompleteFlow() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/me/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        Summoner favorite = new Summoner();
        favorite.setName("ProPlayer");
        favorite.setPuuid("pro-puuid-123");
        favorite.setTier("CHALLENGER");
        favorite.setRank("I");
        favorite.setLp(1500);
        summonerRepository.save(favorite);

        mockMvc.perform(post("/api/v1/dashboard/me/favorites/ProPlayer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_SUCCESS, equalTo(true)))
                .andExpect(jsonPath(JSON_MESSAGE, containsString("added to favorites")));

        mockMvc.perform(get("/api/v1/dashboard/me/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo("ProPlayer")));

        mockMvc.perform(delete("/api/v1/dashboard/me/favorites/ProPlayer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_SUCCESS, equalTo(true)))
                .andExpect(jsonPath(JSON_MESSAGE, containsString("removed from favorites")));

        mockMvc.perform(get("/api/v1/dashboard/me/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(delete("/api/v1/dashboard/me/favorites/NotInFavorites"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/dashboard/me/favorites/" + TEST_PLAYER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(JSON_SUCCESS, equalTo(false)))
                .andExpect(jsonPath(JSON_MESSAGE, containsString("Cannot add your own linked account")));

        mockMvc.perform(post("/api/v1/dashboard/me/favorites/NonExistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(JSON_SUCCESS, equalTo(false)))
                .andExpect(jsonPath(JSON_MESSAGE, containsString("not found")));
    }

    @Test
    @WithMockUser(username = "guestuser", roles = "USER")
    void testGetPersonalStatsWithoutLinkedSummonerReturnsDefaults() throws Exception {

        UserModel guest = new UserModel("guestuser", "pass", "USER");
        guest.setEmail("guest@example.com");
        userRepository.save(guest);

        mockMvc.perform(get(STATS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRank", equalTo("Unranked")))
                .andExpect(jsonPath(JSON_LP_7_DAYS, equalTo(0)))
                .andExpect(jsonPath(JSON_MAIN_ROLE, equalTo("Unknown")))
                .andExpect(jsonPath("$.favoriteChampion", nullValue()));
    }

    @Test
    void testGetPersonalStatsUnauthorized() throws Exception {
        mockMvc.perform(get(STATS_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGetRankedMatchesPagination() throws Exception {

        mockMvc.perform(get(RANKED_MATCHES_URL)
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(5))));

        mockMvc.perform(get(RANKED_MATCHES_URL)
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGetFavoritesAfterAddingMultiple() throws Exception {

        for (int i = 0; i < 3; i++) {
            Summoner fav = new Summoner();
            fav.setName("Favorite" + i);
            fav.setPuuid("fav-" + i);
            fav.setTier("PLATINUM");
            summonerRepository.save(fav);
            testUser.addFavoriteSummoner(fav);
        }
        userRepository.save(testUser);

        mockMvc.perform(get("/api/v1/dashboard/me/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", notNullValue()));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGenerateAiAnalysisSuccess() throws Exception {

        AiAnalysisResponseDto mockResponse = new AiAnalysisResponseDto();
        mockResponse.setAnalysis("Great performance! Your win rate is excellent.");
        when(aiAnalysisService.analyzePerformance(any(), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/dashboard/me/ai-analysis")
                .param("matchCount", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis", equalTo("Great performance! Your win rate is excellent.")));

        UserModel updatedUser = userRepository.findByName("testplayer").orElseThrow();
        assertNotNull(updatedUser.getLastAiAnalysisRequest());
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGenerateAiAnalysisMatchCountTooLow() throws Exception {
        mockMvc.perform(post("/api/v1/dashboard/me/ai-analysis")
                .param("matchCount", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.message", containsString("At least 10 matches are required")));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGenerateAiAnalysisMatchCountCappedAt10() throws Exception {

        AiAnalysisResponseDto mockResponse = new AiAnalysisResponseDto();
        mockResponse.setAnalysis("Analysis with 10 matches");
        when(aiAnalysisService.analyzePerformance(any(), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/dashboard/me/ai-analysis")
                .param("matchCount", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis", equalTo("Analysis with 10 matches")));
    }

    @Test
    void testGenerateAiAnalysisUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/dashboard/me/ai-analysis"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "guestuser", roles = "USER")
    void testGenerateAiAnalysisGuestUser() throws Exception {

        UserModel guest = new UserModel("guestuser", "pass", "USER");
        userRepository.save(guest);

        mockMvc.perform(post("/api/v1/dashboard/me/ai-analysis"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.message", containsString("You must link your League of Legends account first")));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGenerateAiAnalysisUserNotFound() throws Exception {

        userRepository.delete(testUser);

        mockMvc.perform(post("/api/v1/dashboard/me/ai-analysis"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.message", equalTo("User not found")));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGenerateAiAnalysisCooldownActive() throws Exception {

        testUser.setLastAiAnalysisRequest(LocalDateTime.now().minusMinutes(3));
        userRepository.save(testUser);

        mockMvc.perform(post("/api/v1/dashboard/me/ai-analysis"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.message", containsString("You must wait")))
                .andExpect(jsonPath("$.cooldownEnds", notNullValue()))
                .andExpect(jsonPath("$.remainingSeconds", notNullValue()));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGenerateAiAnalysisNoLinkedSummoner() throws Exception {

        testUser.setLinkedSummonerName(null);
        userRepository.save(testUser);

        mockMvc.perform(post("/api/v1/dashboard/me/ai-analysis"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.message", containsString("You must link your League of Legends account first")));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGenerateAiAnalysisLinkedSummonerNotFound() throws Exception {

        testUser.setLinkedSummonerName("NonExistentSummoner");
        userRepository.save(testUser);

        mockMvc.perform(post("/api/v1/dashboard/me/ai-analysis"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.message", containsString("The linked summoner account was not found")));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGenerateAiAnalysisAiServiceNotConfigured() throws Exception {

        when(aiAnalysisService.analyzePerformance(any(), any()))
                .thenThrow(new IllegalStateException("AI service not configured"));

        mockMvc.perform(post("/api/v1/dashboard/me/ai-analysis"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.message", containsString("The AI analysis service is currently unavailable")));
    }

    @Test
    @WithMockUser(username = "testplayer", roles = "USER")
    void testGenerateAiAnalysisGenericException() throws Exception {

        when(aiAnalysisService.analyzePerformance(any(), any()))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(post("/api/v1/dashboard/me/ai-analysis"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.message",
                        containsString("Error generating AI analysis: Database connection failed")));
    }
}
