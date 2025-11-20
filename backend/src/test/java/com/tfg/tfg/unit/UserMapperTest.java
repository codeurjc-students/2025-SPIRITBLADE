package com.tfg.tfg.unit;

import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void testToDTONullUser() {
        UserDTO result = UserMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    void testToDTOValidUser() {
        UserModel user = new UserModel();
        user.setId(1L);
        user.setName("testuser");
        user.setEmail("test@example.com");
        user.setImage("image.jpg");
        user.setRols(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
        user.setActive(true);
        user.setAvatarUrl("avatar.jpg");

        UserDTO result = UserMapper.toDTO(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("image.jpg", result.getImage());
        assertEquals(Arrays.asList("ROLE_USER", "ROLE_ADMIN"), result.getRoles());
        assertTrue(result.isActive());
        assertEquals("avatar.jpg", result.getAvatarUrl());
    }

    @Test
    void testToEntityNullDTO() {
        UserModel result = UserMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void testToEntityValidDTO() {
        UserDTO dto = new UserDTO();
        dto.setName("testuser");
        dto.setEmail("test@example.com");
        dto.setImage("image.jpg");
        dto.setRoles(Arrays.asList("ROLE_USER"));
        dto.setActive(true);
        dto.setAvatarUrl("avatar.jpg");

        UserModel result = UserMapper.toEntity(dto);

        assertNotNull(result);
        assertNull(result.getId()); // ID should not be set
        assertEquals("testuser", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("image.jpg", result.getImage());
        assertEquals(Arrays.asList("ROLE_USER"), result.getRols());
        assertTrue(result.isActive());
        assertEquals("avatar.jpg", result.getAvatarUrl());
        assertNull(result.getEncodedPassword()); // Password should not be set
    }

    @Test
    void testToEntityWithNullFields() {
        UserDTO dto = new UserDTO();
        dto.setName("testuser");
        // Leave other fields null

        UserModel result = UserMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals("testuser", result.getName());
        assertNull(result.getEmail());
        assertNull(result.getImage());
        assertNull(result.getRols());
        assertFalse(result.isActive()); // Should be false when not set
        assertNull(result.getAvatarUrl());
    }
}