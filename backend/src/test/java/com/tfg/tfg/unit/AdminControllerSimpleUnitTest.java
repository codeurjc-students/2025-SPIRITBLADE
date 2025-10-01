package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tfg.tfg.controller.AdminController;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;

@ExtendWith(MockitoExtension.class)
class AdminControllerSimpleUnitTest {

    @Mock
    private UserModelRepository userRepository;

    private AdminController adminController;

    @BeforeEach
    void setUp() {
        adminController = new AdminController(userRepository);
    }

    @Test
    void testListUsers() {
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setActive(true);

        when(userRepository.findAll()).thenReturn(List.of(user));

        ResponseEntity<List<UserDTO>> response = adminController.listUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        
        UserDTO dto = response.getBody().get(0);
        assertEquals(1L, dto.getId());
        assertEquals("testuser", dto.getName());
        assertEquals("test@example.com", dto.getEmail());
        assertTrue(dto.isActive());
    }

    @Test
    void testListUsersEmpty() {
        when(userRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<UserDTO>> response = adminController.listUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testSetUserActiveSuccess() {
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(1L);
        user.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserModel.class))).thenReturn(user);

        Map<String, Object> body = Map.of("active", true);
        ResponseEntity<Void> response = adminController.setUserActive(1L, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void testSetUserActiveNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Map<String, Object> body = Map.of("active", true);
        ResponseEntity<Void> response = adminController.setUserActive(1L, body);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testSetUserInactive() {
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(1L);
        user.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserModel.class))).thenReturn(user);

        Map<String, Object> body = Map.of("active", false);
        ResponseEntity<Void> response = adminController.setUserActive(1L, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void testDeleteUserSuccess() {
        when(userRepository.existsById(1L)).thenReturn(true);

        ResponseEntity<Void> response = adminController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        ResponseEntity<Void> response = adminController.deleteUser(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testSystemStats() {
        when(userRepository.count()).thenReturn(42L);

        ResponseEntity<Map<String, Object>> response = adminController.systemStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(42L, response.getBody().get("users"));
    }

    @Test
    void testSystemStatsZeroUsers() {
        when(userRepository.count()).thenReturn(0L);

        ResponseEntity<Map<String, Object>> response = adminController.systemStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0L, response.getBody().get("users"));
    }

    @Test
    void testSetUserActiveWithNullBody() {
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(1L);
        user.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserModel.class))).thenReturn(user);

        Map<String, Object> body = new HashMap<>();
        body.put("active", null);
        ResponseEntity<Void> response = adminController.setUserActive(1L, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(user.isActive()); // Boolean.TRUE.equals(null) returns false
        verify(userRepository).save(user);
    }
}