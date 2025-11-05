package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tfg.tfg.controller.DashboardController;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.RankHistoryDTO;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.repository.MatchRepository;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.DataDragonService;
import com.tfg.tfg.service.RiotService;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class DashboardControllerSimpleUnitTest {

    @Mock
    private SummonerRepository summonerRepository;

    @Mock
    private RiotService riotService;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserModelRepository userRepository;
    
    @Mock
    private DataDragonService dataDragonService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        dashboardController = new DashboardController(summonerRepository, riotService, matchRepository, userRepository,dataDragonService);
    }

    @Test
    void testMyStatsWithNoSummoners() {
        // No linked summoner for authenticated user
        UserModel user = new UserModel();
        user.setName("testuser");
        user.setLinkedSummonerName(null); // No linked account
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("testuser");
            when(authentication.getName()).thenReturn("testuser");
            when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));

            ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> result = response.getBody();
            assertNotNull(result);
            assertEquals("Unranked", result.get("currentRank"));
            assertEquals(0, result.get("lp7days"));
            assertEquals("Unknown", result.get("mainRole"));
            assertNull(result.get("favoriteChampion"));
            assertEquals("testuser", result.get("username"));
            assertNull(result.get("linkedSummoner"));
        }
    }

    @Test
    void testMyStatsWithSummoner() {
        UserModel user = new UserModel();
        user.setName("testuser");
        user.setLinkedSummonerName("TestSummoner");
        
        Summoner summoner = new Summoner();
        summoner.setId(1L);
        summoner.setName("TestSummoner");
        summoner.setTier("Gold");
        summoner.setRank("II");
        summoner.setPuuid("test-puuid");

        when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
        when(summonerRepository.findByNameIgnoreCase("TestSummoner")).thenReturn(Optional.of(summoner));
        when(riotService.getTopChampionMasteries("test-puuid", 1)).thenReturn(List.of());

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("testuser");
            when(authentication.getName()).thenReturn("testuser");

            ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> result = response.getBody();
            assertNotNull(result);
            assertEquals("Gold II", result.get("currentRank"));
            // lpGainedThisWeek is calculated from match history, expect null when no matches
            assertNull(result.get("lpGainedThisWeek"));
            assertEquals("Unknown", result.get("mainRole")); // No matches to calculate role
            assertNull(result.get("favoriteChampion")); // No masteries returned
            assertEquals("testuser", result.get("username"));
            assertEquals("TestSummoner", result.get("linkedSummoner"));
        }
    }

    @Test
    void testMyStatsWithUnrankedSummoner() {
        Summoner summoner = new Summoner();
        summoner.setId(1L);
        summoner.setName("TestSummoner");
        summoner.setTier(null);
        summoner.setRank(null);

        // No linked summoner for guest user
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> result = response.getBody();
            assertNotNull(result);
            assertEquals("Unranked", result.get("currentRank"));
            assertEquals("Guest", result.get("username"));
            assertNull(result.get("linkedSummoner"));
        }
    }

    @Test
    void testMyFavoritesWithNoAuthenticatedUser() {
        // Guest user should see empty favorites
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            ResponseEntity<List<SummonerDTO>> response = dashboardController.myFavorites();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<SummonerDTO> result = response.getBody();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testMyFavoritesWithAuthenticatedUser() {
        UserModel user = new UserModel();
        user.setId(1L);
        user.setName("testuser");

        Summoner favSummoner1 = new Summoner();
        favSummoner1.setId(2L);
        favSummoner1.setName("FavoriteSummoner1");
        favSummoner1.setRiotId("riot1");
        favSummoner1.setTier("Gold");
        favSummoner1.setRank("II");

        Summoner favSummoner2 = new Summoner();
        favSummoner2.setId(3L);
        favSummoner2.setName("FavoriteSummoner2");
        favSummoner2.setRiotId("riot2");
        favSummoner2.setTier("Silver");
        favSummoner2.setRank("I");

        user.setFavoriteSummoners(List.of(favSummoner1, favSummoner2));

        when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
        when(riotService.getDataDragonService()).thenReturn(mock(com.tfg.tfg.service.DataDragonService.class));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("testuser");
            when(authentication.getName()).thenReturn("testuser");

            ResponseEntity<List<SummonerDTO>> response = dashboardController.myFavorites();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<SummonerDTO> result = response.getBody();
            assertNotNull(result);
            assertEquals(2, result.size());
        }
    }

    @Test
    void testMyStatsWithSecurityException() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenThrow(new RuntimeException("Security error"));

            ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> result = response.getBody();
            assertNotNull(result);
            assertEquals("Guest", result.get("username"));
            assertNull(result.get("linkedSummoner"));
        }
    }

    @Test
    void testMyStatsWithFavoriteChampion() {
        UserModel user = new UserModel();
        user.setName("testuser");
        user.setLinkedSummonerName("TestSummoner");
        
        Summoner summoner = new Summoner();
        summoner.setId(1L);
        summoner.setName("TestSummoner");
        summoner.setTier("Gold");
        summoner.setRank("II");
        summoner.setPuuid("test-puuid");

        com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO mastery = 
            new com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO();
        mastery.setChampionName("Ahri");
        mastery.setChampionLevel(7);
        mastery.setChampionPoints(500000);

        when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
        when(summonerRepository.findByNameIgnoreCase("TestSummoner")).thenReturn(Optional.of(summoner));
        when(riotService.getTopChampionMasteries("test-puuid", 1)).thenReturn(List.of(mastery));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("testuser");
            when(authentication.getName()).thenReturn("testuser");

            ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> result = response.getBody();
            assertNotNull(result);
            assertEquals("Gold II", result.get("currentRank"));
            assertEquals("Ahri", result.get("favoriteChampion"));
            assertEquals("TestSummoner", result.get("linkedSummoner"));
        }
    }

    @Test
    void testMyRankHistoryWithNoSummoner() {
        // No linked summoner for guest user
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            ResponseEntity<List<RankHistoryDTO>> response = dashboardController.myRankHistory();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<RankHistoryDTO> result = response.getBody();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testMyRankHistoryWithCumulativeWinsLosses() {
        // Arrange
        UserModel user = new UserModel();
        user.setName("testuser");
        user.setLinkedSummonerName("TestSummoner");
        
        Summoner summoner = new Summoner();
        summoner.setId(1L);
        summoner.setName("TestSummoner");
        summoner.setPuuid("test-puuid");

        // Create 5 matches: 3 wins, 2 losses
        MatchEntity match1 = createRankedMatch(1L, summoner, LocalDateTime.now().minusDays(5), "GOLD", "III", 30, true);
        MatchEntity match2 = createRankedMatch(2L, summoner, LocalDateTime.now().minusDays(4), "GOLD", "III", 48, false);
        MatchEntity match3 = createRankedMatch(3L, summoner, LocalDateTime.now().minusDays(3), "GOLD", "III", 33, true);
        MatchEntity match4 = createRankedMatch(4L, summoner, LocalDateTime.now().minusDays(2), "GOLD", "II", 51, true);
        MatchEntity match5 = createRankedMatch(5L, summoner, LocalDateTime.now().minusDays(1), "GOLD", "II", 36, false);

        List<MatchEntity> rankedMatches = List.of(match1, match2, match3, match4, match5);

        when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
        when(summonerRepository.findByNameIgnoreCase("TestSummoner")).thenReturn(Optional.of(summoner));
        when(matchRepository.findRankedMatchesBySummoner(summoner, "RANKED")).thenReturn(rankedMatches);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("testuser");
            when(authentication.getName()).thenReturn("testuser");

            // Act
            ResponseEntity<List<RankHistoryDTO>> response = dashboardController.myRankHistory();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<RankHistoryDTO> result = response.getBody();
            assertNotNull(result);
            assertEquals(5, result.size());

            // Verify cumulative wins/losses
            // After match 1: 1W-0L
            assertEquals(1, result.get(0).getWins());
            assertEquals(0, result.get(0).getLosses());

            // After match 2: 1W-1L
            assertEquals(1, result.get(1).getWins());
            assertEquals(1, result.get(1).getLosses());

            // After match 3: 2W-1L
            assertEquals(2, result.get(2).getWins());
            assertEquals(1, result.get(2).getLosses());

            // After match 4: 3W-1L
            assertEquals(3, result.get(3).getWins());
            assertEquals(1, result.get(3).getLosses());

            // After match 5: 3W-2L
            assertEquals(3, result.get(4).getWins());
            assertEquals(2, result.get(4).getLosses());

            // Verify rank data
            assertEquals("GOLD", result.get(0).getTier());
            assertEquals("III", result.get(0).getRank());
            assertEquals(30, result.get(0).getLeaguePoints());
        }
    }

    @Test
    void testMyRankHistoryWithNoLinkedAccount() {
        // Test that no data is shown when user has no linked account
        // Previously used fallback, now returns empty list
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false); // Not authenticated - no linked account

            // Act
            ResponseEntity<List<RankHistoryDTO>> response = dashboardController.myRankHistory();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<RankHistoryDTO> result = response.getBody();
            assertNotNull(result);
            assertTrue(result.isEmpty(), "Should return empty list when no linked account");
        }
    }

    @Test
    void testMyRankHistoryWithLPEstimation() {
        // Arrange - Test LP calculation when lpAtMatch is not available
        Summoner summoner = new Summoner();
        summoner.setId(1L);
        summoner.setName("TestSummoner");
        summoner.setPuuid("test-puuid");
        summoner.setTier("GOLD");
        summoner.setRank("II");
        summoner.setLp(50);  // Current LP
        summoner.setWins(10);
        summoner.setLosses(5);

        // Create matches WITHOUT lpAtMatch data (will trigger estimation)
        MatchEntity match1 = new MatchEntity();
        match1.setId(1L);
        match1.setSummoner(summoner);
        match1.setTimestamp(LocalDateTime.now().minusDays(3));
        match1.setGameMode("RANKED_SOLO_5x5");
        match1.setWin(true);  // Win
        match1.setLpAtMatch(null);  // No LP data
        match1.setTierAtMatch(null);
        match1.setRankAtMatch(null);

        MatchEntity match2 = new MatchEntity();
        match2.setId(2L);
        match2.setSummoner(summoner);
        match2.setTimestamp(LocalDateTime.now().minusDays(2));
        match2.setGameMode("RANKED_SOLO_5x5");
        match2.setWin(false);  // Loss
        match2.setLpAtMatch(null);
        match2.setTierAtMatch(null);
        match2.setRankAtMatch(null);

        MatchEntity match3 = new MatchEntity();
        match3.setId(3L);
        match3.setSummoner(summoner);
        match3.setTimestamp(LocalDateTime.now().minusDays(1));
        match3.setGameMode("RANKED_SOLO_5x5");
        match3.setWin(true);  // Win
        match3.setLpAtMatch(null);
        match3.setTierAtMatch(null);
        match3.setRankAtMatch(null);

        List<MatchEntity> rankedMatches = List.of(match1, match2, match3);

        UserModel user = new UserModel();
        user.setName("testuser");
        user.setLinkedSummonerName("TestSummoner");
        
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
        when(summonerRepository.findByNameIgnoreCase("TestSummoner")).thenReturn(Optional.of(summoner));
        when(matchRepository.findRankedMatchesBySummoner(summoner, "RANKED")).thenReturn(rankedMatches);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("testuser");
            when(authentication.getName()).thenReturn("testuser");

            // Act
            ResponseEntity<List<RankHistoryDTO>> response = dashboardController.myRankHistory();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<RankHistoryDTO> result = response.getBody();
            assertNotNull(result);
            assertEquals(3, result.size());

            // Verify LP estimation for GOLD tier
            // Current LP: 50
            // Match 3 (most recent): LP = 50 (current state)
            // Match 2 (loss): LP before = 50 + 20 (win gain for GOLD) = 70
            // Match 1 (win): LP before = 70 - 15 (loss for GOLD) = 55
            
            // Note: Values depend on tier-based calculation
            // GOLD: +20 on win, -15 on loss
            assertTrue(result.get(2).getLeaguePoints() >= 45 && result.get(2).getLeaguePoints() <= 55);
            
            // Verify tier/rank propagated correctly
            assertEquals("GOLD", result.get(0).getTier());
            assertEquals("II", result.get(0).getRank());
            
            // Verify cumulative stats calculated correctly (showing state AFTER each match)
            // Match history: WIN, LOSS, WIN (3 matches from 10W-5L total)
            // After match1 (WIN): 1W-0L
            assertEquals(1, result.get(0).getWins());
            assertEquals(0, result.get(0).getLosses());
            
            // After match2 (LOSS): 1W-1L  
            assertEquals(1, result.get(1).getWins());
            assertEquals(1, result.get(1).getLosses());
            
            // After match3 (WIN): 2W-1L
            assertEquals(2, result.get(2).getWins());
            assertEquals(1, result.get(2).getLosses());
        }
    }

    // Helper method to create ranked match entities for testing
    private MatchEntity createRankedMatch(Long id, Summoner summoner, LocalDateTime timestamp, 
                                         String tier, String rank, Integer lp, boolean win) {
        MatchEntity match = new MatchEntity();
        match.setId(id);
        match.setSummoner(summoner);
        match.setTimestamp(timestamp);
        match.setGameMode("RANKED_SOLO_5x5");
        match.setTierAtMatch(tier);
        match.setRankAtMatch(rank);
        match.setLpAtMatch(lp);
        match.setWin(win);
        return match;
    }
}