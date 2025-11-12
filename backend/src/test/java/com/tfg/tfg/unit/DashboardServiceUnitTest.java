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
import com.tfg.tfg.service.RiotService;

@ExtendWith(MockitoExtension.class)
class DashboardServiceUnitTest {

    @Mock
    MatchService matchService;

    @Mock
    RiotService riotService;

    @Mock
    DataDragonService dataDragonService;

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
        m.setLpAtMatch(100);
        when(matchService.findRecentMatches(eq(s), any(LocalDateTime.class))).thenReturn(List.of(m));
        assertEquals(20, dashboardService.calculateLPGainedLast7Days(s));

        // Null values should return 0
        m.setLpAtMatch(null);
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
}
