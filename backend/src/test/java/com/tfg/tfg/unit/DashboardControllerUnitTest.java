package com.tfg.tfg.unit;

import com.tfg.tfg.controller.DashboardController;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.service.DataDragonService;
import com.tfg.tfg.service.MatchService;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.service.SummonerService;
import com.tfg.tfg.service.UserService;
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
    private SummonerService summonerService;

    @Mock
    private RiotService riotService;

    @Mock
    private MatchService matchService;

    @Mock
    private UserService userService;

    @Mock
    private DataDragonService dataDragonService;

    @Mock
    private com.tfg.tfg.service.DashboardService dashboardService;

    @Mock
    private com.tfg.tfg.service.AiAnalysisService aiAnalysisService;

    @Mock
    private com.tfg.tfg.service.RankHistoryService rankHistoryService;

    @InjectMocks
    private DashboardController dashboardController;

    private Summoner testSummoner;
    private UserModel testUser;

    @BeforeEach
    void setUp() {
        // Create test summoner
        testSummoner = new Summoner();
        testSummoner.setId(1L);
        testSummoner.setName("TestSummoner");
        testSummoner.setPuuid("test-puuid-123");
        testSummoner.setTier("GOLD");
        testSummoner.setRank("II");
        testSummoner.setLp(50);

        // Create test user
        testUser = new UserModel();
        testUser.setId(1L);
        testUser.setName("testuser");
        testUser.setLinkedSummonerName("TestSummoner");
        testUser.setFavoriteSummoners(new ArrayList<>());

        // No global stubbing here; tests will stub the dashboardService per-case to reflect service responsibilities
    }


    @Test
    void testMyStatsAsGuest() {
        // Clear security context (guest user)
        SecurityContextHolder.clearContext();

        // dashboardService should return default guest stats for null summoner
        Map<String, Object> guestStats = new HashMap<>();
        guestStats.put("currentRank", "Unranked");
        guestStats.put("lp7days", 0);
        guestStats.put("mainRole", "Unknown");
        guestStats.put("favoriteChampion", null);
        when(dashboardService.getPersonalStats(null)).thenReturn(guestStats);

        // Execute
        ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

        // Verify
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
        // Setup authentication - use real SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // User exists but has no linked summoner
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        testUser.setLinkedSummonerName(null);

    Map<String, Object> stats = new HashMap<>();
    stats.put("currentRank", "Unranked");
    stats.put("lp7days", 0);
    stats.put("mainRole", "Unknown");
    when(dashboardService.getPersonalStats(null)).thenReturn(stats);

        // Execute
        ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("testuser", body.get("username"));
        assertNull(body.get("linkedSummoner"));
        assertEquals("Unranked", body.get("currentRank"));
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testMyStatsAuthenticatedUserWithLinkedSummoner() {
        // Setup authentication - use real SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // User has linked summoner
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(summonerService.findByNameIgnoreCase("TestSummoner")).thenReturn(Optional.of(testSummoner));
        
        // dashboardService should compute and return personal stats for the summoner
    Map<String, Object> stats = new HashMap<>();
    stats.put("currentRank", "GOLD II");
    stats.put("favoriteChampion", "Yasuo");
    stats.put("lp7days", 0);
    when(dashboardService.getPersonalStats(testSummoner)).thenReturn(stats);

    // Execute
    ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("testuser", body.get("username"));
        assertEquals("TestSummoner", body.get("linkedSummoner"));
        assertEquals("GOLD II", body.get("currentRank"));
        assertEquals("Yasuo", body.get("favoriteChampion"));
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testMyFavoritesAsGuest() {
        // Clear security context
        SecurityContextHolder.clearContext();

        // Execute
        ResponseEntity<List<SummonerDTO>> response = dashboardController.myFavorites();

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testMyFavoritesAuthenticatedUserWithFavorites() {
        // Setup authentication - use real SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Add favorite to user
        Summoner favoriteSummoner = new Summoner();
        favoriteSummoner.setName("FavoriteSummoner");
        favoriteSummoner.setPuuid("favorite-puuid");
        testUser.addFavoriteSummoner(favoriteSummoner);

        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(riotService.getDataDragonService()).thenReturn(dataDragonService);

        // Execute
        ResponseEntity<List<SummonerDTO>> response = dashboardController.myFavorites();

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAddFavoriteAsGuest() {
        // Clear security context
        SecurityContextHolder.clearContext();

        // Execute
        ResponseEntity<Map<String, Object>> response = dashboardController.addFavorite("SomeSummoner");

        // Verify
        assertNotNull(response);
        assertEquals(401, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertTrue(body.get("message").toString().contains("not authenticated"));
    }

    @Test
    void testAddFavoriteUserNotFound() {
        // Setup authentication - use real SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userService.findByName("testuser")).thenReturn(Optional.empty());

        // Execute
        ResponseEntity<Map<String, Object>> response = dashboardController.addFavorite("SomeSummoner");

        // Verify
        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAddFavoriteSuccess() {
        // Setup authentication - use real SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        Summoner favoriteSummoner = new Summoner();
        favoriteSummoner.setName("FavoriteSummoner");
        
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(summonerService.findByNameIgnoreCase("FavoriteSummoner")).thenReturn(Optional.of(favoriteSummoner));
        when(userService.save(any(UserModel.class))).thenReturn(testUser);

        // Execute
        ResponseEntity<Map<String, Object>> response = dashboardController.addFavorite("FavoriteSummoner");

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertEquals(true, body.get("success"));
        verify(userService).save(testUser);
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCalculateLPGainedLast7DaysNoMatches() {
        // Setup authentication - use real SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(summonerService.findByNameIgnoreCase("TestSummoner")).thenReturn(Optional.of(testSummoner));
    Map<String, Object> stats = new HashMap<>();
    stats.put("lp7days", 0);
    when(dashboardService.getPersonalStats(testSummoner)).thenReturn(stats);

        ResponseEntity<Map<String, Object>> response = dashboardController.myStats();
        
        assertNotNull(response);
        assertEquals(0, response.getBody().get("lp7days"));
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCalculateLPGainedLast7DaysWithMatches() {
        // Setup authentication - use real SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
        
        // Create matches from 7 days ago to now
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        MatchEntity oldMatch = new MatchEntity();
        oldMatch.setId(1L);
        oldMatch.setTimestamp(sevenDaysAgo.plusHours(1));
        
        MatchEntity recentMatch = new MatchEntity();
        recentMatch.setId(2L);
        recentMatch.setTimestamp(LocalDateTime.now().minusHours(1));
        
        // findRecentMatches devuelve en orden cronológico (primero el más antiguo)
        when(matchService.findRecentMatches(eq(testSummoner), any(LocalDateTime.class)))
            .thenReturn(List.of(oldMatch, recentMatch));
        
        // Mock RankHistory for LP data
        when(rankHistoryService.getLpForMatch(1L)).thenReturn(java.util.Optional.of(30)); // Started at 30 LP

        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(summonerService.findByNameIgnoreCase("TestSummoner")).thenReturn(Optional.of(testSummoner));
        
        testSummoner.setLp(50); // Current LP is 50

    Map<String, Object> stats = new HashMap<>();
    stats.put("lp7days", 20);
    when(dashboardService.getPersonalStats(testSummoner)).thenReturn(stats);

        ResponseEntity<Map<String, Object>> response = dashboardController.myStats();
        
        assertNotNull(response);
        // LP gain = current (50) - first match (30) = 20
        assertEquals(20, response.getBody().get("lp7days"));
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    // ========== REMOVE FAVORITE TESTS ==========

    @Test
    void testRemoveFavoriteAsGuest() {
        // Clear security context for guest user
        SecurityContextHolder.clearContext();

        // Execute
        ResponseEntity<Map<String, Object>> response = dashboardController.removeFavorite("SomeSummoner");

        // Verify
        assertNotNull(response);
        assertEquals(401, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertTrue(body.get("message").toString().contains("not authenticated"));
    }

    @Test
    void testRemoveFavoriteSummonerNotFound() {
        // Setup authentication - use real SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(summonerService.findByNameIgnoreCase("NonExistent")).thenReturn(Optional.empty());

        // Execute
        ResponseEntity<Map<String, Object>> response = dashboardController.removeFavorite("NonExistent");

        // Verify
        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertTrue(body.get("message").toString().contains("not found"));
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRemoveFavoriteSuccess() {
        // Setup authentication - use real SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        Summoner favoriteToRemove = new Summoner();
        favoriteToRemove.setName("FavoriteToRemove");
        favoriteToRemove.setPuuid("favorite-puuid");
        
        // Add favorite first so we can remove it
        testUser.addFavoriteSummoner(favoriteToRemove);
        
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(summonerService.findByNameIgnoreCase("FavoriteToRemove")).thenReturn(Optional.of(favoriteToRemove));
        when(userService.save(any(UserModel.class))).thenReturn(testUser);

        // Execute
        ResponseEntity<Map<String, Object>> response = dashboardController.removeFavorite("FavoriteToRemove");

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertTrue(body.get("message").toString().contains("removed"));
        verify(userService).save(testUser);
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    // ==================== getRankedMatches Tests ====================
    
    @Test
    void testGetRankedMatchesAsGuest() {
        // Setup: No authentication
        SecurityContextHolder.clearContext();
        
        // Execute
        ResponseEntity<List<com.tfg.tfg.model.dto.MatchHistoryDTO>> response = 
            dashboardController.getRankedMatches(0, 30, null);
        
        // Verify: Returns empty list for guests
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
    
    @Test
    void testGetRankedMatchesNoLinkedSummoner() {
        // Setup: Authenticated user without linked summoner
        testUser.setLinkedSummonerName(null);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser.getName(), null, List.of())
        );
        
        when(userService.findByName(testUser.getName())).thenReturn(Optional.of(testUser));
        
        // Execute
        ResponseEntity<List<com.tfg.tfg.model.dto.MatchHistoryDTO>> response = 
            dashboardController.getRankedMatches(0, 30, null);
        
        // Verify: Returns empty list
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void testGetRankedMatchesWithCachedMatches() {
        // Setup: Authenticated user with linked summoner
        testUser.setLinkedSummonerName("TestSummoner");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser.getName(), null, List.of())
        );
        
        when(userService.findByName(testUser.getName())).thenReturn(Optional.of(testUser));
        when(summonerService.findByNameIgnoreCase("TestSummoner")).thenReturn(Optional.of(testSummoner));
        
        // Mock cached matches (vamos por el camino de API ya que el cache check es complejo)
        when(matchService.findRankedMatchesBySummonerOrderByTimestampDesc(testSummoner))
            .thenReturn(List.of());
        
        // Mock API response with ranked match
        com.tfg.tfg.model.dto.MatchHistoryDTO apiMatch = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        apiMatch.setMatchId("EUW1_123");
        apiMatch.setChampionName("Ahri");
        apiMatch.setWin(true);
        apiMatch.setKills(10);
        apiMatch.setDeaths(2);
        apiMatch.setAssists(8);
        apiMatch.setQueueId(420);  // Ranked Solo/Duo
        
        when(riotService.getMatchHistory(eq(testSummoner.getPuuid()), anyInt(), anyInt()))
            .thenReturn(List.of(apiMatch));
        
        // dashboardService should return combined/converted MatchHistoryDTO with LP
    com.tfg.tfg.model.dto.MatchHistoryDTO expectedDto = new com.tfg.tfg.model.dto.MatchHistoryDTO();
    expectedDto.setMatchId("EUW1_123");
    expectedDto.setChampionName("Ahri");
    expectedDto.setWin(true);
    expectedDto.setKills(10);
    expectedDto.setDeaths(2);
    expectedDto.setAssists(8);
    expectedDto.setQueueId(420);
    when(dashboardService.getRankedMatchesWithLP(testSummoner, null, 0, 30)).thenReturn(List.of(expectedDto));

        // Execute
        ResponseEntity<List<com.tfg.tfg.model.dto.MatchHistoryDTO>> response = 
            dashboardController.getRankedMatches(0, 30, null);
        
        // Verify: Returns match from API
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        
    com.tfg.tfg.model.dto.MatchHistoryDTO dto = response.getBody().get(0);
        assertEquals("EUW1_123", dto.getMatchId());
        assertEquals("Ahri", dto.getChampionName());
        assertEquals(true, dto.getWin());
        assertEquals(10, dto.getKills());
        assertEquals(420, dto.getQueueId());
        
    verify(dashboardService).getRankedMatchesWithLP(testSummoner, null, 0, 30);
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void testGetRankedMatchesWithQueueIdFilter() {
        // Setup: Authenticated user with queue filter
        testUser.setLinkedSummonerName("TestSummoner");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser.getName(), null, List.of())
        );
        
        when(userService.findByName(testUser.getName())).thenReturn(Optional.of(testUser));
        when(summonerService.findByNameIgnoreCase("TestSummoner")).thenReturn(Optional.of(testSummoner));
        
        // Mock empty cached matches (will fetch from API)
        when(matchService.findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(testSummoner, 440))
            .thenReturn(List.of());
        
        // Mock API response with Flex match
        com.tfg.tfg.model.dto.MatchHistoryDTO flexMatch = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        flexMatch.setMatchId("EUW1_456");
        flexMatch.setChampionName("Zed");
        flexMatch.setQueueId(440);  // Flex queue
        flexMatch.setWin(false);
        
        when(riotService.getMatchHistory(eq(testSummoner.getPuuid()), anyInt(), anyInt()))
            .thenReturn(List.of(flexMatch));
        com.tfg.tfg.model.dto.MatchHistoryDTO flexDto = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        flexDto.setMatchId("EUW1_456");
        flexDto.setChampionName("Zed");
        flexDto.setQueueId(440);
        flexDto.setWin(false);
        when(dashboardService.getRankedMatchesWithLP(testSummoner, 440, 0, 30)).thenReturn(List.of(flexDto));

        // Execute with queueId filter
        ResponseEntity<List<com.tfg.tfg.model.dto.MatchHistoryDTO>> response = 
            dashboardController.getRankedMatches(0, 30, 440);
        
        // Verify: Returns only Flex matches
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(440, response.getBody().get(0).getQueueId());
        
    verify(dashboardService).getRankedMatchesWithLP(testSummoner, 440, 0, 30);
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    // ==================== refreshMatches Tests ====================
    
    @Test
    void testRefreshMatchesAsGuest() {
        // Setup: No authentication
        SecurityContextHolder.clearContext();
        
        // Execute
        ResponseEntity<Map<String, Object>> response = dashboardController.refreshMatches();
        
        // Verify: Returns error for guests
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("No linked summoner account found", response.getBody().get("message"));
    }
    
    @Test
    void testRefreshMatchesNoLinkedSummoner() {
        // Setup: Authenticated user without linked summoner
        testUser.setLinkedSummonerName(null);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser.getName(), null, List.of())
        );
        
        when(userService.findByName(testUser.getName())).thenReturn(Optional.of(testUser));
        
        // Execute
        ResponseEntity<Map<String, Object>> response = dashboardController.refreshMatches();
        
        // Verify: Returns error
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("No linked summoner account found", response.getBody().get("message"));
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void testRefreshMatchesSuccess() {
        // Setup: Authenticated user with linked summoner
        testUser.setLinkedSummonerName("TestSummoner");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser.getName(), null, List.of())
        );
        
        when(userService.findByName(testUser.getName())).thenReturn(Optional.of(testUser));
        when(summonerService.findByNameIgnoreCase("TestSummoner")).thenReturn(Optional.of(testSummoner));
        
        // Mock API response with matches
        com.tfg.tfg.model.dto.MatchHistoryDTO match1 = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        match1.setMatchId("EUW1_111");
        match1.setQueueId(420);
        
        com.tfg.tfg.model.dto.MatchHistoryDTO match2 = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        match2.setMatchId("EUW1_222");
        match2.setQueueId(420);
        
        when(riotService.getMatchHistory(eq(testSummoner.getPuuid()), anyInt(), anyInt()))
            .thenReturn(List.of(match1, match2));
        
        // Execute
        ResponseEntity<Map<String, Object>> response = dashboardController.refreshMatches();
        
        // Verify: Returns success
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("success"));
        assertEquals("Match history refreshed successfully", response.getBody().get("message"));
        assertEquals(2, response.getBody().get("matchesProcessed"));
        
        verify(riotService).getMatchHistory(testSummoner.getPuuid(), 0, 30);
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }
}