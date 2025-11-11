package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.entity.RankHistory;
import com.tfg.tfg.model.entity.Summoner;

/**
 * Unit tests for RankHistory entity.
 */
class RankHistoryUnitTest {

    private Summoner testSummoner;
    private RankHistory rankHistory;

    @BeforeEach
    void setUp() {
        testSummoner = new Summoner();
        testSummoner.setId(1L);
        testSummoner.setName("TestPlayer");
        testSummoner.setPuuid("test-puuid");

        rankHistory = new RankHistory();
    }

    @Test
    void testRankHistoryCreation() {
        rankHistory.setSummoner(testSummoner);
        rankHistory.setTimestamp(LocalDateTime.now());
        rankHistory.setTier("GOLD");
        rankHistory.setRank("II");
        rankHistory.setLeaguePoints(67);
        rankHistory.setWins(100);
        rankHistory.setLosses(95);
        rankHistory.setQueueType("RANKED_SOLO_5x5");
        rankHistory.setLpChange(18);

        assertNotNull(rankHistory);
        assertEquals("GOLD", rankHistory.getTier());
        assertEquals("II", rankHistory.getRank());
        assertEquals(67, rankHistory.getLeaguePoints());
        assertEquals(18, rankHistory.getLpChange());
    }

    @Test
    void testConstructorWithParameters() {
        LocalDateTime now = LocalDateTime.now();
        RankHistory history = new RankHistory(
            testSummoner, now, "PLATINUM", "IV", 
            25, 50, 48, "RANKED_SOLO_5x5"
        );

        assertEquals(testSummoner, history.getSummoner());
        assertEquals(now, history.getTimestamp());
        assertEquals("PLATINUM", history.getTier());
        assertEquals("IV", history.getRank());
        assertEquals(25, history.getLeaguePoints());
        assertEquals(50, history.getWins());
        assertEquals(48, history.getLosses());
        assertEquals("RANKED_SOLO_5x5", history.getQueueType());
    }

    @Test
    void testGetTotalGames() {
        rankHistory.setWins(100);
        rankHistory.setLosses(95);

        assertEquals(195, rankHistory.getTotalGames());
    }

    @Test
    void testGetTotalGamesWithNulls() {
        rankHistory.setWins(null);
        rankHistory.setLosses(null);

        assertEquals(0, rankHistory.getTotalGames());
    }

    @Test
    void testGetWinRate() {
        rankHistory.setWins(60);
        rankHistory.setLosses(40);

        assertEquals(60.0, rankHistory.getWinRate(), 0.01);
    }

    @Test
    void testGetWinRateWithZeroGames() {
        rankHistory.setWins(0);
        rankHistory.setLosses(0);

        assertEquals(0.0, rankHistory.getWinRate(), 0.01);
    }

    @Test
    void testGetFormattedRank() {
        rankHistory.setTier("GOLD");
        rankHistory.setRank("II");

        assertEquals("GOLD II", rankHistory.getFormattedRank());
    }

    @Test
    void testGetFormattedRankForMaster() {
        rankHistory.setTier("MASTER");
        rankHistory.setRank(null);

        assertEquals("MASTER", rankHistory.getFormattedRank());
    }

    @Test
    void testGetFormattedRankForUnranked() {
        rankHistory.setTier(null);
        rankHistory.setRank(null);

        assertEquals("Unranked", rankHistory.getFormattedRank());
    }

    @Test
    void testGetFullRankString() {
        rankHistory.setTier("DIAMOND");
        rankHistory.setRank("III");
        rankHistory.setLeaguePoints(45);

        assertEquals("DIAMOND III (45 LP)", rankHistory.getFullRankString());
    }

    @Test
    void testGetFullRankStringWithoutLP() {
        rankHistory.setTier("SILVER");
        rankHistory.setRank("I");
        rankHistory.setLeaguePoints(null);

        assertEquals("SILVER I", rankHistory.getFullRankString());
    }

    @Test
    void testLpChangePositive() {
        rankHistory.setLpChange(22);

        assertEquals(22, rankHistory.getLpChange());
        assertTrue(rankHistory.getLpChange() > 0);
    }

    @Test
    void testLpChangeNegative() {
        rankHistory.setLpChange(-16);

        assertEquals(-16, rankHistory.getLpChange());
        assertTrue(rankHistory.getLpChange() < 0);
    }

    @Test
    void testToString() {
        rankHistory.setId(1L);
        rankHistory.setSummoner(testSummoner);
        rankHistory.setTier("GOLD");
        rankHistory.setRank("II");
        rankHistory.setLeaguePoints(67);
        rankHistory.setTimestamp(LocalDateTime.of(2025, 11, 10, 12, 0));

        String result = rankHistory.toString();
        
        assertTrue(result.contains("RankHistory"));
        assertTrue(result.contains("TestPlayer"));
        assertTrue(result.contains("GOLD II"));
        assertTrue(result.contains("67"));
    }

    @Test
    void testQueueTypes() {
        rankHistory.setQueueType("RANKED_SOLO_5x5");
        assertEquals("RANKED_SOLO_5x5", rankHistory.getQueueType());

        rankHistory.setQueueType("RANKED_FLEX_SR");
        assertEquals("RANKED_FLEX_SR", rankHistory.getQueueType());
    }

    @Test
    void testTimestampHandling() {
        LocalDateTime now = LocalDateTime.now();
        rankHistory.setTimestamp(now);

        assertEquals(now, rankHistory.getTimestamp());
        assertNotNull(rankHistory.getTimestamp());
    }
}
