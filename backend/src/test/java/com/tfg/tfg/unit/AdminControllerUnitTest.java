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
import com.tfg.tfg.repository.UserModelRepository;

@ExtendWith(MockitoExtension.class)
class AdminControllerUnitTest {

    @Mock
    private UserModelRepository userRepository;
    
    private AdminController adminController;
    
    @BeforeEach
    void setUp() {
        adminController = new AdminController(userRepository);
    }

    @Test
    void testListUsers_Success() {
        // Given
        UserModel user1 = new UserModel("user1", "pass1", "USER");
        user1.setId(1L);
        UserModel user2 = new UserModel("user2", "pass2", "ADMIN");
        user2.setId(2L);
        
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        
        // When
        ResponseEntity<List<UserDTO>> response = adminController.listUsers();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(userRepository).findAll();
    }

    @Test
    void testSetUserActive_Success() {
        // Given
        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        user.setActive(false);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        ResponseEntity<Void> response = adminController.setUserActive(userId, Map.of("active", true));
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(argThat(u -> u.isActive()));
    }

    @Test
    void testSetUserActive_UserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When
        ResponseEntity<Void> response = adminController.setUserActive(userId, Map.of("active", true));
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testSetUserActive_DeactivateUser() {
        // Given
        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        user.setActive(true);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        ResponseEntity<Void> response = adminController.setUserActive(userId, Map.of("active", false));
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(argThat(u -> !u.isActive()));
    }

    @Test
    void testDeleteUser_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        
        // When
        ResponseEntity<Void> response = adminController.deleteUser(userId);
        
        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userRepository).deleteById(userId);
    }

    @Test
    void testDeleteUser_NotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);
        
        // When
        ResponseEntity<Void> response = adminController.deleteUser(userId);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void testSystemStats_Success() {
        // Given
        when(userRepository.count()).thenReturn(42L);
        
        // When
        ResponseEntity<Map<String, Object>> response = adminController.systemStats();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(42L, response.getBody().get("users"));
        verify(userRepository).count();
    }
}
