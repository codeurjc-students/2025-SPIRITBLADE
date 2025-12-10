package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.dto.UserDTO;

class UserDTOUnitTest {

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(userDTO);
        assertNull(userDTO.getId());
        assertNull(userDTO.getName());
        assertNull(userDTO.getEmail());
        assertNull(userDTO.getPassword());
        assertNull(userDTO.getImage());
        assertNull(userDTO.getRoles());
        assertFalse(userDTO.isActive());
    }

    @Test
    void testSettersAndGetters() {
        userDTO.setId(1L);
        userDTO.setName("testUser");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password123");
        userDTO.setImage("profile.jpg");
        userDTO.setRoles(List.of("USER", "ADMIN"));
        userDTO.setActive(true);

        assertEquals(1L, userDTO.getId());
        assertEquals("testUser", userDTO.getName());
        assertEquals("test@example.com", userDTO.getEmail());
        assertEquals("password123", userDTO.getPassword());
        assertEquals("profile.jpg", userDTO.getImage());
        assertEquals(List.of("USER", "ADMIN"), userDTO.getRoles());
        assertTrue(userDTO.isActive());
    }

    @Test
    void testIdHandling() {
        assertNull(userDTO.getId());
        
        userDTO.setId(100L);
        assertEquals(100L, userDTO.getId());
        
        userDTO.setId(null);
        assertNull(userDTO.getId());
    }

    @Test
    void testNameHandling() {
        assertNull(userDTO.getName());
        
        userDTO.setName("John Doe");
        assertEquals("John Doe", userDTO.getName());
        
        userDTO.setName("");
        assertEquals("", userDTO.getName());
        
        userDTO.setName(null);
        assertNull(userDTO.getName());
    }

    @Test
    void testEmailHandling() {
        assertNull(userDTO.getEmail());
        
        userDTO.setEmail("john@example.com");
        assertEquals("john@example.com", userDTO.getEmail());
        
        userDTO.setEmail("");
        assertEquals("", userDTO.getEmail());
        
        userDTO.setEmail(null);
        assertNull(userDTO.getEmail());
    }

    @Test
    void testPasswordHandling() {
        assertNull(userDTO.getPassword());
        
        userDTO.setPassword("secretPassword");
        assertEquals("secretPassword", userDTO.getPassword());
        
        userDTO.setPassword("");
        assertEquals("", userDTO.getPassword());
        
        userDTO.setPassword(null);
        assertNull(userDTO.getPassword());
    }

    @Test
    void testImageHandling() {
        assertNull(userDTO.getImage());
        
        userDTO.setImage("avatar.png");
        assertEquals("avatar.png", userDTO.getImage());
        
        userDTO.setImage("");
        assertEquals("", userDTO.getImage());
        
        userDTO.setImage(null);
        assertNull(userDTO.getImage());
    }

    @Test
    void testRolesHandling() {
        assertNull(userDTO.getRoles());
        
        List<String> roles = List.of("USER");
        userDTO.setRoles(roles);
        assertEquals(roles, userDTO.getRoles());
        
        List<String> multipleRoles = List.of("USER", "ADMIN", "MODERATOR");
        userDTO.setRoles(multipleRoles);
        assertEquals(multipleRoles, userDTO.getRoles());
        assertEquals(3, userDTO.getRoles().size());
        
        userDTO.setRoles(List.of());
        assertTrue(userDTO.getRoles().isEmpty());
        
        userDTO.setRoles(null);
        assertNull(userDTO.getRoles());
    }

    @Test
    void testActiveStatusHandling() {
        assertFalse(userDTO.isActive()); // Default is false
        
        userDTO.setActive(true);
        assertTrue(userDTO.isActive());
        
        userDTO.setActive(false);
        assertFalse(userDTO.isActive());
    }

    @Test
    void testCompleteUserDTOSetup() {
        userDTO.setId(42L);
        userDTO.setName("Complete User");
        userDTO.setEmail("complete@example.com");
        userDTO.setPassword("strongPassword");
        userDTO.setImage("complete_avatar.jpg");
        userDTO.setRoles(List.of("USER", "ADMIN"));
        userDTO.setActive(true);

        // Verify all fields are set correctly
        assertEquals(42L, userDTO.getId());
        assertEquals("Complete User", userDTO.getName());
        assertEquals("complete@example.com", userDTO.getEmail());
        assertEquals("strongPassword", userDTO.getPassword());
        assertEquals("complete_avatar.jpg", userDTO.getImage());
        assertEquals(2, userDTO.getRoles().size());
        assertTrue(userDTO.getRoles().contains("USER"));
        assertTrue(userDTO.getRoles().contains("ADMIN"));
        assertTrue(userDTO.isActive());
    }

    @Test
    void testValidation() {
        jakarta.validation.Validator validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        
        // Valid user
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("123456");
        assertTrue(validator.validate(userDTO).isEmpty());
        
        // Invalid email
        userDTO.setEmail("invalid-email");
        assertFalse(validator.validate(userDTO).isEmpty());
        
        // Invalid password (too short)
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("12345");
        assertFalse(validator.validate(userDTO).isEmpty());
    }
}