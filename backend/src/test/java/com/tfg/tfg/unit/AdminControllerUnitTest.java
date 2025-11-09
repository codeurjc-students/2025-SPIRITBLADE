package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.tfg.tfg.service.UserService;

@ExtendWith(MockitoExtension.class)
class AdminControllerUnitTest {

    @Mock
    private UserService userService;
    
    private AdminController adminController;
    
    @BeforeEach
    void setUp() {
        adminController = new AdminController(userService);
    }

    @Test
    void testListUsersSuccess() {
        // Given
        UserModel user1 = new UserModel("user1", "pass1", "USER");
        user1.setId(1L);
        UserModel user2 = new UserModel("user2", "pass2", "ADMIN");
        user2.setId(2L);
        
        when(userService.findAllUsers()).thenReturn(List.of(user1, user2));
        
        // When
        ResponseEntity<List<UserDTO>> response = adminController.listUsers();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(userService).findAllUsers();
    }

    @Test
    void testSetUserActiveSuccess() {
        // Given
        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        user.setActive(false);
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(userService.setUserActive(userId, true)).thenReturn(Optional.of(user));
        
        // When
        ResponseEntity<Void> response = adminController.setUserActive(userId, Map.of("active", true));
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).findById(userId);
        verify(userService).setUserActive(userId, true);
    }

    @Test
    void testSetUserActiveUserNotFound() {
        // Given
        Long userId = 999L;
        when(userService.findById(userId)).thenReturn(Optional.empty());
        
        // When
        ResponseEntity<Void> response = adminController.setUserActive(userId, Map.of("active", true));
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).findById(userId);
        verify(userService, never()).setUserActive(anyLong(), anyBoolean());
    }

    @Test
    void testSetUserActiveDeactivateUser() {
        // Given
        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        user.setActive(true);
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(userService.setUserActive(userId, false)).thenReturn(Optional.of(user));
        
        // When
        ResponseEntity<Void> response = adminController.setUserActive(userId, Map.of("active", false));
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).findById(userId);
        verify(userService).setUserActive(userId, false);
    }

    @Test
    void testDeleteUserSuccess() {
        // Given
        Long userId = 1L;
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(userId);
        
        when(userService.existsById(userId)).thenReturn(true);
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        
        // When
        ResponseEntity<Void> response = adminController.deleteUser(userId);
        
        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUser(userId);
    }

    @Test
    void testDeleteUserNotFound() {
        // Given
        Long userId = 999L;
        when(userService.existsById(userId)).thenReturn(false);
        
        // When
        ResponseEntity<Void> response = adminController.deleteUser(userId);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, never()).deleteUser(any());
    }

    @Test
    void testSystemStatsSuccess() {
        // Given
        when(userService.countUsers()).thenReturn(42L);
        
        // When
        ResponseEntity<Map<String, Object>> response = adminController.systemStats();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(42L, response.getBody().get("users"));
        verify(userService).countUsers();
    }
}
