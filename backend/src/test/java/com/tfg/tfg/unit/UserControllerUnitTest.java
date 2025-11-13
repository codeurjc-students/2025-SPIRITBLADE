package com.tfg.tfg.unit;

import com.tfg.tfg.controller.UserController;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.service.UserAvatarService;
import com.tfg.tfg.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private RiotService riotService;

    @Mock
    private UserAvatarService userAvatarService;

    @Mock
    private SecurityContext securityContext;

    private UserController controller;

    private UserModel testUser;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService, riotService, userAvatarService);

        testUser = new UserModel();
        testUser.setId(1L);
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setActive(true);
        testUser.setRols(List.of("ROLE_USER"));

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testListUsersWithoutFilters() {
        // Arrange
        Page<UserModel> page = new PageImpl<>(List.of(testUser));
        when(userService.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<UserDTO>> response = controller.listUsers(0, 20, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(userService).findAll(any(Pageable.class));
    }

    @Test
    void testListUsersWithSearchFilter() {
        // Arrange
        Page<UserModel> page = new PageImpl<>(List.of(testUser));
        when(userService.findBySearch(anyString(), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<UserDTO>> response = controller.listUsers(0, 20, null, null, "test");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService).findBySearch(eq("test"), any(Pageable.class));
    }

    @Test
    void testListUsersWithRoleAndActiveFilters() {
        // Arrange
        Page<UserModel> page = new PageImpl<>(List.of(testUser));
        when(userService.findByRoleAndActive(anyString(), anyBoolean(), any(Pageable.class)))
            .thenReturn(page);

        // Act
        ResponseEntity<Page<UserDTO>> response = controller.listUsers(0, 20, "ROLE_USER", true, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).findByRoleAndActive(eq("ROLE_USER"), eq(true), any(Pageable.class));
    }

    @Test
    void testListUsersWithRoleFilter() {
        // Arrange
        Page<UserModel> page = new PageImpl<>(List.of(testUser));
        when(userService.findByRole(anyString(), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<UserDTO>> response = controller.listUsers(0, 20, "ROLE_ADMIN", null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).findByRole(eq("ROLE_ADMIN"), any(Pageable.class));
    }

    @Test
    void testListUsersWithActiveFilter() {
        // Arrange
        Page<UserModel> page = new PageImpl<>(List.of(testUser));
        when(userService.findByActive(anyBoolean(), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<UserDTO>> response = controller.listUsers(0, 20, null, false, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).findByActive(eq(false), any(Pageable.class));
    }

    @Test
    void testGetUserByIdFound() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(testUser);

        // Act
        ResponseEntity<UserDTO> response = controller.getUserById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getName());
        verify(userService).getUserById(1L);
    }

    @Test
    void testGetUserByIdNotFound() {
        // Arrange
        when(userService.getUserById(999L)).thenThrow(new com.tfg.tfg.exception.UserNotFoundException("User not found"));

        // Act & Assert
        assertThrows(com.tfg.tfg.exception.UserNotFoundException.class, () -> controller.getUserById(999L));
        verify(userService).getUserById(999L);
    }

    @Test
    void testGetByNameFound() {
        // Arrange
        when(userService.getUserByName("testuser")).thenReturn(testUser);

        // Act
        ResponseEntity<UserDTO> response = controller.getByName("testuser");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getName());
        verify(userService).getUserByName("testuser");
    }

    @Test
    void testGetByNameNotFound() {
        // Arrange
        when(userService.getUserByName("nonexistent")).thenThrow(new com.tfg.tfg.exception.UserNotFoundException("User not found"));

        // Act & Assert
        assertThrows(com.tfg.tfg.exception.UserNotFoundException.class, () -> controller.getByName("nonexistent"));
        verify(userService).getUserByName("nonexistent");
    }

    @Test
    void testGetMyProfileUserFound() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<UserDTO> response = controller.getMyProfile();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getName());
        verify(userService).findByName("testuser");
    }

    @Test
    void testGetMyProfileUserNotFound() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "nonexistent", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("nonexistent")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserDTO> response = controller.getMyProfile();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUploadAvatarSuccess() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "avatar.png", 
            "image/png", 
            "test image content".getBytes()
        );
        
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userAvatarService.uploadAvatar("testuser", file)).thenReturn("http://avatar.url");

        // Act
        ResponseEntity<Map<String, Object>> response = controller.uploadAvatar(file);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("http://avatar.url", response.getBody().get("avatarUrl"));
        verify(userAvatarService).uploadAvatar("testuser", file);
    }

    @Test
    void testUploadAvatarUserNotFound() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "avatar.png", 
            "image/png", 
            "test image content".getBytes()
        );
        
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "nonexistent", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userAvatarService.uploadAvatar(eq("nonexistent"), any())).thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            controller.uploadAvatar(file);
        });
        
        verify(userAvatarService).uploadAvatar(eq("nonexistent"), any());
    }
}
