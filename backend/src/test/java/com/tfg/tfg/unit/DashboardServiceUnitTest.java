package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.service.DataDragonService;
import com.tfg.tfg.service.DashboardService;
import com.tfg.tfg.service.MatchService;
import com.tfg.tfg.service.RankHistoryService;
import com.tfg.tfg.service.RiotService;

@ExtendWith(MockitoExtension.class)
class DashboardServiceUnitTest {

    @Mock
    MatchService matchService;

    @Mock
    RiotService riotService;

    @Mock
    DataDragonService dataDragonService;
    
    @Mock
    RankHistoryService rankHistoryService;

    @InjectMocks
    DashboardService dashboardService;

    @Test
    void testFormatRankAndPersonalStatsNullSummoner() {
        Summoner nullSummoner = null;
        Map<String, Object> stats = dashboardService.getPersonalStats(nullSummoner);
        assertEquals("Unranked", stats.get("currentRank"));
        assertEquals(0, stats.get("lp7days"));
        assertEquals("Unknown", stats.get("mainRole"));
        assertNull(stats.get("favoriteChampion"));

        Summoner s = new Summoner();
        s.setTier("GOLD");
        s.setRank("II");
        String formatted = dashboardService.formatRank(s);
        assertTrue(formatted.contains("GOLD"));
    }

    @Test
    void testCalculateLPGainedLast7Days_emptyAnd_values() {
        Summoner s = new Summoner();
        s.setName("player1");
        s.setLp(120);

        when(matchService.findRecentMatches(eq(s), any(LocalDateTime.class))).thenReturn(List.of());
        assertEquals(0, dashboardService.calculateLPGainedLast7Days(s));

        MatchEntity m = new MatchEntity();
        m.setId(1L);
        when(matchService.findRecentMatches(eq(s), any(LocalDateTime.class))).thenReturn(List.of(m));
        when(rankHistoryService.getLpForMatch(1L)).thenReturn(java.util.Optional.of(100));
        assertEquals(20, dashboardService.calculateLPGainedLast7Days(s));

        // No RankHistory should return 0
        when(rankHistoryService.getLpForMatch(1L)).thenReturn(java.util.Optional.empty());
        when(matchService.findRecentMatches(eq(s), any(LocalDateTime.class))).thenReturn(List.of(m));
        assertEquals(0, dashboardService.calculateLPGainedLast7Days(s));
    }

    @Test
    void testCalculateMainRole_variations() {
        Summoner s = new Summoner();
        s.setName("roleTest");

        when(matchService.findRecentMatchesForRoleAnalysis(eq(s), eq(20))).thenReturn(List.of());
        assertEquals("Unknown", dashboardService.calculateMainRole(s));

        MatchEntity m1 = new MatchEntity(); m1.setLane("TOP");
        MatchEntity m2 = new MatchEntity(); m2.setLane("TOP");
        MatchEntity m3 = new MatchEntity(); m3.setLane("MID");
        when(matchService.findRecentMatchesForRoleAnalysis(eq(s), eq(20))).thenReturn(List.of(m1, m2, m3));
        assertEquals("Top Lane", dashboardService.calculateMainRole(s));
    }

    @Test
    void testGetFavoriteChampion_and_errorPath() {
        Summoner s = new Summoner();
        s.setName("fav");
        // no puuid -> no champion
        s.setPuuid(null);
        assertNull(dashboardService.getFavoriteChampion(s));

        s.setPuuid("puuid-1");
        RiotChampionMasteryDTO dto = new RiotChampionMasteryDTO();
        dto.setChampionName("Zed");
        when(riotService.getTopChampionMasteries(eq("puuid-1"), eq(1))).thenReturn(List.of(dto));
        assertEquals("Zed", dashboardService.getFavoriteChampion(s));

        // exception path
        when(riotService.getTopChampionMasteries(eq("puuid-1"), eq(1))).thenThrow(new RuntimeException("boom"));
        assertNull(dashboardService.getFavoriteChampion(s));
    }

    @Test
    void testCalculateBackwardsLpChange_and_demote_logic() throws Exception {
        // call private method via reflection to cover demotion logic
        Method m = DashboardService.class.getDeclaredMethod("calculateBackwardsLpChange", int.class, boolean.class, String.class, String.class);
        m.setAccessible(true);

        // Case: winning (so backwards subtracts 20), causing negative LP and demotion
        Object res = m.invoke(dashboardService, 10, true, "GOLD", "I");
        assertTrue(res instanceof Integer);
        int val = (Integer) res;
        assertTrue(val >= 0);

        // Case: cannot demote (MASTER) -> clamp
        Object res2 = m.invoke(dashboardService, 5, true, "MASTER", "I");
        assertTrue(res2 instanceof Integer);
        int val2 = (Integer) res2;
        assertEquals(0, val2);
    }

