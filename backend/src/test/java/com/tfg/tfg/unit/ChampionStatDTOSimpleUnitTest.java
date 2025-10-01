package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.dto.ChampionStatDTO;

class ChampionStatDTOSimpleUnitTest {

    @Test
    void testDefaultConstructor() {
        ChampionStatDTO dto = new ChampionStatDTO();
        assertNull(dto.getId());
        assertNull(dto.getChampionId());
        assertEquals(0, dto.getGamesPlayed());
        assertEquals(0, dto.getWins());
        assertEquals(0, dto.getKills());
        assertEquals(0, dto.getDeaths());
        assertEquals(0, dto.getAssists());
    }

    @Test
    void testAllArgsConstructor() {
        ChampionStatDTO dto = new ChampionStatDTO(1L, 101, 5, 3, 10, 2, 8);
        assertEquals(1L, dto.getId());
        assertEquals(101, dto.getChampionId());
        assertEquals(5, dto.getGamesPlayed());
        assertEquals(3, dto.getWins());
        assertEquals(10, dto.getKills());
        assertEquals(2, dto.getDeaths());
        assertEquals(8, dto.getAssists());
    }

    @Test
    void testSetters() {
        ChampionStatDTO dto = new ChampionStatDTO();
        
        dto.setId(2L);
        dto.setChampionId(102);
        dto.setGamesPlayed(10);
        dto.setWins(7);
        dto.setKills(25);
        dto.setDeaths(5);
        dto.setAssists(15);
        
        assertEquals(2L, dto.getId());
        assertEquals(102, dto.getChampionId());
        assertEquals(10, dto.getGamesPlayed());
        assertEquals(7, dto.getWins());
        assertEquals(25, dto.getKills());
        assertEquals(5, dto.getDeaths());
        assertEquals(15, dto.getAssists());
    }

    @Test
    void testEquals() {
        ChampionStatDTO dto1 = new ChampionStatDTO(1L, 101, 5, 3, 10, 2, 8);
        ChampionStatDTO dto2 = new ChampionStatDTO(1L, 101, 5, 3, 10, 2, 8);
        ChampionStatDTO dto3 = new ChampionStatDTO(2L, 102, 6, 4, 12, 3, 9);
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto3, dto1);
        assertNotEquals(null, dto1);
        assertNotEquals("not a ChampionStatDTO", dto1);
    }

    @Test
    void testHashCode() {
        ChampionStatDTO dto1 = new ChampionStatDTO(1L, 101, 5, 3, 10, 2, 8);
        ChampionStatDTO dto2 = new ChampionStatDTO(1L, 101, 5, 3, 10, 2, 8);
        
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        ChampionStatDTO dto = new ChampionStatDTO(1L, 101, 5, 3, 10, 2, 8);
        String toString = dto.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("101"));
        assertTrue(toString.contains("5"));
        assertTrue(toString.contains("3"));
        assertTrue(toString.contains("10"));
        assertTrue(toString.contains("2"));
        assertTrue(toString.contains("8"));
    }
}