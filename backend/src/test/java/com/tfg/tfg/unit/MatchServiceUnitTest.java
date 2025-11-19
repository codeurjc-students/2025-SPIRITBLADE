package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
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

        matchWithoutRank = new MatchEntity();
        matchWithoutRank.setMatchId("EUW1_124");
        matchWithoutRank.setTimestamp(LocalDateTime.now().minusDays(2));
        matchWithoutRank.setSummoner(testSummoner);

        matchWithLane = new MatchEntity();
        matchWithLane.setMatchId("EUW1_125");
        matchWithLane.setTimestamp(LocalDateTime.now().minusDays(3));
        matchWithLane.setSummoner(testSummoner);
        matchWithLane.setLane("TOP");

        matchWithoutLane = new MatchEntity();
        matchWithoutLane.setMatchId("EUW1_126");
        matchWithoutLane.setTimestamp(LocalDateTime.now().minusDays(4));
        matchWithoutLane.setSummoner(testSummoner);
        matchWithoutLane.setLane("");
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
        oldMatch.setId(1L);
        oldMatch.setMatchId("OLD");
        oldMatch.setTimestamp(LocalDateTime.now().minusDays(10));

        MatchEntity recentMatch1 = new MatchEntity();
        recentMatch1.setId(2L);
        recentMatch1.setMatchId("RECENT1");
        recentMatch1.setTimestamp(LocalDateTime.now().minusDays(2));

        MatchEntity recentMatch2 = new MatchEntity();
        recentMatch2.setId(3L);
        recentMatch2.setMatchId("RECENT2");
        recentMatch2.setTimestamp(LocalDateTime.now().minusDays(1));

        List<MatchEntity> allMatches = Arrays.asList(recentMatch2, recentMatch1, oldMatch);
        when(matchRepository.findBySummonerOrderByTimestampDesc(testSummoner)).thenReturn(allMatches);
        
        // Mock RankHistory existence
        when(rankHistoryService.getLpForMatch(2L)).thenReturn(java.util.Optional.of(50));
        when(rankHistoryService.getLpForMatch(3L)).thenReturn(java.util.Optional.of(55));

        List<MatchEntity> result = matchService.findRecentMatches(testSummoner, since);

        assertEquals(2, result.size());
        assertEquals("RECENT1", result.get(0).getMatchId()); // Should be sorted oldest first
        assertEquals("RECENT2", result.get(1).getMatchId());
    }

    @Test
    void testFindRecentMatchesFiltersOutNullTimestamp() {
        LocalDateTime since = LocalDateTime.now().minusDays(5);
        
        MatchEntity matchWithNullTimestamp = new MatchEntity();
        matchWithNullTimestamp.setId(10L);
        matchWithNullTimestamp.setMatchId("NULL_TIME");
        matchWithNullTimestamp.setTimestamp(null);

        MatchEntity validMatch = new MatchEntity();
        validMatch.setId(11L);
        validMatch.setMatchId("VALID");
        validMatch.setTimestamp(LocalDateTime.now().minusDays(1));

        List<MatchEntity> allMatches = Arrays.asList(validMatch, matchWithNullTimestamp);
        when(matchRepository.findBySummonerOrderByTimestampDesc(testSummoner)).thenReturn(allMatches);
        when(rankHistoryService.getLpForMatch(11L)).thenReturn(java.util.Optional.of(50));

        List<MatchEntity> result = matchService.findRecentMatches(testSummoner, since);

        assertEquals(1, result.size());
        assertEquals("VALID", result.get(0).getMatchId());
    }

    @Test
    void testFindRecentMatchesFiltersOutMatchesWithoutRankHistory() {
        LocalDateTime since = LocalDateTime.now().minusDays(5);
        
        MatchEntity matchWithoutRankHistory = new MatchEntity();
        matchWithoutRankHistory.setId(20L);
        matchWithoutRankHistory.setMatchId("NO_RANK_HISTORY");
        matchWithoutRankHistory.setTimestamp(LocalDateTime.now().minusDays(1));

        MatchEntity validMatch = new MatchEntity();
        validMatch.setId(21L);
        validMatch.setMatchId("VALID");
        validMatch.setTimestamp(LocalDateTime.now().minusDays(2));

        List<MatchEntity> allMatches = Arrays.asList(matchWithoutRankHistory, validMatch);
        when(matchRepository.findBySummonerOrderByTimestampDesc(testSummoner)).thenReturn(allMatches);
        when(rankHistoryService.getLpForMatch(20L)).thenReturn(java.util.Optional.empty());
        when(rankHistoryService.getLpForMatch(21L)).thenReturn(java.util.Optional.of(50));

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
    void testSaveCallsRepository() {
        when(matchRepository.save(matchWithRank)).thenReturn(matchWithRank);

        MatchEntity result = matchService.save(matchWithRank);

        assertEquals(matchWithRank, result);
        verify(matchRepository).save(matchWithRank);
    }

    @Test
    void testSaveAllCallsRepository() {
        List<MatchEntity> matches = Arrays.asList(matchWithRank, matchWithLane);
        when(matchRepository.saveAll(matches)).thenReturn(matches);

        List<MatchEntity> result = matchService.saveAll(matches);

        assertEquals(2, result.size());
        verify(matchRepository).saveAll(matches);
    }

    @Test
    void testSaveAllWithEmptyList() {
        List<MatchEntity> matches = Arrays.asList();
        when(matchRepository.saveAll(matches)).thenReturn(matches);

        List<MatchEntity> result = matchService.saveAll(matches);

        assertEquals(0, result.size());
        verify(matchRepository).saveAll(matches);
    }
}
