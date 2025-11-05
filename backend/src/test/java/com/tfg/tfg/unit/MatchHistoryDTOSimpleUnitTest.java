package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.MatchHistoryDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchHistoryDTOSimpleUnitTest {

    @Test
    void testDefaultConstructor() {
        MatchHistoryDTO dto = new MatchHistoryDTO();
        assertNotNull(dto);
        assertNull(dto.getMatchId());
    }

    @Test
    void testParameterizedConstructor() {
        MatchHistoryDTO dto = new MatchHistoryDTO(
            "MATCH123", "Ahri", true, 10, 3, 15, 1800L, 1699999999L
        );
        
        assertEquals("MATCH123", dto.getMatchId());
        assertEquals("Ahri", dto.getChampionName());
        assertTrue(dto.getWin());
        assertEquals(10, dto.getKills());
        assertEquals(3, dto.getDeaths());
        assertEquals(15, dto.getAssists());
        assertEquals(1800L, dto.getGameDuration());
        assertEquals(1699999999L, dto.getGameTimestamp());
    }

    @Test
    void testAllSettersAndGetters() {
        MatchHistoryDTO dto = new MatchHistoryDTO();
        
        dto.setMatchId("MATCH456");
        dto.setChampionName("Yasuo");
        dto.setChampionIconUrl("https://icon.url");
        dto.setWin(false);
        dto.setKills(5);
        dto.setDeaths(8);
        dto.setAssists(3);
        dto.setGameDuration(2400L);
        dto.setGameTimestamp(1700000000L);
        dto.setLpAtMatch(75);
        dto.setQueueId(420);
        
        assertEquals("MATCH456", dto.getMatchId());
        assertEquals("Yasuo", dto.getChampionName());
        assertEquals("https://icon.url", dto.getChampionIconUrl());
        assertFalse(dto.getWin());
        assertEquals(5, dto.getKills());
        assertEquals(8, dto.getDeaths());
        assertEquals(3, dto.getAssists());
        assertEquals(2400L, dto.getGameDuration());
        assertEquals(1700000000L, dto.getGameTimestamp());
        assertEquals(75, dto.getLpAtMatch());
        assertEquals(420, dto.getQueueId());
    }

    @Test
    void testGetKda() {
        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setKills(10);
        dto.setDeaths(5);
        dto.setAssists(8);
        
        String kda = dto.getKda();
        assertEquals("10/5/8", kda);
    }

    @Test
    void testGetKda_WithZeroValues() {
        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setKills(0);
        dto.setDeaths(0);
        dto.setAssists(0);
        
        String kda = dto.getKda();
        assertEquals("0/0/0", kda);
    }

    @Test
    @SuppressWarnings("java:S5976") // Tests intentionally kept simple and readable
    void testGetFormattedDuration_WithValidDuration() {
        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setGameDuration(1865L); // 31 minutes and 5 seconds
        
        String formatted = dto.getFormattedDuration();
        assertEquals("31m", formatted);
    }

    @Test
    void testGetFormattedDuration_WithNullDuration() {
        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setGameDuration(null);
        
        String formatted = dto.getFormattedDuration();
        assertEquals("0m", formatted);
    }

    @Test
    void testGetFormattedDuration_ExactMinutes() {
        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setGameDuration(1800L); // Exactly 30 minutes
        
        String formatted = dto.getFormattedDuration();
        assertEquals("30m", formatted);
    }

    @Test
    void testGetFormattedDuration_LessThanMinute() {
        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setGameDuration(45L); // 45 seconds
        
        String formatted = dto.getFormattedDuration();
        assertEquals("0m", formatted);
    }

    @Test
    void testQueueIdValues() {
        MatchHistoryDTO dto = new MatchHistoryDTO();
        
        // Solo/Duo
        dto.setQueueId(420);
        assertEquals(420, dto.getQueueId());
        
        // Flex
        dto.setQueueId(440);
        assertEquals(440, dto.getQueueId());
        
        // ARAM
        dto.setQueueId(450);
        assertEquals(450, dto.getQueueId());
    }

    @Test
    void testCompleteMatchHistory() {
        MatchHistoryDTO dto = new MatchHistoryDTO(
            "EUW1_123456", "Lux", true, 7, 2, 20, 2100L, 1700000000L
        );
        dto.setChampionIconUrl("https://ddragon.leagueoflegends.com/cdn/13.21.1/img/champion/Lux.png");
        dto.setLpAtMatch(85);
        dto.setQueueId(420);
        
        assertEquals("EUW1_123456", dto.getMatchId());
        assertEquals("Lux", dto.getChampionName());
        assertNotNull(dto.getChampionIconUrl());
        assertTrue(dto.getWin());
        assertEquals("7/2/20", dto.getKda());
        assertEquals("35m", dto.getFormattedDuration());
        assertEquals(85, dto.getLpAtMatch());
        assertEquals(420, dto.getQueueId());
    }
}
