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
    void testGetRankedMatchesfromCachenoUpdate() {
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

        MatchHistoryDTO apiLatest = new MatchHistoryDTO();
        apiLatest.setMatchId("m1");
        when(riotService.getMatchHistory(s.getPuuid(), 0, 1)).thenReturn(List.of(apiLatest));

        when(dataDragonService.getChampionIconUrl(103L)).thenReturn("http://img/ahri.png");

        var res = dashboardService.getRankedMatchesWithLP(s, null, 0, 1);

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals("m1", res.get(0).getMatchId());

        assertEquals(30, res.get(0).getLpAtMatch());
    }

    @Test
    void testGetRankedMatchesfetchFromApiandcalculateLP() {
        Summoner s = new Summoner();
        s.setPuuid("puuid-api");
        s.setName("apiUser");
        s.setTier("GOLD");
        s.setRank("I");
        s.setLp(50);

        lenient().when(matchService.findRankedMatchesBySummonerOrderByTimestampDesc(eq(s))).thenReturn(List.of());

        MatchHistoryDTO newest = new MatchHistoryDTO();
        newest.setMatchId("m_new");
        newest.setChampionName("Ahri");
        newest.setWin(true);
        newest.setKills(10);
        newest.setDeaths(2);
        newest.setAssists(3);
        newest.setGameDuration(1800L);
        newest.setGameTimestamp(System.currentTimeMillis()/1000);
        newest.setQueueId(420);
        MatchHistoryDTO older = new MatchHistoryDTO();
        older.setMatchId("m_old");
        older.setChampionName("Zed");
        older.setWin(false);
        older.setKills(5);
        older.setDeaths(5);
        older.setAssists(5);
        older.setGameDuration(1600L);
        older.setGameTimestamp((System.currentTimeMillis()/1000)-3600);
        older.setQueueId(420);

        lenient().when(riotService.getMatchHistory(eq(s.getPuuid()), anyInt(), anyInt())).thenReturn(List.of(newest, older));

        lenient().when(matchService.findExistingMatchesByMatchIdsAndSummoner(anyList(), any())).thenReturn(new HashMap<>());

        lenient().when(dataDragonService.getChampionIconUrl(anyLong())).thenReturn("http://img/champ.png");

        lenient().when(matchService.saveAll(anyList())).thenAnswer(invocation -> {
        List<MatchEntity> matches = invocation.getArgument(0);
        for (int i = 0; i < matches.size(); i++) {
            matches.get(i).setId((long) (i + 1));
        }
        return matches;
    });

    lenient().when(rankHistoryService.recordRankSnapshot(any(), any(), anyString(), anyString(), anyInt())).thenReturn(null);

        var res = dashboardService.getRankedMatchesWithLP(s, null, 0, 2);

        assertNotNull(res);
        assertEquals(2, res.size());

        assertNotNull(res.get(0).getLpAtMatch());
        assertNotNull(res.get(1).getLpAtMatch());

        assertTrue(res.get(0).getLpAtMatch() >= 0);
        assertTrue(res.get(1).getLpAtMatch() >= 0);
    }
}