    @Test
    void testCalculateAverageVisionScore() {
        Summoner s = new Summoner();
        s.setName("visionTest");
        
        // No matches
        when(matchService.findRecentMatches(eq(s), any(LocalDateTime.class))).thenReturn(List.of());
        assertEquals(0.0, dashboardService.calculateAverageVisionScore(s));
        
        // Matches with vision score
        MatchEntity m1 = new MatchEntity(); m1.setQueueId(420); m1.setVisionScore(20);
        MatchEntity m2 = new MatchEntity(); m2.setQueueId(440); m2.setVisionScore(30);
        MatchEntity m3 = new MatchEntity(); m3.setQueueId(450); m3.setVisionScore(100); // Ignored (ARAM)
        
        when(matchService.findRecentMatches(eq(s), any(LocalDateTime.class))).thenReturn(List.of(m1, m2, m3));
        assertEquals(25.0, dashboardService.calculateAverageVisionScore(s));
    }

    @Test
    void testCalculateAverageKDA() {
        Summoner s = new Summoner();
        s.setName("kdaTest");
        
        // No matches
        when(matchService.findRecentMatches(eq(s), any(LocalDateTime.class))).thenReturn(List.of());
        assertEquals("0/0/0", dashboardService.calculateAverageKDA(s));
        
        // Matches with KDA
        MatchEntity m1 = new MatchEntity(); m1.setQueueId(420); m1.setKills(5); m1.setDeaths(2); m1.setAssists(10);
        MatchEntity m2 = new MatchEntity(); m2.setQueueId(440); m2.setKills(10); m2.setDeaths(4); m2.setAssists(5);
        
        when(matchService.findRecentMatches(eq(s), any(LocalDateTime.class))).thenReturn(List.of(m1, m2));
        // Total: 15/6/15 -> Avg: 7/3/7 (integer division)
        assertEquals("7/3/7", dashboardService.calculateAverageKDA(s));
    }

    @Test
    void testGetRankedMatchesWithLP_Cached() {
        Summoner s = new Summoner();
        s.setName("ranked");
        s.setPuuid("puuid");
        
        MatchEntity m = new MatchEntity();
        m.setMatchId("M1");
        m.setTimestamp(LocalDateTime.now());
        
        when(matchService.findRankedMatchesBySummonerOrderByTimestampDesc(s)).thenReturn(List.of(m));
        // Mock checkIfCacheNeedsUpdate logic (via riotService)
        // If I mock riotService.getMatchHistory to return empty or same match ID, it won't update
        when(riotService.getMatchHistory(eq("puuid"), eq(0), eq(1))).thenReturn(List.of());
        
        List<com.tfg.tfg.model.dto.MatchHistoryDTO> result = dashboardService.getRankedMatchesWithLP(s, null, 0, 1);
        assertEquals(1, result.size());
        assertEquals("M1", result.get(0).getMatchId());
    }

    @Test
    void testGetRankedMatchesWithLP_NeedsUpdate() {
        Summoner s = new Summoner();
        s.setName("needsUpdate");
        s.setPuuid("puuid123");
        s.setTier("GOLD");
        s.setRank("II");
        s.setLp(50);
        
        // Empty cache forces update
        when(matchService.findRankedMatchesBySummonerOrderByTimestampDesc(s)).thenReturn(List.of());
        
        // Mock API response
        com.tfg.tfg.model.dto.MatchHistoryDTO apiMatch = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        apiMatch.setMatchId("NEW_MATCH");
        apiMatch.setQueueId(420);
        apiMatch.setWin(true);
        apiMatch.setGameTimestamp(System.currentTimeMillis() / 1000);
        
        when(riotService.getMatchHistory("puuid123", 0, 10)).thenReturn(List.of(apiMatch));
        when(matchService.findExistingMatchesByMatchIds(any())).thenReturn(java.util.Map.of());
        when(matchService.saveAll(any())).thenReturn(List.of());
        
        List<com.tfg.tfg.model.dto.MatchHistoryDTO> result = dashboardService.getRankedMatchesWithLP(s, null, 0, 10);
        assertNotNull(result);
    }

