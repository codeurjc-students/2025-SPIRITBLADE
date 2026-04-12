package com.tfg.tfg.unit;

import com.tfg.tfg.controller.DashboardController;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.service.interfaces.IAiAnalysisService;
import com.tfg.tfg.service.interfaces.IDataDragonService;
import com.tfg.tfg.service.interfaces.IDashboardService;
import com.tfg.tfg.service.interfaces.IMatchService;
import com.tfg.tfg.service.interfaces.IRankHistoryService;
import com.tfg.tfg.service.interfaces.IRiotService;
import com.tfg.tfg.service.interfaces.ISummonerService;
import com.tfg.tfg.service.interfaces.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardController.
 * Tests basic dashboard functionality step by step.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardControllerUnitTest {

    @Mock
    private ISummonerService summonerService;

    @Mock
    private IRiotService riotService;

    @Mock
    private IMatchService matchService;

    @Mock
    private IUserService userService;

    @Mock
    private IDataDragonService dataDragonService;

    @Mock
    private IDashboardService dashboardService;

    @Mock
    private IAiAnalysisService aiAnalysisService;

    @Mock
    private IRankHistoryService rankHistoryService;

    @InjectMocks
    private DashboardController dashboardController;

    private Summoner testSummoner;
    private UserModel testUser;

    @BeforeEach
    void setUp() {

        testSummoner = new Summoner();
        testSummoner.setId(1L);
        testSummoner.setName("TestSummoner");
        testSummoner.setPuuid("test-puuid-123");
        testSummoner.setTier("GOLD");
        testSummoner.setRank("II");
        testSummoner.setLp(50);

        testUser = new UserModel();
        testUser.setId(1L);
        testUser.setName("testuser");
        testUser.setLinkedSummonerName("TestSummoner");
        testUser.setFavoriteSummoners(new ArrayList<>());
    }

    @Test
    void testMyStatsAsGuest() {

        SecurityContextHolder.clearContext();

        Map<String, Object> guestStats = new HashMap<>();
        guestStats.put("currentRank", "Unranked");
        guestStats.put("lp7days", 0);
        guestStats.put("mainRole", "Unknown");
        guestStats.put("favoriteChampion", null);
        when(dashboardService.getPersonalStats(null)).thenReturn(guestStats);

        ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Guest", body.get("username"));
        assertNull(body.get("linkedSummoner"));
        assertEquals("Unranked", body.get("currentRank"));
        assertEquals(0, body.get("lp7days"));
        assertEquals("Unknown", body.get("mainRole"));
        assertNull(body.get("favoriteChampion"));
    }

    @Test
    void testMyStatsAuthenticatedUserNoLinkedSummoner() {

        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        testUser.setLinkedSummonerName(null);

    Map<String, Object> stats = new HashMap<>();
    stats.put("currentRank", "Unranked");
    stats.put("lp7days", 0);
    stats.put("mainRole", "Unknown");
    when(dashboardService.getPersonalStats(null)).thenReturn(stats);

        ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("testuser", body.get("username"));
        assertNull(body.get("linkedSummoner"));
        assertEquals("Unranked", body.get("currentRank"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void testMyStatsAuthenticatedUserWithLinkedSummoner() {

        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(summonerService.findByName("TestSummoner")).thenReturn(Optional.of(testSummoner));

    Map<String, Object> stats = new HashMap<>();
    stats.put("currentRank", "GOLD II");
    stats.put("favoriteChampion", "Yasuo");
    stats.put("lp7days", 0);
    when(dashboardService.getPersonalStats(testSummoner)).thenReturn(stats);

    ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("testuser", body.get("username"));
        assertEquals("TestSummoner", body.get("linkedSummoner"));
        assertEquals("GOLD II", body.get("currentRank"));
        assertEquals("Yasuo", body.get("favoriteChampion"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void testMyFavoritesAsGuest() {

        SecurityContextHolder.clearContext();

        ResponseEntity<List<SummonerDTO>> response = dashboardController.myFavorites();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testMyFavoritesAuthenticatedUserWithFavorites() {

        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        Summoner favoriteSummoner = new Summoner();
        favoriteSummoner.setName("FavoriteSummoner");
        favoriteSummoner.setPuuid("favorite-puuid");
        testUser.addFavoriteSummoner(favoriteSummoner);

        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(riotService.getDataDragonService()).thenReturn(dataDragonService);

        ResponseEntity<List<SummonerDTO>> response = dashboardController.myFavorites();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());

        SecurityContextHolder.clearContext();
    }

    @Test
    void testAddFavoriteAsGuest() {

        SecurityContextHolder.clearContext();

        ResponseEntity<Map<String, Object>> response = dashboardController.addFavorite("SomeSummoner");

        assertNotNull(response);
        assertEquals(401, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertTrue(body.get("message").toString().contains("not authenticated"));
    }

    @Test
    void testAddFavoriteUserNotFound() {

        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userService.findByName("testuser")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = dashboardController.addFavorite("SomeSummoner");

        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());

        SecurityContextHolder.clearContext();
    }

    @Test
    void testAddFavoriteSuccess() {

        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        Summoner favoriteSummoner = new Summoner();
        favoriteSummoner.setName("FavoriteSummoner");
        
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(summonerService.findByName("FavoriteSummoner")).thenReturn(Optional.of(favoriteSummoner));
        when(userService.save(any(UserModel.class))).thenReturn(testUser);

        ResponseEntity<Map<String, Object>> response = dashboardController.addFavorite("FavoriteSummoner");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertEquals(true, body.get("success"));
        verify(userService).save(testUser);

        SecurityContextHolder.clearContext();
    }

    @Test
    void testCalculateLPGainedLast7DaysNoMatches() {

        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(summonerService.findByName("TestSummoner")).thenReturn(Optional.of(testSummoner));
    Map<String, Object> stats = new HashMap<>();
    stats.put("lp7days", 0);
    when(dashboardService.getPersonalStats(testSummoner)).thenReturn(stats);

        ResponseEntity<Map<String, Object>> response = dashboardController.myStats();
        
        assertNotNull(response);
        assertEquals(0, response.getBody().get("lp7days"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void testCalculateLPGainedLast7DaysWithMatches() {

        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        MatchEntity oldMatch = new MatchEntity();
        oldMatch.setId(1L);
        oldMatch.setTimestamp(sevenDaysAgo.plusHours(1));
        
        MatchEntity recentMatch = new MatchEntity();
        recentMatch.setId(2L);
        recentMatch.setTimestamp(LocalDateTime.now().minusHours(1));

        when(matchService.findRecentMatches(eq(testSummoner), any(LocalDateTime.class)))
            .thenReturn(List.of(oldMatch, recentMatch));

        when(rankHistoryService.getLpForMatch(1L)).thenReturn(java.util.Optional.of(30));

        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(summonerService.findByName("TestSummoner")).thenReturn(Optional.of(testSummoner));
        
        testSummoner.setLp(50);

    Map<String, Object> stats = new HashMap<>();
    stats.put("lp7days", 20);
    when(dashboardService.getPersonalStats(testSummoner)).thenReturn(stats);

        ResponseEntity<Map<String, Object>> response = dashboardController.myStats();
        
        assertNotNull(response);

        assertEquals(20, response.getBody().get("lp7days"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void testRemoveFavoriteAsGuest() {

        SecurityContextHolder.clearContext();

        ResponseEntity<Map<String, Object>> response = dashboardController.removeFavorite("SomeSummoner");

        assertNotNull(response);
        assertEquals(401, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertTrue(body.get("message").toString().contains("not authenticated"));
    }

    @Test
    void testRemoveFavoriteSummonerNotFound() {

        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(summonerService.findByName("NonExistent")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = dashboardController.removeFavorite("NonExistent");

        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertTrue(body.get("message").toString().contains("not found"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void testRemoveFavoriteSuccess() {

        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        Summoner favoriteToRemove = new Summoner();
        favoriteToRemove.setName("FavoriteToRemove");
        favoriteToRemove.setPuuid("favorite-puuid");

        testUser.addFavoriteSummoner(favoriteToRemove);
        
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(summonerService.findByName("FavoriteToRemove")).thenReturn(Optional.of(favoriteToRemove));
        when(userService.save(any(UserModel.class))).thenReturn(testUser);

        ResponseEntity<Map<String, Object>> response = dashboardController.removeFavorite("FavoriteToRemove");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertTrue(body.get("message").toString().contains("removed"));
        verify(userService).save(testUser);

        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetRankedMatchesAsGuest() {

        SecurityContextHolder.clearContext();

        ResponseEntity<List<MatchHistoryDTO>> response = 
            dashboardController.getRankedMatches(0, 30, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
    
    @Test
    void testGetRankedMatchesNoLinkedSummoner() {

        testUser.setLinkedSummonerName(null);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser.getName(), null, List.of())
        );
        
        when(userService.findByName(testUser.getName())).thenReturn(Optional.of(testUser));

        ResponseEntity<List<MatchHistoryDTO>> response = 
            dashboardController.getRankedMatches(0, 30, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        SecurityContextHolder.clearContext();
    }
    
    @Test
    void testGetRankedMatchesWithCachedMatches() {

        testUser.setLinkedSummonerName("TestSummoner");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser.getName(), null, List.of())
        );
        
        when(userService.findByName(testUser.getName())).thenReturn(Optional.of(testUser));
        when(summonerService.findByName("TestSummoner")).thenReturn(Optional.of(testSummoner));

        when(matchService.findRankedMatchesBySummonerOrderByTimestampDesc(testSummoner))
            .thenReturn(List.of());

        MatchHistoryDTO apiMatch = new MatchHistoryDTO();
        apiMatch.setMatchId("EUW1_123");
        apiMatch.setChampionName("Ahri");
        apiMatch.setWin(true);
        apiMatch.setKills(10);
        apiMatch.setDeaths(2);
        apiMatch.setAssists(8);
        apiMatch.setQueueId(420);
        
        when(riotService.getMatchHistory(eq(testSummoner.getPuuid()), anyInt(), anyInt()))
            .thenReturn(List.of(apiMatch));

        MatchHistoryDTO expectedDto = new MatchHistoryDTO();
        expectedDto.setMatchId("EUW1_123");
        expectedDto.setChampionName("Ahri");
        expectedDto.setWin(true);
        expectedDto.setKills(10);
        expectedDto.setDeaths(2);
        expectedDto.setAssists(8);
        expectedDto.setQueueId(420);
        when(dashboardService.getRankedMatchesWithLP(testSummoner, null, 0, 30)).thenReturn(List.of(expectedDto));

        ResponseEntity<List<MatchHistoryDTO>> response = 
            dashboardController.getRankedMatches(0, 30, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        
    MatchHistoryDTO dto = response.getBody().get(0);
        assertEquals("EUW1_123", dto.getMatchId());
        assertEquals("Ahri", dto.getChampionName());
        assertEquals(true, dto.getWin());
        assertEquals(10, dto.getKills());
        assertEquals(420, dto.getQueueId());
        
    verify(dashboardService).getRankedMatchesWithLP(testSummoner, null, 0, 30);

        SecurityContextHolder.clearContext();
    }
    
    @Test
    void testGetRankedMatchesWithQueueIdFilter() {

        testUser.setLinkedSummonerName("TestSummoner");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser.getName(), null, List.of())
        );
        
        when(userService.findByName(testUser.getName())).thenReturn(Optional.of(testUser));
        when(summonerService.findByName("TestSummoner")).thenReturn(Optional.of(testSummoner));

        when(matchService.findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(testSummoner, 440))
            .thenReturn(List.of());

        MatchHistoryDTO flexMatch = new MatchHistoryDTO();
        flexMatch.setMatchId("EUW1_456");
        flexMatch.setChampionName("Zed");
        flexMatch.setQueueId(440);
        flexMatch.setWin(false);
        
        when(riotService.getMatchHistory(eq(testSummoner.getPuuid()), anyInt(), anyInt()))
            .thenReturn(List.of(flexMatch));
        MatchHistoryDTO flexDto = new MatchHistoryDTO();
        flexDto.setMatchId("EUW1_456");
        flexDto.setChampionName("Zed");
        flexDto.setQueueId(440);
        flexDto.setWin(false);
        when(dashboardService.getRankedMatchesWithLP(testSummoner, 440, 0, 30)).thenReturn(List.of(flexDto));

        ResponseEntity<List<MatchHistoryDTO>> response = 
            dashboardController.getRankedMatches(0, 30, 440);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(440, response.getBody().get(0).getQueueId());
        
        verify(dashboardService).getRankedMatchesWithLP(testSummoner, 440, 0, 30);

        SecurityContextHolder.clearContext();
    }
}