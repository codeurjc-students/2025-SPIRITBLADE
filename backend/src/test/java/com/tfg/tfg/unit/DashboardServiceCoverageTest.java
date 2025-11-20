package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.service.DashboardService;
import com.tfg.tfg.service.DataDragonService;
import com.tfg.tfg.service.MatchService;
import com.tfg.tfg.service.RankHistoryService;
import com.tfg.tfg.service.RiotService;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DashboardServiceCoverageTest {

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
    void testGetRankedMatches_fromCache_noUpdate() {
        Summoner s = new Summoner();
        s.setPuuid("puuid-cache");
        s.setName("cacheUser");

        MatchEntity cached = new MatchEntity();
        cached.setId(1L);
        cached.setMatchId("m1");
        cached.setChampionName("Ahri");
        cached.setChampionId(103);
        cached.setTimestamp(LocalDateTime.now());
        cached.setQueueId(420);

        when(matchService.findRankedMatchesBySummonerOrderByTimestampDesc(s)).thenReturn(List.of(cached));
        when(rankHistoryService.getLpForMatch(1L)).thenReturn(Optional.of(30));

        // Simulate API returning the same most-recent match id so cache is considered fresh
        MatchHistoryDTO apiLatest = new MatchHistoryDTO();
        apiLatest.setMatchId("m1");
        when(riotService.getMatchHistory(s.getPuuid(), 0, 1)).thenReturn(List.of(apiLatest));

        when(dataDragonService.getChampionIconUrl(103L)).thenReturn("http://img/ahri.png");

        var res = dashboardService.getRankedMatchesWithLP(s, null, 0, 1);

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals("m1", res.get(0).getMatchId());
        // LP should be taken from cache
        assertEquals(30, res.get(0).getLpAtMatch());
    }

    @Test
    void testGetRankedMatches_fetchFromApi_and_calculateLP() {
        Summoner s = new Summoner();
        s.setPuuid("puuid-api");
        s.setName("apiUser");
        s.setTier("GOLD");
        s.setRank("I");
        s.setLp(50);

    // allow lenient stubbing here since some paths may not invoke every interaction
    lenient().when(matchService.findRankedMatchesBySummonerOrderByTimestampDesc(eq(s))).thenReturn(List.of());

        // Two matches returned from API (most recent first)
    MatchHistoryDTO newest = new MatchHistoryDTO("m_new", "Ahri", true, 10, 2, 3, 1800L, System.currentTimeMillis()/1000);
    newest.setQueueId(420);
    MatchHistoryDTO older = new MatchHistoryDTO("m_old", "Zed", false, 5, 5, 5, 1600L, (System.currentTimeMillis()/1000)-3600);
    older.setQueueId(420);

    lenient().when(riotService.getMatchHistory(eq(s.getPuuid()), anyInt(), anyInt())).thenReturn(List.of(newest, older));

        // No existing matches in DB
    lenient().when(matchService.findExistingMatchesByMatchIds(anyList())).thenReturn(new HashMap<>());

    lenient().when(dataDragonService.getChampionIconUrl(anyLong())).thenReturn("http://img/champ.png");

    // Mock saveAll to return saved entities with IDs
    lenient().when(matchService.saveAll(anyList())).thenAnswer(invocation -> {
        List<MatchEntity> matches = invocation.getArgument(0);
        for (int i = 0; i < matches.size(); i++) {
            matches.get(i).setId((long) (i + 1));
        }
        return matches;
    });
    
    // Mock rankHistoryService to simulate LP being saved (recordRankSnapshot returns RankHistory, so return null)
    lenient().when(rankHistoryService.recordRankSnapshot(any(), any(), anyString(), anyString(), anyInt())).thenReturn(null);

        var res = dashboardService.getRankedMatchesWithLP(s, null, 0, 2);

        assertNotNull(res);
        assertEquals(2, res.size());

        // After calculation, DTOs should include LP values (non-null)
        assertNotNull(res.get(0).getLpAtMatch());
        assertNotNull(res.get(1).getLpAtMatch());
        // LP values should be non-negative integers
        assertTrue(res.get(0).getLpAtMatch() >= 0);
        assertTrue(res.get(1).getLpAtMatch() >= 0);
    }
}
