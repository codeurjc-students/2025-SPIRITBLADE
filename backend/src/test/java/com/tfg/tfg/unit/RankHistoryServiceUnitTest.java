package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tfg.tfg.model.dto.RankHistoryDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.RankHistory;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.RankHistoryRepository;
import com.tfg.tfg.service.RankHistoryService;

@ExtendWith(MockitoExtension.class)
class RankHistoryServiceUnitTest {

    @Mock
    RankHistoryRepository repo;

    RankHistoryService service;

    @BeforeEach
    void setUp() {
        service = new RankHistoryService(repo);
    }

    @Test
    void recordRankSnapshot_returnsNullWhenNullParams() {
        assertNull(service.recordRankSnapshot(null, null));

        Summoner s = new Summoner();
        s.setName("X");
        assertNull(service.recordRankSnapshot(s, null));
        assertNull(service.recordRankSnapshot(null, new MatchEntity()));
    }

    @Test
    void recordRankSnapshot_skipsWhenNoTier() {
        Summoner s = new Summoner();
        s.setName("S");
        MatchEntity m = new MatchEntity();
        m.setMatchId("M1");
        m.setTierAtMatch(null); // no tier

        assertNull(service.recordRankSnapshot(s, m));
        verifyNoInteractions(repo);
    }

    @Test
    void recordRankSnapshot_firstEntrySavesWithoutLpChangeAndDefaultQueue() {
        Summoner s = new Summoner();
        s.setName("Player");

        MatchEntity m = new MatchEntity();
        m.setMatchId("M2");
        m.setTierAtMatch("GOLD");
        m.setRankAtMatch("II");
        m.setLpAtMatch(50);
        m.setTimestamp(LocalDateTime.of(2025, 1, 1, 12, 0));
        m.setQueueId(null); // should default to RANKED_SOLO_5x5

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), anyString()))
            .thenReturn(Optional.empty());

        ArgumentCaptor<RankHistory> captor = ArgumentCaptor.forClass(RankHistory.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory saved = service.recordRankSnapshot(s, m);

        assertNotNull(saved);
        RankHistory captured = captor.getValue();
        assertEquals("GOLD", captured.getTier());
        assertEquals("II", captured.getRank());
        assertEquals(50, captured.getLeaguePoints());
        assertNull(captured.getLpChange());
        assertEquals("RANKED_SOLO_5x5", captured.getQueueType());
    }

    @Test
    void recordRankSnapshot_withPreviousCalculatesLpChange() {
        Summoner s = new Summoner();
        s.setName("Player2");

        MatchEntity m = new MatchEntity();
        m.setMatchId("M3");
        m.setTierAtMatch("PLATINUM");
        m.setRankAtMatch("I");
        m.setLpAtMatch(120);
        m.setTimestamp(LocalDateTime.now());
        m.setQueueId(420);

        RankHistory previous = new RankHistory();
        previous.setLeaguePoints(100);
        previous.setQueueType("RANKED_SOLO_5x5");

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), anyString()))
            .thenReturn(Optional.of(previous));

        when(repo.save(any(RankHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory result = service.recordRankSnapshot(s, m);

        assertNotNull(result);
        assertEquals(20, result.getLpChange());
        assertEquals("RANKED_SOLO_5x5", result.getQueueType());
    }

    @Test
    void calculateAverageLpChange_returnsZeroOnEmptyAndCorrectAverage() {
        Summoner s = new Summoner();
        s.setName("AvgPlayer");

        when(repo.findBySummonerAndTimestampBetweenOrderByTimestampDesc(eq(s), any(), any()))
            .thenReturn(List.of());

        double empty = service.calculateAverageLpChange(s, "RANKED_SOLO_5x5",
                LocalDateTime.now().minusDays(10), LocalDateTime.now());
        assertEquals(0.0, empty);

        RankHistory r1 = new RankHistory(); r1.setLpChange(10);
        RankHistory r2 = new RankHistory(); r2.setLpChange(-5);
        RankHistory r3 = new RankHistory(); r3.setLpChange(null);

        when(repo.findBySummonerAndTimestampBetweenOrderByTimestampDesc(eq(s), any(), any()))
            .thenReturn(List.of(r1, r2, r3));

        double avg = service.calculateAverageLpChange(s, "RANKED_SOLO_5x5",
                LocalDateTime.now().minusDays(10), LocalDateTime.now());
        assertEquals((10 + -5) / 2.0, avg);
    }

    @Test
    void getEntryCount_delegatesToRepository() {
        Summoner s = new Summoner();
        when(repo.countBySummoner(eq(s))).thenReturn(7L);

        long count = service.getEntryCount(s);
        assertEquals(7L, count);
    }

    @Test
    void getRankHistory_defaultQueue() {
        Summoner s = new Summoner();
        s.setName("TestPlayer");

        RankHistory rh = new RankHistory();
        rh.setTier("DIAMOND");
        rh.setRank("IV");
        rh.setLeaguePoints(75);

        when(repo.findBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), eq("RANKED_SOLO_5x5")))
            .thenReturn(List.of(rh));

        List<RankHistoryDTO> result = service.getRankHistory(s);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DIAMOND", result.get(0).getTier());
        verify(repo).findBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), eq("RANKED_SOLO_5x5"));
    }

    @Test
    void getRankHistory_specificQueue() {
        Summoner s = new Summoner();
        s.setName("FlexPlayer");

        RankHistory rh = new RankHistory();
        rh.setTier("PLATINUM");
        rh.setRank("III");

        when(repo.findBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), eq("RANKED_FLEX_SR")))
            .thenReturn(List.of(rh));

        List<RankHistoryDTO> result = service.getRankHistory(s, "RANKED_FLEX_SR");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repo).findBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), eq("RANKED_FLEX_SR"));
    }

    @Test
    void getRankProgression() {
        RankHistory rh1 = new RankHistory();
        rh1.setTier("GOLD");
        RankHistory rh2 = new RankHistory();
        rh2.setTier("PLATINUM");

        when(repo.findRankProgressionBySummonerAndQueue(1L, "RANKED_SOLO_5x5"))
            .thenReturn(List.of(rh1, rh2));

        List<RankHistoryDTO> result = service.getRankProgression(1L, "RANKED_SOLO_5x5");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repo).findRankProgressionBySummonerAndQueue(1L, "RANKED_SOLO_5x5");
    }

    @Test
    void getCurrentRank_found() {
        Summoner s = new Summoner();
        RankHistory rh = new RankHistory();
        rh.setTier("MASTER");
        rh.setRank("I");

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), eq("RANKED_SOLO_5x5")))
            .thenReturn(Optional.of(rh));

        Optional<RankHistoryDTO> result = service.getCurrentRank(s, "RANKED_SOLO_5x5");

        assertTrue(result.isPresent());
        assertEquals("MASTER", result.get().getTier());
    }

    @Test
    void getCurrentRank_notFound() {
        Summoner s = new Summoner();

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), eq("RANKED_SOLO_5x5")))
            .thenReturn(Optional.empty());

        Optional<RankHistoryDTO> result = service.getCurrentRank(s, "RANKED_SOLO_5x5");

        assertFalse(result.isPresent());
    }

    @Test
    void getPeakRank_found() {
        Summoner s = new Summoner();
        RankHistory rh = new RankHistory();
        rh.setTier("CHALLENGER");
        rh.setLeaguePoints(1000);

        when(repo.findPeakRank(eq(s), eq("RANKED_SOLO_5x5")))
            .thenReturn(Optional.of(rh));

        Optional<RankHistoryDTO> result = service.getPeakRank(s, "RANKED_SOLO_5x5");

        assertTrue(result.isPresent());
        assertEquals("CHALLENGER", result.get().getTier());
        assertEquals(1000, result.get().getLeaguePoints());
    }

    @Test
    void getPeakRank_notFound() {
        Summoner s = new Summoner();

        when(repo.findPeakRank(eq(s), eq("RANKED_SOLO_5x5")))
            .thenReturn(Optional.empty());

        Optional<RankHistoryDTO> result = service.getPeakRank(s, "RANKED_SOLO_5x5");

        assertFalse(result.isPresent());
    }

    @Test
    void cleanupOldEntries() {
        LocalDateTime cutoffDate = LocalDateTime.of(2023, 1, 1, 0, 0);

        doNothing().when(repo).deleteByTimestampBefore(cutoffDate);

        service.cleanupOldEntries(cutoffDate);

        verify(repo).deleteByTimestampBefore(cutoffDate);
    }

    @Test
    void recordRankSnapshot_flexQueueId() {
        Summoner s = new Summoner();
        s.setName("FlexPlayer");

        MatchEntity m = new MatchEntity();
        m.setMatchId("FLEX_M");
        m.setTierAtMatch("SILVER");
        m.setLpAtMatch(20);
        m.setTimestamp(LocalDateTime.now());
        m.setQueueId(440); // Flex queue

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), eq("RANKED_FLEX_SR")))
            .thenReturn(Optional.empty());

        ArgumentCaptor<RankHistory> captor = ArgumentCaptor.forClass(RankHistory.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory result = service.recordRankSnapshot(s, m);

        assertNotNull(result);
        assertEquals("RANKED_FLEX_SR", captor.getValue().getQueueType());
    }

    @Test
    void recordRankSnapshot_unknownQueueIdDefaultsToSolo() {
        Summoner s = new Summoner();
        MatchEntity m = new MatchEntity();
        m.setTierAtMatch("BRONZE");
        m.setLpAtMatch(5);
        m.setTimestamp(LocalDateTime.now());
        m.setQueueId(999); // Unknown queue

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), eq("RANKED_SOLO_5x5")))
            .thenReturn(Optional.empty());

        ArgumentCaptor<RankHistory> captor = ArgumentCaptor.forClass(RankHistory.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory result = service.recordRankSnapshot(s, m);

        assertNotNull(result);
        assertEquals("RANKED_SOLO_5x5", captor.getValue().getQueueType());
    }

    @Test
    void recordRankSnapshot_nullTimestampUsesNow() {
        Summoner s = new Summoner();
        MatchEntity m = new MatchEntity();
        m.setTierAtMatch("IRON");
        m.setLpAtMatch(0);
        m.setTimestamp(null); // null timestamp
        m.setQueueId(420);

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), anyString()))
            .thenReturn(Optional.empty());

        ArgumentCaptor<RankHistory> captor = ArgumentCaptor.forClass(RankHistory.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory result = service.recordRankSnapshot(s, m);

        assertNotNull(result);
        assertNotNull(captor.getValue().getTimestamp());
    }

    @Test
    void recordRankSnapshot_previousWithNullLpDoesntCalculateChange() {
        Summoner s = new Summoner();
        MatchEntity m = new MatchEntity();
        m.setTierAtMatch("GOLD");
        m.setLpAtMatch(60);
        m.setTimestamp(LocalDateTime.now());
        m.setQueueId(420);

        RankHistory previous = new RankHistory();
        previous.setLeaguePoints(null); // null LP in previous

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), anyString()))
            .thenReturn(Optional.of(previous));

        ArgumentCaptor<RankHistory> captor = ArgumentCaptor.forClass(RankHistory.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory result = service.recordRankSnapshot(s, m);

        assertNotNull(result);
        assertNull(captor.getValue().getLpChange());
    }

    @Test
    void recordRankSnapshot_currentWithNullLpDoesntCalculateChange() {
        Summoner s = new Summoner();
        MatchEntity m = new MatchEntity();
        m.setTierAtMatch("GOLD");
        m.setLpAtMatch(null); // null current LP
        m.setTimestamp(LocalDateTime.now());
        m.setQueueId(420);

        RankHistory previous = new RankHistory();
        previous.setLeaguePoints(50);

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), anyString()))
            .thenReturn(Optional.of(previous));

        ArgumentCaptor<RankHistory> captor = ArgumentCaptor.forClass(RankHistory.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory result = service.recordRankSnapshot(s, m);

        assertNotNull(result);
        assertNull(captor.getValue().getLpChange());
    }
}
