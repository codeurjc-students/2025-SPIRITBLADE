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
    void recordRankSnapshotreturnsNullWhenNullParams() {
        assertNull(service.recordRankSnapshot(null, null, null, null, null));

        Summoner s = new Summoner();
        s.setName("X");
        assertNull(service.recordRankSnapshot(s, null, "GOLD", "II", 50));
        assertNull(service.recordRankSnapshot(null, new MatchEntity(), "GOLD", "II", 50));
    }

    @Test
    void recordRankSnapshotskipsWhenNoTier() {
        Summoner s = new Summoner();
        s.setName("S");
        MatchEntity m = new MatchEntity();
        m.setMatchId("M1");

        assertNull(service.recordRankSnapshot(s, m, null, null, null));
        verifyNoInteractions(repo);
    }

    @Test
    void recordRankSnapshotfirstEntrySavesWithoutLpChangeAndDefaultQueue() {
        Summoner s = new Summoner();
        s.setName("Player");

        MatchEntity m = new MatchEntity();
        m.setMatchId("M2");
        m.setTimestamp(LocalDateTime.of(2025, 1, 1, 12, 0));
        m.setQueueId(null); // should default to RANKED_SOLO_5x5

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), anyString()))
            .thenReturn(Optional.empty());

        ArgumentCaptor<RankHistory> captor = ArgumentCaptor.forClass(RankHistory.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory saved = service.recordRankSnapshot(s, m, "GOLD", "II", 50);

        assertNotNull(saved);
        RankHistory captured = captor.getValue();
        assertEquals("GOLD", captured.getTier());
        assertEquals("II", captured.getRank());
        assertEquals(50, captured.getLeaguePoints());
        assertNull(captured.getLpChange());
        assertEquals("RANKED_SOLO_5x5", captured.getQueueType());
    }

    @Test
    void recordRankSnapshotwithPreviousCalculatesLpChange() {
        Summoner s = new Summoner();
        s.setName("Player2");

        MatchEntity m = new MatchEntity();
        m.setMatchId("M3");
        m.setTimestamp(LocalDateTime.now());
        m.setQueueId(420);

        RankHistory previous = new RankHistory();
        previous.setLeaguePoints(100);
        previous.setQueueType("RANKED_SOLO_5x5");

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), anyString()))
            .thenReturn(Optional.of(previous));

        when(repo.save(any(RankHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory result = service.recordRankSnapshot(s, m, "PLATINUM", "I", 120);

        assertNotNull(result);
        assertEquals(20, result.getLpChange());
        assertEquals("RANKED_SOLO_5x5", result.getQueueType());
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
    void getPeakRankfound() {
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
    void recordRankSnapshotflexQueueId() {
        Summoner s = new Summoner();
        s.setName("FlexPlayer");

        MatchEntity m = new MatchEntity();
        m.setMatchId("FLEX_M");
        m.setTimestamp(LocalDateTime.now());
        m.setQueueId(440); // Flex queue

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), eq("RANKED_FLEX_SR")))
            .thenReturn(Optional.empty());

        ArgumentCaptor<RankHistory> captor = ArgumentCaptor.forClass(RankHistory.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory result = service.recordRankSnapshot(s, m, "SILVER", "III", 20);

        assertNotNull(result);
        assertEquals("RANKED_FLEX_SR", captor.getValue().getQueueType());
    }

    @Test
    void recordRankSnapshotunknownQueueIdDefaultsToSolo() {
        Summoner s = new Summoner();
        MatchEntity m = new MatchEntity();
        m.setTimestamp(LocalDateTime.now());
        m.setQueueId(999); // Unknown queue

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), eq("RANKED_SOLO_5x5")))
            .thenReturn(Optional.empty());

        ArgumentCaptor<RankHistory> captor = ArgumentCaptor.forClass(RankHistory.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory result = service.recordRankSnapshot(s, m, "BRONZE", "IV", 5);

        assertNotNull(result);
        assertEquals("RANKED_SOLO_5x5", captor.getValue().getQueueType());
    }

    @Test
    void recordRankSnapshotnullTimestampUsesNow() {
        Summoner s = new Summoner();
        MatchEntity m = new MatchEntity();
        m.setTimestamp(null); // null timestamp
        m.setQueueId(420);

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), anyString()))
            .thenReturn(Optional.empty());

        ArgumentCaptor<RankHistory> captor = ArgumentCaptor.forClass(RankHistory.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory result = service.recordRankSnapshot(s, m, "IRON", "IV", 0);

        assertNotNull(result);
        assertNotNull(captor.getValue().getTimestamp());
    }

    @Test
    void recordRankSnapshotpreviousWithNullLpDoesntCalculateChange() {
        Summoner s = new Summoner();
        MatchEntity m = new MatchEntity();
        m.setTimestamp(LocalDateTime.now());
        m.setQueueId(420);

        RankHistory previous = new RankHistory();
        previous.setLeaguePoints(null); // null LP in previous

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), anyString()))
            .thenReturn(Optional.of(previous));

        ArgumentCaptor<RankHistory> captor = ArgumentCaptor.forClass(RankHistory.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory result = service.recordRankSnapshot(s, m, "GOLD", "III", 60);

        assertNotNull(result);
        assertNull(captor.getValue().getLpChange());
    }

    @Test
    void recordRankSnapshotcurrentWithNullLpDoesntCalculateChange() {
        Summoner s = new Summoner();
        MatchEntity m = new MatchEntity();
        m.setTimestamp(LocalDateTime.now());
        m.setQueueId(420);

        RankHistory previous = new RankHistory();
        previous.setLeaguePoints(50);

        when(repo.findFirstBySummonerAndQueueTypeOrderByTimestampDesc(eq(s), anyString()))
            .thenReturn(Optional.of(previous));

        ArgumentCaptor<RankHistory> captor = ArgumentCaptor.forClass(RankHistory.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        RankHistory result = service.recordRankSnapshot(s, m, "GOLD", "II", null);

        assertNotNull(result);
        assertNull(captor.getValue().getLpChange());
    }
}
