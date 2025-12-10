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
}
