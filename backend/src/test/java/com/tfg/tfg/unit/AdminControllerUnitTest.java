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

        UserModel user1 = new UserModel("user1", "pass1", "USER");
        user1.setId(1L);
        UserModel user2 = new UserModel("user2", "pass2", "ADMIN");
        user2.setId(2L);
        
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());
        Page<UserModel> page = new PageImpl<>(List.of(user1, user2), pageable, 2);
        
        when(userService.findAll(pageable)).thenReturn(page);

        ResponseEntity<Page<UserDTO>> response = adminController.listUsers(0, 20, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        verify(userService).findAll(pageable);
    }

    @Test
    void testToggleUserActiveSuccess() {

        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        user.setActive(false);
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.toggleUserActive(userId)).thenReturn(Optional.of(user));

        ResponseEntity<UserDTO> response = adminController.toggleUserActive(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService).getUserById(userId);
        verify(userService).toggleUserActive(userId);
    }

    @Test
    void testToggleUserActiveUserNotFound() {

        Long userId = 999L;
        when(userService.getUserById(userId)).thenThrow(new com.tfg.tfg.exception.UserNotFoundException("User not found"));

        assertThrows(com.tfg.tfg.exception.UserNotFoundException.class, () ->
            adminController.toggleUserActive(userId));
        verify(userService).getUserById(userId);
        verify(userService, never()).toggleUserActive(anyLong());
    }

    @Test
    void testToggleUserActiveDeactivateUser() {

        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        user.setActive(true);
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.toggleUserActive(userId)).thenReturn(Optional.of(user));

        ResponseEntity<UserDTO> response = adminController.toggleUserActive(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService).getUserById(userId);
        verify(userService).toggleUserActive(userId);
    }

    @Test
    void testDeleteUserSuccess() {

        Long userId = 1L;
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(userId);
        
        when(userService.getUserById(userId)).thenReturn(user);
        doNothing().when(userService).deleteUserOrThrow(userId);

        ResponseEntity<Void> response = adminController.deleteUser(userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUserOrThrow(userId);
    }

    @Test
    void testDeleteUserNotFound() {

        Long userId = 999L;
        when(userService.getUserById(userId)).thenThrow(new com.tfg.tfg.exception.UserNotFoundException("User not found"));

        assertThrows(com.tfg.tfg.exception.UserNotFoundException.class, () -> adminController.deleteUser(userId));
        verify(userService, never()).deleteUserOrThrow(anyLong());
    }

    @Test
    void testUpdateUserSuccess() {

        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        
        UserDTO userDTO = new UserDTO();
        userDTO.setName("testuser");
        userDTO.setEmail("newemail@test.com");
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.updateUserOrThrow(userId, userDTO)).thenReturn(user);

        ResponseEntity<UserDTO> response = adminController.updateUser(userId, userDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService).getUserById(userId);
        verify(userService).updateUserOrThrow(userId, userDTO);
    }

    @Test
    void testUpdateUserUsernameChangeForbidden() {

        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        
        UserDTO userDTO = new UserDTO();
        userDTO.setName("newusername");
        
        when(userService.getUserById(userId)).thenReturn(user);

        ResponseEntity<UserDTO> response = adminController.updateUser(userId, userDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService).getUserById(userId);
        verify(userService, never()).updateUserOrThrow(anyLong(), any());
    }

    @Test
    void testUpdateUseradminUserForbidden() {
        Long userId = 2L;
        UserModel adminUser = new UserModel("otheradmin", "pass", "ADMIN");
        adminUser.setId(userId);
        when(userService.getUserById(userId)).thenReturn(adminUser);

        ResponseEntity<UserDTO> response = adminController.updateUser(userId, new UserDTO());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userService, never()).updateUserOrThrow(anyLong(), any());
    }

    @Test
    void testToggleUserActiveadminUserForbidden() {
        Long userId = 2L;
        UserModel adminUser = new UserModel("otheradmin", "pass", "ADMIN");
        adminUser.setId(userId);
        when(userService.getUserById(userId)).thenReturn(adminUser);

        ResponseEntity<UserDTO> response = adminController.toggleUserActive(userId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userService, never()).toggleUserActive(anyLong());
    }

    @Test
    void testDeleteUseradminUserForbidden() {
        Long userId = 2L;
        UserModel adminUser = new UserModel("otheradmin", "pass", "ADMIN");
        adminUser.setId(userId);
        when(userService.getUserById(userId)).thenReturn(adminUser);

        ResponseEntity<Void> response = adminController.deleteUser(userId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userService, never()).deleteUserOrThrow(anyLong());
    }

    @Test
    void testToggleUserActivereturnsNotFoundwhenServiceReturnsEmpty() {
        Long userId = 1L;
        UserModel user = new UserModel("testuser", "pass", "USER");
        user.setId(userId);
        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.toggleUserActive(userId)).thenReturn(Optional.empty());

        ResponseEntity<UserDTO> response = adminController.toggleUserActive(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
