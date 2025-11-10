package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.entity.UserModel;

class UserModelUnitTest {

    private UserModel userModel;

    @BeforeEach
    void setUp() {
        userModel = new UserModel();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(userModel);
        assertTrue(userModel.isActive());
    }

    @Test
    void testParameterizedConstructor() {
        UserModel user = new UserModel("testUser", "encodedPassword", "USER", "ADMIN");
        
        assertEquals("testUser", user.getName());
        assertEquals("encodedPassword", user.getEncodedPassword());
        assertEquals(List.of("USER", "ADMIN"), user.getRols());
        assertTrue(user.isActive());
    }

    @Test
    void testParameterizedConstructorWithNullRoles() {
        UserModel user = new UserModel("testUser", "encodedPassword", (String[]) null);
        
        assertEquals("testUser", user.getName());
        assertEquals("encodedPassword", user.getEncodedPassword());
        assertTrue(user.getRols().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        userModel.setId(1L);
        userModel.setName("testUser");
        userModel.setEmail("test@example.com");
        userModel.setImage("test-image.jpg");
        userModel.setActive(false);
        userModel.setRols(List.of("USER"));

        assertEquals(1L, userModel.getId());
        assertEquals("testUser", userModel.getName());
        assertEquals("test@example.com", userModel.getEmail());
        assertEquals("test-image.jpg", userModel.getImage());
        assertFalse(userModel.isActive());
        assertEquals(List.of("USER"), userModel.getRols());
    }

    @Test
    void testDetermineUserTypeAdmin() {
        userModel.setRols(List.of("ADMIN", "USER"));
        assertEquals("Administrator", userModel.determineUserType());
    }

    @Test
    void testDetermineUserTypeUser() {
        userModel.setRols(List.of("USER"));
        assertEquals("Registered User", userModel.determineUserType());
    }

    @Test
    void testDetermineUserTypeUnknown() {
        userModel.setRols(List.of("SOME_OTHER_ROLE"));
        assertEquals("Unknown", userModel.determineUserType());
    }

    @Test
    void testDetermineUserTypeEmptyRoles() {
        userModel.setRols(List.of());
        assertEquals("Unknown", userModel.determineUserType());
    }

    @Test
    void testPasswordSetter() {
        userModel.setPass("newPassword");
        assertEquals("newPassword", userModel.getEncodedPassword());
    }

    @Test
    void testUserWithOnlyUserRole() {
        UserModel user = new UserModel("user", "pass", "USER");
        assertEquals("Registered User", user.determineUserType());
        assertEquals(List.of("USER"), user.getRols());
    }

    @Test
    void testUserWithMultipleRolesButAdminFirst() {
        UserModel user = new UserModel("admin", "pass", "ADMIN", "USER", "MANAGER");
        assertEquals("Administrator", user.determineUserType());
        assertTrue(user.getRols().contains("ADMIN"));
        assertTrue(user.getRols().contains("USER"));
        assertTrue(user.getRols().contains("MANAGER"));
    }

    @Test
    void testActiveStatusToggle() {
        assertTrue(userModel.isActive()); // Default is true
        
        userModel.setActive(false);
        assertFalse(userModel.isActive());
        
        userModel.setActive(true);
        assertTrue(userModel.isActive());
    }
}