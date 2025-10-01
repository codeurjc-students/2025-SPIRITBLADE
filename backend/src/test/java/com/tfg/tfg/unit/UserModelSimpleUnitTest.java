package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.entity.UserModel;

class UserModelSimpleUnitTest {

    @Test
    void testUserModelCreation() {
        UserModel user = new UserModel("testuser", "password", "USER");
        
        assertEquals("testuser", user.getName());
        assertEquals("password", user.getEncodedPassword());
        assertTrue(user.getRols().contains("USER"));
        assertTrue(user.isActive());
    }

    @Test
    void testUserModelWithMultipleRoles() {
        UserModel user = new UserModel("admin", "password", "USER", "ADMIN");
        
        assertEquals(2, user.getRols().size());
        assertTrue(user.getRols().contains("USER"));
        assertTrue(user.getRols().contains("ADMIN"));
    }

    @Test
    void testUserModelSetters() {
        UserModel user = new UserModel();
        user.setName("testuser");
        user.setEmail("test@example.com");
        user.setActive(false);
        user.setRols(List.of("USER"));
        
        assertEquals("testuser", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertFalse(user.isActive());
        assertEquals(1, user.getRols().size());
    }

    @Test
    void testDetermineUserTypeAdmin() {
        UserModel user = new UserModel("admin", "password", "ADMIN");
        assertEquals("Administrator", user.determineUserType());
    }

    @Test
    void testDetermineUserTypeUser() {
        UserModel user = new UserModel("user", "password", "USER");
        assertEquals("Registered User", user.determineUserType());
    }

    @Test
    void testDetermineUserTypeUnknown() {
        UserModel user = new UserModel("guest", "password");
        assertEquals("Unknown", user.determineUserType());
    }

    @Test
    void testUserModelWithoutRoles() {
        UserModel user = new UserModel("user", "password");
        assertTrue(user.getRols().isEmpty());
        assertEquals("Unknown", user.determineUserType());
    }

    @Test
    void testUserModelActiveByDefault() {
        UserModel user = new UserModel("user", "password", "USER");
        assertTrue(user.isActive());
    }

    @Test
    void testSetUserInactive() {
        UserModel user = new UserModel("user", "password", "USER");
        user.setActive(false);
        assertFalse(user.isActive());
    }

    @Test
    void testUserWithMultipleRolesPriority() {
        UserModel user = new UserModel("superuser", "password", "USER", "ADMIN");
        assertEquals("Administrator", user.determineUserType());
    }

    @Test
    void testImagePathGeneration() {
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(1L);
        user.setImage("/users/" + user.getId() + "/image");
        assertEquals("/users/1/image", user.getImage());
    }

    @Test
    void testUserIdSetting() {
        UserModel user = new UserModel();
        user.setId(123L);
        assertEquals(123L, user.getId());
    }
}