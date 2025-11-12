package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.MatchRepository;
import com.tfg.tfg.service.MatchService;
import com.tfg.tfg.service.RankHistoryService;

/**
 * Unit tests for MatchService.
 * Tests all repository delegations, filtering logic, and rank snapshot integration.
 */
@ExtendWith(MockitoExtension.class)
class MatchServiceUnitTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private RankHistoryService rankHistoryService;

    @InjectMocks
    private MatchService matchService;

    private Summoner testSummoner;
    private MatchEntity matchWithRank;
    private MatchEntity matchWithoutRank;
    private MatchEntity matchWithLane;
    private MatchEntity matchWithoutLane;

    @BeforeEach
    void setup() {
        testSummoner = new Summoner();
        testSummoner.setId(1L);
        testSummoner.setName("TestPlayer");
        testSummoner.setPuuid("test-puuid");

        matchWithRank = new MatchEntity();
        matchWithRank.setMatchId("EUW1_123");
        matchWithRank.setTimestamp(LocalDateTime.now().minusDays(1));
        matchWithRank.setSummoner(testSummoner);
        matchWithRank.setTierAtMatch("GOLD");
        matchWithRank.setRankAtMatch("II");
        matchWithRank.setLpAtMatch(50);

        matchWithoutRank = new MatchEntity();
        matchWithoutRank.setMatchId("EUW1_124");
        matchWithoutRank.setTimestamp(LocalDateTime.now().minusDays(2));
        matchWithoutRank.setSummoner(testSummoner);
        matchWithoutRank.setTierAtMatch(null);
        matchWithoutRank.setLpAtMatch(null);

        matchWithLane = new MatchEntity();
        matchWithLane.setMatchId("EUW1_125");
        matchWithLane.setTimestamp(LocalDateTime.now().minusDays(3));
        matchWithLane.setSummoner(testSummoner);
        matchWithLane.setLane("TOP");
        matchWithLane.setLpAtMatch(60);

        matchWithoutLane = new MatchEntity();
        matchWithoutLane.setMatchId("EUW1_126");
        matchWithoutLane.setTimestamp(LocalDateTime.now().minusDays(4));
        matchWithoutLane.setSummoner(testSummoner);
        matchWithoutLane.setLane("");
    }

    @Test
    void testFindBySummonerOrderByTimestampDesc() {
        List<MatchEntity> matches = Arrays.asList(matchWithRank, matchWithoutRank);
        when(matchRepository.findBySummonerOrderByTimestampDesc(testSummoner)).thenReturn(matches);

        List<MatchEntity> result = matchService.findBySummonerOrderByTimestampDesc(testSummoner);

        assertEquals(2, result.size());
        verify(matchRepository).findBySummonerOrderByTimestampDesc(testSummoner);
    }

    @Test
    void testFindRankedMatchesBySummoner() {
        String queueType = "RANKED_SOLO_5x5";
        List<MatchEntity> matches = Arrays.asList(matchWithRank);
        when(matchRepository.findRankedMatchesBySummoner(testSummoner, queueType)).thenReturn(matches);

        List<MatchEntity> result = matchService.findRankedMatchesBySummoner(testSummoner, queueType);

        assertEquals(1, result.size());
        verify(matchRepository).findRankedMatchesBySummoner(testSummoner, queueType);
    }

    @Test
    void testFindRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc() {
        Integer queueId = 420;
        List<MatchEntity> matches = Arrays.asList(matchWithRank);
        when(matchRepository.findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(testSummoner, queueId))
                .thenReturn(matches);

        List<MatchEntity> result = matchService.findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(testSummoner, queueId);

        assertEquals(1, result.size());
        verify(matchRepository).findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(testSummoner, queueId);
    }

    @Test
    void testFindRankedMatchesBySummonerOrderByTimestampDesc() {
        List<MatchEntity> matches = Arrays.asList(matchWithRank);
        when(matchRepository.findRankedMatchesBySummonerOrderByTimestampDesc(testSummoner)).thenReturn(matches);

        List<MatchEntity> result = matchService.findRankedMatchesBySummonerOrderByTimestampDesc(testSummoner);

        assertEquals(1, result.size());
        verify(matchRepository).findRankedMatchesBySummonerOrderByTimestampDesc(testSummoner);
    }

    @Test
    void testFindRecentMatchesFiltersAndSorts() {
        LocalDateTime since = LocalDateTime.now().minusDays(5);
        
        MatchEntity oldMatch = new MatchEntity();
        oldMatch.setMatchId("OLD");
        oldMatch.setTimestamp(LocalDateTime.now().minusDays(10));
        oldMatch.setLpAtMatch(40);

        MatchEntity recentMatch1 = new MatchEntity();
        recentMatch1.setMatchId("RECENT1");
        recentMatch1.setTimestamp(LocalDateTime.now().minusDays(2));
        recentMatch1.setLpAtMatch(50);

        MatchEntity recentMatch2 = new MatchEntity();
        recentMatch2.setMatchId("RECENT2");
        recentMatch2.setTimestamp(LocalDateTime.now().minusDays(1));
        recentMatch2.setLpAtMatch(55);

        List<MatchEntity> allMatches = Arrays.asList(recentMatch2, recentMatch1, oldMatch);
        when(matchRepository.findBySummonerOrderByTimestampDesc(testSummoner)).thenReturn(allMatches);

        List<MatchEntity> result = matchService.findRecentMatches(testSummoner, since);

        assertEquals(2, result.size());
        assertEquals("RECENT1", result.get(0).getMatchId()); // Should be sorted oldest first
        assertEquals("RECENT2", result.get(1).getMatchId());
    }

    @Test
    void testFindRecentMatchesFiltersOutNullTimestamp() {
        LocalDateTime since = LocalDateTime.now().minusDays(5);
        
        MatchEntity matchWithNullTimestamp = new MatchEntity();
        matchWithNullTimestamp.setMatchId("NULL_TIME");
        matchWithNullTimestamp.setTimestamp(null);
        matchWithNullTimestamp.setLpAtMatch(40);

        MatchEntity validMatch = new MatchEntity();
        validMatch.setMatchId("VALID");
        validMatch.setTimestamp(LocalDateTime.now().minusDays(1));
        validMatch.setLpAtMatch(50);

        List<MatchEntity> allMatches = Arrays.asList(validMatch, matchWithNullTimestamp);
        when(matchRepository.findBySummonerOrderByTimestampDesc(testSummoner)).thenReturn(allMatches);

        List<MatchEntity> result = matchService.findRecentMatches(testSummoner, since);

        assertEquals(1, result.size());
        assertEquals("VALID", result.get(0).getMatchId());
    }

    @Test
    void testFindRecentMatchesFiltersOutNullLp() {
        LocalDateTime since = LocalDateTime.now().minusDays(5);
        
        MatchEntity matchWithNullLp = new MatchEntity();
        matchWithNullLp.setMatchId("NULL_LP");
        matchWithNullLp.setTimestamp(LocalDateTime.now().minusDays(1));
        matchWithNullLp.setLpAtMatch(null);

        MatchEntity validMatch = new MatchEntity();
        validMatch.setMatchId("VALID");
        validMatch.setTimestamp(LocalDateTime.now().minusDays(2));
        validMatch.setLpAtMatch(50);

        List<MatchEntity> allMatches = Arrays.asList(matchWithNullLp, validMatch);
        when(matchRepository.findBySummonerOrderByTimestampDesc(testSummoner)).thenReturn(allMatches);

        List<MatchEntity> result = matchService.findRecentMatches(testSummoner, since);

        assertEquals(1, result.size());
        assertEquals("VALID", result.get(0).getMatchId());
    }

    @Test
    void testFindRecentMatchesForRoleAnalysisLimitsAndFilters() {
        List<MatchEntity> allMatches = Arrays.asList(matchWithLane, matchWithoutLane, matchWithRank, matchWithoutRank);
        when(matchRepository.findBySummonerOrderByTimestampDesc(testSummoner)).thenReturn(allMatches);

        List<MatchEntity> result = matchService.findRecentMatchesForRoleAnalysis(testSummoner, 2);

        assertEquals(1, result.size()); // Only matchWithLane has non-empty lane
        assertEquals("EUW1_125", result.get(0).getMatchId());
    }

    @Test
    void testFindRecentMatchesForRoleAnalysisFiltersEmptyLane() {
        matchWithoutLane.setLane("");
        List<MatchEntity> allMatches = Arrays.asList(matchWithoutLane);
        when(matchRepository.findBySummonerOrderByTimestampDesc(testSummoner)).thenReturn(allMatches);

        List<MatchEntity> result = matchService.findRecentMatchesForRoleAnalysis(testSummoner, 10);

        assertEquals(0, result.size());
    }

    @Test
    void testFindRecentMatchesForRoleAnalysisFiltersNullLane() {
        matchWithoutLane.setLane(null);
        List<MatchEntity> allMatches = Arrays.asList(matchWithoutLane);
        when(matchRepository.findBySummonerOrderByTimestampDesc(testSummoner)).thenReturn(allMatches);

        List<MatchEntity> result = matchService.findRecentMatchesForRoleAnalysis(testSummoner, 10);

        assertEquals(0, result.size());
    }

    @Test
    void testFindExistingMatchesByMatchIds() {
        List<String> matchIds = Arrays.asList("EUW1_123", "EUW1_124", "EUW1_125");
        List<MatchEntity> matches = Arrays.asList(matchWithRank, matchWithoutRank, matchWithLane);
        when(matchRepository.findByMatchIdIn(matchIds)).thenReturn(matches);

        Map<String, MatchEntity> result = matchService.findExistingMatchesByMatchIds(matchIds);

        assertEquals(3, result.size());
        assertTrue(result.containsKey("EUW1_123"));
        assertTrue(result.containsKey("EUW1_124"));
        assertTrue(result.containsKey("EUW1_125"));
        assertEquals(matchWithRank, result.get("EUW1_123"));
    }

    @Test
    void testSaveWithRankDataRecordsSnapshot() {
        when(matchRepository.save(matchWithRank)).thenReturn(matchWithRank);

        MatchEntity result = matchService.save(matchWithRank);

        assertEquals(matchWithRank, result);
        verify(matchRepository).save(matchWithRank);
        verify(rankHistoryService).recordRankSnapshot(testSummoner, matchWithRank);
    }

    @Test
    void testSaveWithoutRankDataDoesNotRecordSnapshot() {
        when(matchRepository.save(matchWithoutRank)).thenReturn(matchWithoutRank);

        MatchEntity result = matchService.save(matchWithoutRank);

        assertEquals(matchWithoutRank, result);
        verify(matchRepository).save(matchWithoutRank);
        verify(rankHistoryService, never()).recordRankSnapshot(any(), any());
    }

    @Test
    void testSaveWithoutSummonerDoesNotRecordSnapshot() {
        matchWithRank.setSummoner(null);
        when(matchRepository.save(matchWithRank)).thenReturn(matchWithRank);

        MatchEntity result = matchService.save(matchWithRank);

        assertEquals(matchWithRank, result);
        verify(matchRepository).save(matchWithRank);
        verify(rankHistoryService, never()).recordRankSnapshot(any(), any());
    }

    @Test
    void testSaveAllWithRankDataRecordsSnapshots() {
        List<MatchEntity> matches = Arrays.asList(matchWithRank, matchWithLane);
        when(matchRepository.saveAll(matches)).thenReturn(matches);

        List<MatchEntity> result = matchService.saveAll(matches);

        assertEquals(2, result.size());
        verify(matchRepository).saveAll(matches);
        verify(rankHistoryService).recordRankSnapshot(testSummoner, matchWithRank);
        verify(rankHistoryService, never()).recordRankSnapshot(testSummoner, matchWithLane); // matchWithLane has no tier
    }

    @Test
    void testSaveAllWithMixedDataRecordsOnlyValidSnapshots() {
        matchWithLane.setTierAtMatch("PLATINUM");
        List<MatchEntity> matches = Arrays.asList(matchWithRank, matchWithoutRank, matchWithLane);
        when(matchRepository.saveAll(matches)).thenReturn(matches);

        List<MatchEntity> result = matchService.saveAll(matches);

        assertEquals(3, result.size());
        verify(matchRepository).saveAll(matches);
        verify(rankHistoryService).recordRankSnapshot(testSummoner, matchWithRank);
        verify(rankHistoryService).recordRankSnapshot(testSummoner, matchWithLane);
        verify(rankHistoryService, never()).recordRankSnapshot(testSummoner, matchWithoutRank);
    }

    @Test
    void testSaveAllWithNoRankDataDoesNotRecordSnapshots() {
        List<MatchEntity> matches = Arrays.asList(matchWithoutRank);
        when(matchRepository.saveAll(matches)).thenReturn(matches);

        List<MatchEntity> result = matchService.saveAll(matches);

        assertEquals(1, result.size());
        verify(matchRepository).saveAll(matches);
        verify(rankHistoryService, never()).recordRankSnapshot(any(), any());
    }

    @Test
    void testSaveAllWithEmptyList() {
        List<MatchEntity> matches = Arrays.asList();
        when(matchRepository.saveAll(matches)).thenReturn(matches);

        List<MatchEntity> result = matchService.saveAll(matches);

        assertEquals(0, result.size());
        verify(matchRepository).saveAll(matches);
        verify(rankHistoryService, never()).recordRankSnapshot(any(), any());
    }
}