    @Test
    void testGetRankedMatchesWithLP_WithQueueIdFilter() {
        Summoner s = new Summoner();
        s.setName("queueFilter");
        s.setPuuid("puuid456");
        
        MatchEntity m1 = new MatchEntity();
        m1.setMatchId("SOLO_MATCH");
        m1.setQueueId(420);
        
        when(matchService.findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(s, 420)).thenReturn(List.of(m1));
        when(riotService.getMatchHistory("puuid456", 0, 1)).thenReturn(List.of());
        
        List<com.tfg.tfg.model.dto.MatchHistoryDTO> result = dashboardService.getRankedMatchesWithLP(s, 420, 0, 10);
        assertNotNull(result);
    }

    @Test
    void testFormatLaneName() throws Exception {
        // Test private formatLaneName method via reflection
        java.lang.reflect.Method method = DashboardService.class.getDeclaredMethod("formatLaneName", String.class);
        method.setAccessible(true);
        
        assertEquals("Top Lane", method.invoke(dashboardService, "TOP"));
        assertEquals("Jungle", method.invoke(dashboardService, "JUNGLE"));
        assertEquals("Mid Lane", method.invoke(dashboardService, "MIDDLE"));
        assertEquals("Bot Lane", method.invoke(dashboardService, "BOTTOM"));
        assertEquals("Support", method.invoke(dashboardService, "SUPPORT"));
        assertEquals("Unknown", method.invoke(dashboardService, (String) null));
        assertEquals("Unknown", method.invoke(dashboardService, ""));
    }

