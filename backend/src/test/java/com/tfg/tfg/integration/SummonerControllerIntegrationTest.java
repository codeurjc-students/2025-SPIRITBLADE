package com.tfg.tfg.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.tfg.tfg.model.dto.MatchDetailDTO;
import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.MatchRepository;
import com.tfg.tfg.repository.RankHistoryRepository;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.service.DataDragonService;

/**
 * Strategic Integration Tests for SummonerController
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SummonerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SummonerRepository summonerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private RankHistoryRepository rankHistoryRepository;

    @Autowired
    private UserModelRepository userModelRepository;

    @MockitoBean
    private RiotService riotService;

    @MockitoBean
    private DataDragonService dataDragonService;

    @BeforeEach
    void setup() {
        // Clean up in correct order due to foreign key constraints
        // user_favorite_summoners (many-to-many) -> rank_history -> matches -> summoners -> users
        
        // Clear favorite summoners relationships
        userModelRepository.findAll().forEach(user -> {
            user.getFavoriteSummoners().clear();
            userModelRepository.save(user);
        });
        
        rankHistoryRepository.deleteAll();
        matchRepository.deleteAll();
        summonerRepository.deleteAll();
        
        // Mock DataDragonService
        when(riotService.getDataDragonService()).thenReturn(dataDragonService);
    }

    @Test
    @WithMockUser
    void testGetRecentSearchesEmptyDatabase() throws Exception {
        mockMvc.perform(get("/api/v1/summoners/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser
    void testGetRecentSearchesWithSummoners() throws Exception {
        // Create 12 summoners (should return only 10 most recent)
        for (int i = 0; i < 12; i++) {
            Summoner s = new Summoner();
            s.setName("Recent" + i);
            s.setPuuid("puuid-" + i);
            s.setTier("DIAMOND");
            s.setLastSearchedAt(LocalDateTime.now().minusHours(i));
            summonerRepository.save(s);
        }

        mockMvc.perform(get("/api/v1/summoners/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].name", equalTo("Recent0"))) // Most recent
                .andExpect(jsonPath("$[9].name", equalTo("Recent9")));
    }

    @Test
    @WithMockUser
    void testGetRecentSearchesFiltersNullLastSearchedAt() throws Exception {
        // Create summoner WITH lastSearchedAt
        Summoner withDate = new Summoner();
        withDate.setName("WithDate");
        withDate.setPuuid("puuid-1");
        withDate.setTier("GOLD");
        withDate.setLastSearchedAt(LocalDateTime.now());
        summonerRepository.save(withDate);

        // Create summoner WITHOUT lastSearchedAt (should be filtered)
        Summoner withoutDate = new Summoner();
        withoutDate.setName("WithoutDate");
        withoutDate.setPuuid("puuid-2");
        withoutDate.setTier("GOLD");
        withoutDate.setLastSearchedAt(null);
        summonerRepository.save(withoutDate);

        mockMvc.perform(get("/api/v1/summoners/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo("WithDate")));
    }

    @Test
    void testGetSummonerByNameSuccess() throws Exception {
        SummonerDTO mockDTO = new SummonerDTO();
        mockDTO.setName("ProPlayer");
        mockDTO.setTier("CHALLENGER");
        mockDTO.setRank("I");
        mockDTO.setLp(1500);

        when(riotService.getSummonerByName("ProPlayer")).thenReturn(mockDTO);

        mockMvc.perform(get("/api/v1/summoners/name/ProPlayer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("ProPlayer")))
                .andExpect(jsonPath("$.tier", equalTo("CHALLENGER")))
                .andExpect(jsonPath("$.lp", equalTo(1500)));

        verify(riotService).getSummonerByName("ProPlayer");
    }

    @Test
    void testGetSummonerByNameNotFound() throws Exception {
    when(riotService.getSummonerByName("NonExistent")).thenThrow(new com.tfg.tfg.exception.SummonerNotFoundException("Summoner not found"));

    mockMvc.perform(get("/api/v1/summoners/name/NonExistent"))
        .andExpect(status().isNotFound());

    verify(riotService).getSummonerByName("NonExistent");
    }

    @Test
    void testGetTopChampionsSuccess() throws Exception {
        SummonerDTO mockSummoner = new SummonerDTO();
        mockSummoner.setName("TestPlayer");
        mockSummoner.setPuuid("test-puuid-123");

        RiotChampionMasteryDTO mastery1 = new RiotChampionMasteryDTO();
        mastery1.setChampionName("Ahri");
        mastery1.setChampionLevel(7);
        mastery1.setChampionPoints(250000);

        RiotChampionMasteryDTO mastery2 = new RiotChampionMasteryDTO();
        mastery2.setChampionName("Zed");
        mastery2.setChampionLevel(6);
        mastery2.setChampionPoints(150000);

        when(riotService.getSummonerByName("TestPlayer")).thenReturn(mockSummoner);
        when(riotService.getTopChampionMasteries("test-puuid-123", 3))
                .thenReturn(List.of(mastery1, mastery2));

        mockMvc.perform(get("/api/v1/summoners/name/TestPlayer/masteries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].championName", equalTo("Ahri")))
                .andExpect(jsonPath("$[0].championLevel", equalTo(7)))
                .andExpect(jsonPath("$[1].championName", equalTo("Zed")));

        verify(riotService).getSummonerByName("TestPlayer");
        verify(riotService).getTopChampionMasteries("test-puuid-123", 3);
    }

    @Test
    void testGetTopChampionsSummonerNotFound() throws Exception {
    when(riotService.getSummonerByName("NonExistent")).thenThrow(new com.tfg.tfg.exception.SummonerNotFoundException("Summoner not found"));

    mockMvc.perform(get("/api/v1/summoners/name/NonExistent/masteries"))
        .andExpect(status().isNotFound());

    verify(riotService).getSummonerByName("NonExistent");
    verify(riotService, never()).getTopChampionMasteries(anyString(), anyInt());
    }

    @Test
    void testGetTopChampionsSummonerWithoutPuuid() throws Exception {
        SummonerDTO mockSummoner = new SummonerDTO();
        mockSummoner.setName("NoPuuid");
        mockSummoner.setPuuid(null);

        when(riotService.getSummonerByName("NoPuuid")).thenReturn(mockSummoner);

    // If PUUID is null the service should not proceed; mock the subsequent call to throw so controller returns 404
    when(riotService.getTopChampionMasteries(isNull(), anyInt()))
        .thenThrow(new com.tfg.tfg.exception.SummonerNotFoundException("Summoner not found"));

    mockMvc.perform(get("/api/v1/summoners/name/NoPuuid/masteries"))
        .andExpect(status().isNotFound());

    verify(riotService).getSummonerByName("NoPuuid");
    verify(riotService).getTopChampionMasteries(isNull(), anyInt());
    }

    @Test
    void testGetRecentMatchesSuccess() throws Exception {
        SummonerDTO mockSummoner = new SummonerDTO();
        mockSummoner.setName("TestPlayer");
        mockSummoner.setPuuid("test-puuid-123");

        MatchHistoryDTO match1 = new MatchHistoryDTO();
        match1.setMatchId("EUW1_123");
        match1.setChampionName("Ahri");

        MatchHistoryDTO match2 = new MatchHistoryDTO();
        match2.setMatchId("EUW1_124");
        match2.setChampionName("Zed");

        when(riotService.getSummonerByName("TestPlayer")).thenReturn(mockSummoner);
        when(riotService.getMatchHistory("test-puuid-123", 0, 5))
                .thenReturn(List.of(match1, match2));

        mockMvc.perform(get("/api/v1/summoners/name/TestPlayer/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].matchId", equalTo("EUW1_123")))
                .andExpect(jsonPath("$[1].matchId", equalTo("EUW1_124")));

        verify(riotService).getMatchHistory("test-puuid-123", 0, 5);
    }

    @Test
    void testGetRecentMatchesWithPagination() throws Exception {
        SummonerDTO mockSummoner = new SummonerDTO();
        mockSummoner.setName("TestPlayer");
        mockSummoner.setPuuid("test-puuid-123");

        when(riotService.getSummonerByName("TestPlayer")).thenReturn(mockSummoner);
        when(riotService.getMatchHistory("test-puuid-123", 10, 5))
                .thenReturn(List.of());

        // Page 2 with size 5 means start = 2 * 5 = 10
        mockMvc.perform(get("/api/v1/summoners/name/TestPlayer/matches")
                .param("page", "2")
                .param("size", "5"))
                .andExpect(status().isOk());

        verify(riotService).getMatchHistory("test-puuid-123", 10, 5);
    }

    @Test
    void testGetRecentMatchesSummonerNotFound() throws Exception {
    when(riotService.getSummonerByName("NonExistent")).thenThrow(new com.tfg.tfg.exception.SummonerNotFoundException("Summoner not found"));

    mockMvc.perform(get("/api/v1/summoners/name/NonExistent/matches"))
        .andExpect(status().isNotFound());

    verify(riotService, never()).getMatchHistory(anyString(), anyInt(), anyInt());
    }

    @Test
    void testGetRecentMatchesSummonerWithoutPuuid() throws Exception {
        SummonerDTO mockSummoner = new SummonerDTO();
        mockSummoner.setName("NoPuuid");
        mockSummoner.setPuuid(null);

        when(riotService.getSummonerByName("NoPuuid")).thenReturn(mockSummoner);

    // If PUUID is null the service should not proceed; mock the subsequent call to throw so controller returns 404
    when(riotService.getMatchHistory(isNull(), anyInt(), anyInt()))
        .thenThrow(new com.tfg.tfg.exception.SummonerNotFoundException("Summoner not found"));

    mockMvc.perform(get("/api/v1/summoners/name/NoPuuid/matches"))
        .andExpect(status().isNotFound());

    verify(riotService).getSummonerByName("NoPuuid");
    verify(riotService).getMatchHistory(isNull(), anyInt(), anyInt());
    }

    @Test
    @WithMockUser
    void testGetMatchDetailsSuccess() throws Exception {
        MatchDetailDTO mockDetail = new MatchDetailDTO();
        mockDetail.setMatchId("EUW1_123456");
        mockDetail.setGameDuration(1850L);

        when(riotService.getMatchDetails("EUW1_123456")).thenReturn(mockDetail);

        mockMvc.perform(get("/api/v1/summoners/matches/EUW1_123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId", equalTo("EUW1_123456")))
                .andExpect(jsonPath("$.gameDuration", equalTo(1850)));

        verify(riotService).getMatchDetails("EUW1_123456");
    }

    @Test
    @WithMockUser
    void testGetMatchDetailsNotFound() throws Exception {
        when(riotService.getMatchDetails("EUW1_INVALID")).thenReturn(null);

        mockMvc.perform(get("/api/v1/summoners/matches/EUW1_INVALID"))
                .andExpect(status().isNotFound());

        verify(riotService).getMatchDetails("EUW1_INVALID");
    }

    @Test
    @WithMockUser
    void testGetByIdSuccess() throws Exception {
        Summoner summoner = new Summoner();
        summoner.setName("FoundById");
        summoner.setPuuid("puuid-by-id");
        summoner.setTier("PLATINUM");
        summoner.setRank("II");
        summoner.setLp(75);
        Summoner saved = summonerRepository.save(summoner);

        mockMvc.perform(get("/api/v1/summoners/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("FoundById")))
                .andExpect(jsonPath("$.tier", equalTo("PLATINUM")));
    }

    @Test
    @WithMockUser
    void testGetByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/summoners/99999"))
                .andExpect(status().isNotFound());
    }
}
