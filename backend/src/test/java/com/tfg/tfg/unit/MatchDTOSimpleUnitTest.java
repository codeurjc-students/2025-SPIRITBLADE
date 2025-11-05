package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.MatchDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MatchDTOSimpleUnitTest {

    @Test
    void testNoArgsConstructor() {
        MatchDTO dto = new MatchDTO();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getMatchId());
        assertNull(dto.getTimestamp());
        assertFalse(dto.isWin());
        assertEquals(0, dto.getKills());
        assertEquals(0, dto.getDeaths());
        assertEquals(0, dto.getAssists());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        MatchDTO dto = new MatchDTO(1L, "MATCH123", now, true, 10, 5, 8);
        
        assertEquals(1L, dto.getId());
        assertEquals("MATCH123", dto.getMatchId());
        assertEquals(now, dto.getTimestamp());
        assertTrue(dto.isWin());
        assertEquals(10, dto.getKills());
        assertEquals(5, dto.getDeaths());
        assertEquals(8, dto.getAssists());
    }

    @Test
    void testSettersAndGetters() {
        MatchDTO dto = new MatchDTO();
        LocalDateTime now = LocalDateTime.now();
        
        dto.setId(2L);
        dto.setMatchId("MATCH456");
        dto.setTimestamp(now);
        dto.setWin(false);
        dto.setKills(3);
        dto.setDeaths(7);
        dto.setAssists(2);
        
        assertEquals(2L, dto.getId());
        assertEquals("MATCH456", dto.getMatchId());
        assertEquals(now, dto.getTimestamp());
        assertFalse(dto.isWin());
        assertEquals(3, dto.getKills());
        assertEquals(7, dto.getDeaths());
        assertEquals(2, dto.getAssists());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        MatchDTO dto1 = new MatchDTO(1L, "MATCH123", now, true, 10, 5, 8);
        MatchDTO dto2 = new MatchDTO(1L, "MATCH123", now, true, 10, 5, 8);
        MatchDTO dto3 = new MatchDTO(2L, "MATCH456", now, false, 3, 7, 2);
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        LocalDateTime now = LocalDateTime.now();
        MatchDTO dto = new MatchDTO(1L, "MATCH123", now, true, 10, 5, 8);
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("MATCH123"));
    }
}