    @Test
    void testCheckIfCacheNeedsUpdate() throws Exception {
        Summoner s = new Summoner();
        s.setPuuid("test-puuid");
        
        MatchEntity cachedMatch = new MatchEntity();
        cachedMatch.setMatchId("OLD_MATCH");
        
        // Mock API returning different match (needs update)
        com.tfg.tfg.model.dto.MatchHistoryDTO newMatch = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        newMatch.setMatchId("NEW_MATCH");
        when(riotService.getMatchHistory("test-puuid", 0, 1)).thenReturn(List.of(newMatch));
        
        java.lang.reflect.Method method = DashboardService.class.getDeclaredMethod("checkIfCacheNeedsUpdate", List.class, String.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(dashboardService, List.of(cachedMatch), "test-puuid");
        assertTrue(result);
    }

    @Test
    void testCheckIfCacheNeedsUpdate_EmptyCache() throws Exception {
        java.lang.reflect.Method method = DashboardService.class.getDeclaredMethod("checkIfCacheNeedsUpdate", List.class, String.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(dashboardService, List.of(), "test-puuid");
        assertTrue(result);
    }

    @Test
    void testCanDemote() throws Exception {
        java.lang.reflect.Method method = DashboardService.class.getDeclaredMethod("canDemote", String.class, String.class);
        method.setAccessible(true);
        
        // Cannot demote from Master+
        assertFalse((boolean) method.invoke(dashboardService, "MASTER", "I"));
        assertFalse((boolean) method.invoke(dashboardService, "GRANDMASTER", "I"));
        assertFalse((boolean) method.invoke(dashboardService, "CHALLENGER", "I"));
        
        // Cannot demote from Iron IV
        assertFalse((boolean) method.invoke(dashboardService, "IRON", "IV"));
        
        // Can demote from other divisions
        assertTrue((boolean) method.invoke(dashboardService, "GOLD", "II"));
        assertTrue((boolean) method.invoke(dashboardService, "SILVER", "I"));
    }

    @Test
    void testDemoteDivision() throws Exception {
        java.lang.reflect.Method method = DashboardService.class.getDeclaredMethod("demoteDivision", String.class);
        method.setAccessible(true);
        
        assertEquals("II", method.invoke(dashboardService, "I"));
        assertEquals("III", method.invoke(dashboardService, "II"));
        assertEquals("IV", method.invoke(dashboardService, "III"));
        assertEquals("IV", method.invoke(dashboardService, "IV"));
    }

    @Test
    void testPopulateLpFromHistory() throws Exception {
        // Test private populateLpFromHistory method via reflection
        java.lang.reflect.Method method = DashboardService.class.getDeclaredMethod(
            "populateLpFromHistory", 
            List.class, 
            Map.class
        );
        method.setAccessible(true);
        
        // Create test data
        com.tfg.tfg.model.dto.MatchHistoryDTO match1 = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        match1.setMatchId("MATCH_1");
        
        com.tfg.tfg.model.dto.MatchHistoryDTO match2 = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        match2.setMatchId("MATCH_2");
        
        MatchEntity entity1 = new MatchEntity();
        entity1.setId(1L);
        entity1.setMatchId("MATCH_1");
        
        MatchEntity entity2 = new MatchEntity();
        entity2.setId(2L);
        entity2.setMatchId("MATCH_2");
        
        Map<String, MatchEntity> existingMatches = Map.of(
            "MATCH_1", entity1,
            "MATCH_2", entity2
        );
        
        when(rankHistoryService.getLpForMatch(1L)).thenReturn(java.util.Optional.of(120));
        when(rankHistoryService.getLpForMatch(2L)).thenReturn(java.util.Optional.of(95));
        
        List<com.tfg.tfg.model.dto.MatchHistoryDTO> matches = List.of(match1, match2);
        
        // When
        method.invoke(dashboardService, matches, existingMatches);
        
        // Then
        assertEquals(120, match1.getLpAtMatch());
        assertEquals(95, match2.getLpAtMatch());
    }

    @Test
    void testPopulateLpFromHistoryNoRankHistory() throws Exception {
        java.lang.reflect.Method method = DashboardService.class.getDeclaredMethod(
            "populateLpFromHistory", 
            List.class, 
            Map.class
        );
        method.setAccessible(true);
        
        com.tfg.tfg.model.dto.MatchHistoryDTO match = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        match.setMatchId("NO_LP_MATCH");
        
        MatchEntity entity = new MatchEntity();
        entity.setId(999L);
        entity.setMatchId("NO_LP_MATCH");
        
        Map<String, MatchEntity> existingMatches = Map.of("NO_LP_MATCH", entity);
        
        when(rankHistoryService.getLpForMatch(999L)).thenReturn(java.util.Optional.empty());
        
        List<com.tfg.tfg.model.dto.MatchHistoryDTO> matches = List.of(match);
        
        // When
        method.invoke(dashboardService, matches, existingMatches);
        
        // Then - LP should not be set when not found in history
        assertNull(match.getLpAtMatch());
    }

    @Test
    void testPopulateLpFromHistoryMatchNotInMap() throws Exception {
        java.lang.reflect.Method method = DashboardService.class.getDeclaredMethod(
            "populateLpFromHistory", 
            List.class, 
            Map.class
        );
        method.setAccessible(true);
        
        com.tfg.tfg.model.dto.MatchHistoryDTO match = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        match.setMatchId("NOT_FOUND");
        
        Map<String, MatchEntity> existingMatches = Map.of();
        List<com.tfg.tfg.model.dto.MatchHistoryDTO> matches = List.of(match);
        
        // When - should not throw exception
        method.invoke(dashboardService, matches, existingMatches);
        
        // Then - LP should remain null
        assertNull(match.getLpAtMatch());
    }

    @Test
    void testSaveMatchesWithoutLp() throws Exception {
        java.lang.reflect.Method method = DashboardService.class.getDeclaredMethod(
            "saveMatchesWithoutLp",
            List.class,
            Map.class,
            Summoner.class
        );
        method.setAccessible(true);
        
        Summoner summoner = new Summoner();
        summoner.setName("TestSummoner");
        summoner.setPuuid("test-puuid");
        
        com.tfg.tfg.model.dto.MatchHistoryDTO match1 = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        match1.setMatchId("SAVE_1");
        match1.setQueueId(420);
        match1.setWin(true);
        match1.setGameTimestamp(System.currentTimeMillis() / 1000);
        
        com.tfg.tfg.model.dto.MatchHistoryDTO match2 = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        match2.setMatchId("SAVE_2");
        match2.setQueueId(420);
        match2.setWin(false);
        match2.setGameTimestamp(System.currentTimeMillis() / 1000);
        
        MatchEntity existingEntity = new MatchEntity();
        existingEntity.setId(100L);
        existingEntity.setMatchId("SAVE_1");
        
        Map<String, MatchEntity> existingMatches = Map.of("SAVE_1", existingEntity);
        List<com.tfg.tfg.model.dto.MatchHistoryDTO> matches = List.of(match1, match2);
        
        // When
        method.invoke(dashboardService, matches, existingMatches, summoner);
        
        // Then - Verify matchService.save was called for each match
        verify(matchService, times(2)).save(any(MatchEntity.class));
    }

    @Test
    void testSaveMatchesWithoutLpEmptyList() throws Exception {
        java.lang.reflect.Method method = DashboardService.class.getDeclaredMethod(
            "saveMatchesWithoutLp",
            List.class,
            Map.class,
            Summoner.class
        );
        method.setAccessible(true);
        
        Summoner summoner = new Summoner();
        summoner.setName("EmptySummoner");
        
        Map<String, MatchEntity> existingMatches = Map.of();
        List<com.tfg.tfg.model.dto.MatchHistoryDTO> matches = List.of();
        
        // When
        method.invoke(dashboardService, matches, existingMatches, summoner);
        
        // Then - save should not be called
        verify(matchService, never()).save(any(MatchEntity.class));
    }
}
