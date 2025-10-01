package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.dto.NoteDTO;

class NoteDTOSimpleUnitTest {

    @Test
    void testDefaultConstructor() {
        NoteDTO dto = new NoteDTO();
        assertNull(dto.getId());
        assertNull(dto.getText());
        assertNull(dto.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        NoteDTO dto = new NoteDTO(1L, "Test note", now);
        
        assertEquals(1L, dto.getId());
        assertEquals("Test note", dto.getText());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    void testSetters() {
        NoteDTO dto = new NoteDTO();
        LocalDateTime now = LocalDateTime.now();
        
        dto.setId(2L);
        dto.setText("Another test note");
        dto.setCreatedAt(now);
        
        assertEquals(2L, dto.getId());
        assertEquals("Another test note", dto.getText());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    void testEquals() {
        LocalDateTime now = LocalDateTime.now();
        NoteDTO dto1 = new NoteDTO(1L, "Test note", now);
        NoteDTO dto2 = new NoteDTO(1L, "Test note", now);
        NoteDTO dto3 = new NoteDTO(2L, "Different note", now);
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto3, dto1);
        assertNotEquals(null, dto1);
        assertNotEquals("not a NoteDTO", dto1);
    }

    @Test
    void testHashCode() {
        LocalDateTime now = LocalDateTime.now();
        NoteDTO dto1 = new NoteDTO(1L, "Test note", now);
        NoteDTO dto2 = new NoteDTO(1L, "Test note", now);
        
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        LocalDateTime now = LocalDateTime.now();
        NoteDTO dto = new NoteDTO(1L, "Test note", now);
        String toString = dto.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("Test note"));
    }
}