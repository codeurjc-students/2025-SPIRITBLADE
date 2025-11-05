package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.RankHistoryDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RankHistoryDTOSimpleUnitTest {

    @Test
    void testDefaultConstructor() {
        RankHistoryDTO dto = new RankHistoryDTO();
        assertNotNull(dto);
        assertNull(dto.getDate());
        assertNull(dto.getTier());
        assertNull(dto.getRank());
        assertNull(dto.getLeaguePoints());
        assertNull(dto.getWins());
        assertNull(dto.getLosses());
    }

    @Test
    void testParameterizedConstructor() {
        RankHistoryDTO dto = new RankHistoryDTO(
            "2024-11-04", "GOLD", "I", 75, 100, 85
        );
        
        assertEquals("2024-11-04", dto.getDate());
        assertEquals("GOLD", dto.getTier());
        assertEquals("I", dto.getRank());
        assertEquals(75, dto.getLeaguePoints());
        assertEquals(100, dto.getWins());
        assertEquals(85, dto.getLosses());
    }

    @Test
    void testAllSettersAndGetters() {
        RankHistoryDTO dto = new RankHistoryDTO();
        
        dto.setDate("2024-11-05");
        dto.setTier("PLATINUM");
        dto.setRank("III");
        dto.setLeaguePoints(42);
        dto.setWins(120);
        dto.setLosses(95);
        
        assertEquals("2024-11-05", dto.getDate());
        assertEquals("PLATINUM", dto.getTier());
        assertEquals("III", dto.getRank());
        assertEquals(42, dto.getLeaguePoints());
        assertEquals(120, dto.getWins());
        assertEquals(95, dto.getLosses());
    }

    @Test
    void testBronzeTier() {
        RankHistoryDTO dto = new RankHistoryDTO(
            "2024-01-01", "BRONZE", "IV", 25, 50, 60
        );
        
        assertEquals("BRONZE", dto.getTier());
        assertEquals("IV", dto.getRank());
        assertEquals(25, dto.getLeaguePoints());
    }

    @Test
    void testSilverTier() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setTier("SILVER");
        dto.setRank("II");
        dto.setLeaguePoints(50);
        
        assertEquals("SILVER", dto.getTier());
        assertEquals("II", dto.getRank());
    }

    @Test
    void testDiamondTier() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setTier("DIAMOND");
        dto.setRank("I");
        dto.setLeaguePoints(99);
        
        assertEquals("DIAMOND", dto.getTier());
        assertEquals("I", dto.getRank());
        assertEquals(99, dto.getLeaguePoints());
    }

    @Test
    void testMasterTier() {
        RankHistoryDTO dto = new RankHistoryDTO(
            "2024-06-15", "MASTER", "I", 150, 200, 180
        );
        
        assertEquals("MASTER", dto.getTier());
        assertEquals("I", dto.getRank());
        assertEquals(150, dto.getLeaguePoints());
    }

    @Test
    void testGrandmasterTier() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setTier("GRANDMASTER");
        dto.setRank("I");
        dto.setLeaguePoints(500);
        
        assertEquals("GRANDMASTER", dto.getTier());
    }

    @Test
    void testChallengerTier() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setTier("CHALLENGER");
        dto.setRank("I");
        dto.setLeaguePoints(1000);
        dto.setWins(500);
        dto.setLosses(450);
        
        assertEquals("CHALLENGER", dto.getTier());
        assertEquals(1000, dto.getLeaguePoints());
        assertEquals(500, dto.getWins());
        assertEquals(450, dto.getLosses());
    }

    @Test
    void testZeroLeaguePoints() {
        RankHistoryDTO dto = new RankHistoryDTO();
        dto.setLeaguePoints(0);
        dto.setWins(0);
        dto.setLosses(0);
        
        assertEquals(0, dto.getLeaguePoints());
        assertEquals(0, dto.getWins());
        assertEquals(0, dto.getLosses());
    }

    @Test
    void testHighWinRate() {
        RankHistoryDTO dto = new RankHistoryDTO(
            "2024-08-20", "GOLD", "II", 60, 150, 100
        );
        
        assertEquals(150, dto.getWins());
        assertEquals(100, dto.getLosses());
        // Win rate would be 60%
    }

    @Test
    void testDateFormats() {
        RankHistoryDTO dto1 = new RankHistoryDTO();
        dto1.setDate("2024-11-04");
        assertEquals("2024-11-04", dto1.getDate());
        
        RankHistoryDTO dto2 = new RankHistoryDTO();
        dto2.setDate("04/11/2024");
        assertEquals("04/11/2024", dto2.getDate());
    }
}
