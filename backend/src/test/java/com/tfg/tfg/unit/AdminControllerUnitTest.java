package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());
        Page<UserModel> page = new PageImpl<>(List.of(user1, user2), pageable, 2);
        
        when(userService.findAll(pageable)).thenReturn(page);
        
        // When
        ResponseEntity<Page<UserDTO>> response = adminController.listUsers(0, 20, null, null, null);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        verify(userService).findAll(pageable);
    }

    @Test
    void testToggleUserActiveSuccess() {
        // Given
        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        user.setActive(false);
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.toggleUserActive(userId)).thenReturn(Optional.of(user));
        
        // When
        ResponseEntity<UserDTO> response = adminController.toggleUserActive(userId);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService).getUserById(userId);
        verify(userService).toggleUserActive(userId);
    }

    @Test
    void testToggleUserActiveUserNotFound() {
        // Given
        Long userId = 999L;
        when(userService.getUserById(userId)).thenThrow(new com.tfg.tfg.exception.UserNotFoundException("User not found"));

        // Act & Assert: controller delegates exception handling to GlobalExceptionHandler; at unit level the exception will be thrown
        assertThrows(com.tfg.tfg.exception.UserNotFoundException.class, () ->
            adminController.toggleUserActive(userId));
        verify(userService).getUserById(userId);
        verify(userService, never()).toggleUserActive(anyLong());
    }

    @Test
    void testToggleUserActiveDeactivateUser() {
        // Given
        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        user.setActive(true);
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.toggleUserActive(userId)).thenReturn(Optional.of(user));
        
        // When
        ResponseEntity<UserDTO> response = adminController.toggleUserActive(userId);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService).getUserById(userId);
        verify(userService).toggleUserActive(userId);
    }

    @Test
    void testDeleteUserSuccess() {
        // Given
        Long userId = 1L;
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(userId);
        
        when(userService.getUserById(userId)).thenReturn(user);
        doNothing().when(userService).deleteUserOrThrow(userId);
        
        // When
        ResponseEntity<Void> response = adminController.deleteUser(userId);
        
        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUserOrThrow(userId);
    }

    @Test
    void testDeleteUserNotFound() {
        // Given
        Long userId = 999L;
        when(userService.getUserById(userId)).thenThrow(new com.tfg.tfg.exception.UserNotFoundException("User not found"));

        // Act & Assert
        assertThrows(com.tfg.tfg.exception.UserNotFoundException.class, () -> adminController.deleteUser(userId));
        verify(userService, never()).deleteUserOrThrow(anyLong());
    }

    @Test
    void testUpdateUserSuccess() {
        // Given
        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        
        UserDTO userDTO = new UserDTO();
        userDTO.setName("testuser"); // Same name
        userDTO.setEmail("newemail@test.com");
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.updateUserOrThrow(userId, userDTO)).thenReturn(user);
        
        // When
        ResponseEntity<UserDTO> response = adminController.updateUser(userId, userDTO);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService).getUserById(userId);
        verify(userService).updateUserOrThrow(userId, userDTO);
    }

    @Test
    void testUpdateUserUsernameChangeForbidden() {
        // Given
        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        
        UserDTO userDTO = new UserDTO();
        userDTO.setName("newusername"); // Different name
        
        when(userService.getUserById(userId)).thenReturn(user);
        
        // When
        ResponseEntity<UserDTO> response = adminController.updateUser(userId, userDTO);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService).getUserById(userId);
        verify(userService, never()).updateUserOrThrow(anyLong(), any());
    }
}
