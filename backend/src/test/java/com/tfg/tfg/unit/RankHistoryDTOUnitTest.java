package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.dto.RankHistoryDTO;

/**
 * Unit tests for RankHistoryDTO to achieve maximum code coverage.
 * Tests all constructors, getters, setters, calculated fields, and edge cases.
 */
public class RankHistoryDTOUnitTest {

    @Test
    public void testDefaultConstructor() {
        RankHistoryDTO dto = new RankHistoryDTO();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getTier());
        assertNull(dto.getRank());
    }

    @Test
    public void testBackwardCompatibleConstructor() {
        RankHistoryDTO dto = new RankHistoryDTO("2025-11-10", "GOLD", "II", 50, 10, 5);
        
        assertEquals("2025-11-10", dto.getDate());
        assertEquals("GOLD", dto.getTier());
        assertEquals("II", dto.getRank());
        assertEquals(50, dto.getLeaguePoints());
        assertEquals(10, dto.getWins());
        assertEquals(5, dto.getLosses());
        assertEquals("GOLD II", dto.getFormattedRank());
        assertEquals(15, dto.getTotalGames());
        assertEquals(66.66666666666666, dto.getWinRate(), 0.001);
    }

    @Test
    public void testEnhancedConstructor() {
        LocalDateTime now = LocalDateTime.now();
        RankHistoryDTO dto = new RankHistoryDTO(
            1L, "TestSummoner", now, "PLATINUM", "III", 75, 20, 15, "RANKED_SOLO_5x5", 15
        );
        
        assertEquals(1L, dto.getId());
        assertEquals("TestSummoner", dto.getSummonerName());
        assertEquals(now, dto.getTimestamp());
        assertEquals("PLATINUM", dto.getTier());
        assertEquals("III", dto.getRank());
        assertEquals(75, dto.getLeaguePoints());
        assertEquals(20, dto.getWins());
        assertEquals(15, dto.getLosses());
        assertEquals("RANKED_SOLO_5x5", dto.getQueueType());
        assertEquals(15, dto.getLpChange());
        assertEquals("PLATINUM III", dto.getFormattedRank());
        assertEquals(35, dto.getTotalGames());
        assertEquals(57.142857142857146, dto.getWinRate(), 0.001);
    }

    @Test
    public void testFormatRankWithNullTier() {
        RankHistoryDTO dto = new RankHistoryDTO("2025-11-10", null, "II", 50, 10, 5);
        assertEquals("Unranked", dto.getFormattedRank());
    }

    @Test
    public void testFormatRankWithNullRank() {
        RankHistoryDTO dto = new RankHistoryDTO("2025-11-10", "DIAMOND", null, 80, 30, 10);
        assertEquals("DIAMOND", dto.getFormattedRank());
    }

    @Test
    public void testFormatRankWithEmptyRank() {
        RankHistoryDTO dto = new RankHistoryDTO("2025-11-10", "MASTER", "", 100, 50, 20);
        assertEquals("MASTER", dto.getFormattedRank());
    }

    @Test
    public void testWinRateCalculationWithZeroGames() {
        RankHistoryDTO dto = new RankHistoryDTO("2025-11-10", "SILVER", "I", 30, 0, 0);
        assertEquals(0, dto.getTotalGames());
        assertEquals(0.0, dto.getWinRate());
    }

    @Test
    public void testWinRateCalculationWith100PercentWins() {
        RankHistoryDTO dto = new RankHistoryDTO("2025-11-10", "GOLD", "IV", 40, 25, 0);
        assertEquals(25, dto.getTotalGames());
        assertEquals(100.0, dto.getWinRate());
    }

    @Test
    public void testWinRateCalculationWith0PercentWins() {
        RankHistoryDTO dto = new RankHistoryDTO("2025-11-10", "BRONZE", "III", 10, 0, 30);
        assertEquals(30, dto.getTotalGames());
        assertEquals(0.0, dto.getWinRate());
    }

    @Test
    public void testSettersAndRecalculateStats() {
        RankHistoryDTO dto = new RankHistoryDTO();
        
        dto.setId(100L);
        assertEquals(100L, dto.getId());
        
        dto.setSummonerName("PlayerOne");
        assertEquals("PlayerOne", dto.getSummonerName());
        
        LocalDateTime timestamp = LocalDateTime.of(2025, 11, 10, 14, 30);
        dto.setTimestamp(timestamp);
        assertEquals(timestamp, dto.getTimestamp());
        
        dto.setDate("2025-11-10");
        assertEquals("2025-11-10", dto.getDate());
        
        dto.setTier("GOLD");
        assertEquals("GOLD", dto.getTier());
        
        dto.setRank("I");
        assertEquals("I", dto.getRank());
        assertEquals("GOLD I", dto.getFormattedRank());
        
        dto.setLeaguePoints(65);
        assertEquals(65, dto.getLeaguePoints());
        
        dto.setWins(15);
        assertEquals(15, dto.getWins());
        
        dto.setLosses(10);
        assertEquals(10, dto.getLosses());
        assertEquals(25, dto.getTotalGames());
        assertEquals(60.0, dto.getWinRate());
        
        dto.setQueueType("RANKED_FLEX_SR");
        assertEquals("RANKED_FLEX_SR", dto.getQueueType());
        
        dto.setLpChange(20);
        assertEquals(20, dto.getLpChange());
    }

    @Test
    public void testSetTierUpdatesFormattedRank() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setRank("II");
        dto.setTier("PLATINUM");
        assertEquals("PLATINUM II", dto.getFormattedRank());
    }

    @Test
    public void testSetRankUpdatesFormattedRank() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setTier("DIAMOND");
        dto.setRank("IV");
        assertEquals("DIAMOND IV", dto.getFormattedRank());
    }

    @Test
    public void testSetWinsRecalculatesStats() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setLosses(20);
        dto.setWins(40);
        
        assertEquals(60, dto.getTotalGames());
        assertEquals(66.66666666666666, dto.getWinRate(), 0.001);
    }

    @Test
    public void testSetLossesRecalculatesStats() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setWins(30);
        dto.setLosses(30);
        
        assertEquals(60, dto.getTotalGames());
        assertEquals(50.0, dto.getWinRate());
    }

    @Test
    public void testRecalculateStatsWithNullWins() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setWins(null);
        dto.setLosses(10);
        
        assertEquals(10, dto.getTotalGames());
        assertEquals(0.0, dto.getWinRate());
    }

    @Test
    public void testRecalculateStatsWithNullLosses() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setWins(25);
        dto.setLosses(null);
        
        assertEquals(25, dto.getTotalGames());
        assertEquals(100.0, dto.getWinRate());
    }

    @Test
    public void testRecalculateStatsWithBothNull() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setWins(null);
        dto.setLosses(null);
        
        assertEquals(0, dto.getTotalGames());
        assertEquals(0.0, dto.getWinRate());
    }

    @Test
    public void testSetFormattedRank() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setFormattedRank("CHALLENGER");
        assertEquals("CHALLENGER", dto.getFormattedRank());
    }

    @Test
    public void testSetWinRate() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setWinRate(75.5);
        assertEquals(75.5, dto.getWinRate());
    }

    @Test
    public void testSetTotalGames() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setTotalGames(150);
        assertEquals(150, dto.getTotalGames());
    }

    @Test
    public void testToStringWithLpChange() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 11, 10, 14, 30);
        RankHistoryDTO dto = new RankHistoryDTO(
            1L, "TestPlayer", timestamp, "GOLD", "II", 50, 10, 5, "RANKED_SOLO_5x5", 15
        );
        
        String result = dto.toString();
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("rank=GOLD II"));
        assertTrue(result.contains("lp=50"));
        assertTrue(result.contains("lpChange=15"));
        assertTrue(result.contains("timestamp=2025-11-10T14:30"));
    }

    @Test
    public void testToStringWithoutLpChange() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 11, 10, 14, 30);
        RankHistoryDTO dto = new RankHistoryDTO(
            2L, "PlayerTwo", timestamp, "PLATINUM", "I", 85, 20, 10, "RANKED_SOLO_5x5", null
        );
        
        String result = dto.toString();
        assertTrue(result.contains("id=2"));
        assertTrue(result.contains("rank=PLATINUM I"));
        assertTrue(result.contains("lp=85"));
        assertTrue(result.contains("lpChange=N/A"));
        assertTrue(result.contains("timestamp=2025-11-10T14:30"));
    }

    @Test
    public void testComplexScenario() {
        // Test a realistic scenario with all features
        LocalDateTime now = LocalDateTime.now();
        RankHistoryDTO dto = new RankHistoryDTO(
            999L, "ComplexPlayer", now, "EMERALD", "III", 42, 55, 45, "RANKED_FLEX_SR", -18
        );
        
        // Verify all initial values
        assertEquals(999L, dto.getId());
        assertEquals("ComplexPlayer", dto.getSummonerName());
        assertEquals("EMERALD", dto.getTier());
        assertEquals("III", dto.getRank());
        assertEquals(42, dto.getLeaguePoints());
        assertEquals(55, dto.getWins());
        assertEquals(45, dto.getLosses());
        assertEquals("RANKED_FLEX_SR", dto.getQueueType());
        assertEquals(-18, dto.getLpChange());
        assertEquals("EMERALD III", dto.getFormattedRank());
        assertEquals(100, dto.getTotalGames());
        assertEquals(55.0, dto.getWinRate());
        
        // Modify and verify recalculations
        dto.setWins(60);
        assertEquals(105, dto.getTotalGames());
        assertEquals(57.142857142857146, dto.getWinRate(), 0.001);
        
        dto.setLosses(50);
        assertEquals(110, dto.getTotalGames());
        assertEquals(54.54545454545454, dto.getWinRate(), 0.001);
        
        // Change rank
        dto.setTier("DIAMOND");
        dto.setRank("IV");
        assertEquals("DIAMOND IV", dto.getFormattedRank());
    }
}
