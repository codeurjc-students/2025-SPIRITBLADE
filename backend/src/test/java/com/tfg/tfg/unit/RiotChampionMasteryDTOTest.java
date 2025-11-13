package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RiotChampionMasteryDTO.
 * Tests remaining getters and setters not covered by existing tests.
 */
class RiotChampionMasteryDTOTest {

    @Test
    void testAllGettersAndSetters() {
        RiotChampionMasteryDTO dto = new RiotChampionMasteryDTO();
        
        // Set all fields
        dto.setPuuid("test-puuid-123");
        dto.setChampionId(157L);
        dto.setChampionLevel(7);
        dto.setChampionPoints(250000);
        dto.setChampionPointsSinceLastLevel(50000);
        dto.setChampionPointsUntilNextLevel(0);
        dto.setChestGranted(true);
        dto.setLastPlayTime(1699999999000L);
        dto.setTokensEarned(3);
        dto.setChampionName("Yasuo");
        dto.setChampionIconUrl("https://ddragon.leagueoflegends.com/cdn/14.1.1/img/champion/Yasuo.png");
        
        // Assert all getters
        assertEquals("test-puuid-123", dto.getPuuid());
        assertEquals(157L, dto.getChampionId());
        assertEquals(7, dto.getChampionLevel());
        assertEquals(250000, dto.getChampionPoints());
        assertEquals(50000, dto.getChampionPointsSinceLastLevel());
        assertEquals(0, dto.getChampionPointsUntilNextLevel());
        assertEquals(Boolean.TRUE, dto.getChestGranted());
        assertEquals(1699999999000L, dto.getLastPlayTime());
        assertEquals(3, dto.getTokensEarned());
        assertEquals("Yasuo", dto.getChampionName());
        assertEquals("https://ddragon.leagueoflegends.com/cdn/14.1.1/img/champion/Yasuo.png", dto.getChampionIconUrl());
    }

    @Test
    void testChestGrantedBoolean() {
        RiotChampionMasteryDTO dto = new RiotChampionMasteryDTO();
        
        dto.setChestGranted(true);
        assertEquals(Boolean.TRUE, dto.getChestGranted());
        
        dto.setChestGranted(false);
        assertEquals(Boolean.FALSE, dto.getChestGranted());
        
        dto.setChestGranted(null);
        assertNull(dto.getChestGranted());
    }

    @Test
    void testChampionIdLongType() {
        RiotChampionMasteryDTO dto = new RiotChampionMasteryDTO();
        
        dto.setChampionId(266L);
        assertEquals(266L, dto.getChampionId());
        
        dto.setChampionId(null);
        assertNull(dto.getChampionId());
    }

    @Test
    void testLastPlayTimeLongType() {
        RiotChampionMasteryDTO dto = new RiotChampionMasteryDTO();
        
        long timestamp = System.currentTimeMillis();
        dto.setLastPlayTime(timestamp);
        assertEquals(timestamp, dto.getLastPlayTime());
    }

    @Test
    void testChampionNameAndIconUrl() {
        RiotChampionMasteryDTO dto = new RiotChampionMasteryDTO();
        
        dto.setChampionName("Aatrox");
        dto.setChampionIconUrl("http://example.com/aatrox.png");
        
        assertEquals("Aatrox", dto.getChampionName());
        assertEquals("http://example.com/aatrox.png", dto.getChampionIconUrl());
    }

    @Test
    void testTokensEarned() {
        RiotChampionMasteryDTO dto = new RiotChampionMasteryDTO();
        
        dto.setTokensEarned(2);
        assertEquals(2, dto.getTokensEarned());
        
        dto.setTokensEarned(null);
        assertNull(dto.getTokensEarned());
    }
}
