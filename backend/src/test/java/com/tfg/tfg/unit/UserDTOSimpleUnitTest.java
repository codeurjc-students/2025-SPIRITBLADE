package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.dto.UserDTO;

class UserDTOSimpleUnitTest {

    @Test
    void testUserDTOCreation() {
        UserDTO dto = new UserDTO();
        assertNotNull(dto);
    }

    @Test
    void testUserDTOSettersAndGetters() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setName("testuser");
        dto.setEmail("test@example.com");
        dto.setActive(true);
        dto.setImage("/path/to/image");
        dto.setRoles(List.of("USER"));
        
        assertEquals(1L, dto.getId());
        assertEquals("testuser", dto.getName());
        assertEquals("test@example.com", dto.getEmail());
        assertTrue(dto.isActive());
        assertEquals("/path/to/image", dto.getImage());
        assertEquals(1, dto.getRoles().size());
        assertTrue(dto.getRoles().contains("USER"));
    }

    @Test
    void testUserDTOWithMultipleRoles() {
        UserDTO dto = new UserDTO();
        dto.setRoles(List.of("USER", "ADMIN"));
        
        assertEquals(2, dto.getRoles().size());
        assertTrue(dto.getRoles().contains("USER"));
        assertTrue(dto.getRoles().contains("ADMIN"));
    }

    @Test
    void testUserDTONullValues() {
        UserDTO dto = new UserDTO();
        dto.setName(null);
        dto.setEmail(null);
        dto.setRoles(null);
        
        assertNull(dto.getName());
        assertNull(dto.getEmail());
        assertNull(dto.getRoles());
    }

    @Test
    void testUserDTOEmptyRoles() {
        UserDTO dto = new UserDTO();
        dto.setRoles(List.of());
        
        assertTrue(dto.getRoles().isEmpty());
    }

    @Test
    void testUserDTOActiveDefault() {
        UserDTO dto = new UserDTO();
        // By default, active is false for primitive boolean
        assertFalse(dto.isActive());
    }

    @Test
    void testUserDTOSetActiveTrue() {
        UserDTO dto = new UserDTO();
        dto.setActive(true);
        assertTrue(dto.isActive());
    }

    @Test
    void testUserDTOSetActiveFalse() {
        UserDTO dto = new UserDTO();
        dto.setActive(false);
        assertFalse(dto.isActive());
    }

    @Test
    void testUserDTOImageHandling() {
        UserDTO dto = new UserDTO();
        String imagePath = "/users/123/image";
        dto.setImage(imagePath);
        assertEquals(imagePath, dto.getImage());
    }

    @Test
    void testUserDTOLongId() {
        UserDTO dto = new UserDTO();
        Long id = 999999999L;
        dto.setId(id);
        assertEquals(id, dto.getId());
    }

    @Test
    void testUserDTOCompleteUser() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setName("johndoe");
        dto.setEmail("john@example.com");
        dto.setActive(true);
        dto.setImage("/users/1/image");
        dto.setRoles(List.of("USER", "MODERATOR"));
        
        assertEquals(1L, dto.getId());
        assertEquals("johndoe", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
        assertTrue(dto.isActive());
        assertEquals("/users/1/image", dto.getImage());
        assertEquals(2, dto.getRoles().size());
    }
}