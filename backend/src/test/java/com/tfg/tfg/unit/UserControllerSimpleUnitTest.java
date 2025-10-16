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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tfg.tfg.controller.UserController;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.UserService;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.service.UserAvatarService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UserControllerSimpleUnitTest {

    @Mock
    private UserModelRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private RiotService riotService;

    @Mock
    private UserAvatarService userAvatarService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userRepository, userService, riotService, userAvatarService);
    }

    @Test
    void testListUsers() {
        UserModel user1 = new UserModel("user1", "password", "USER");
        user1.setId(1L);
        user1.setImage("/users/1/image");

        UserModel user2 = new UserModel("user2", "password", "USER");
        user2.setId(2L);
        user2.setImage("/users/2/image");

        Page<UserModel> usersPage = new PageImpl<>(List.of(user1, user2));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(usersPage);

        ResponseEntity<Page<UserDTO>> response = userController.listUsers(0, 20, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        
        UserDTO dto1 = response.getBody().getContent().get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("user1", dto1.getName());
        assertEquals("/users/1/image", dto1.getImage());
    }

    @Test
    void testListUsersEmpty() {
        Page<UserModel> emptyPage = new PageImpl<>(List.of());
        when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        ResponseEntity<Page<UserDTO>> response = userController.listUsers(0, 20, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());
    }

    @Test
    void testGetByNameFound() {
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(1L);
        user.setImage("/users/1/image");

        when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));

        ResponseEntity<UserDTO> response = userController.getByName("testuser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("testuser", response.getBody().getName());
        assertEquals("/users/1/image", response.getBody().getImage());
    }

    @Test
    void testGetByNameNotFound() {
        when(userRepository.findByName("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<UserDTO> response = userController.getByName("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testChangeRoleSuccess() {
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserModel.class))).thenReturn(user);

        ResponseEntity<UserDTO> response = userController.changeRole(1L, "ADMIN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(user.getRols().contains("ADMIN"));
        verify(userRepository).save(user);
    }

    @Test
    void testChangeRoleUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<UserDTO> response = userController.changeRole(1L, "ADMIN");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testDeleteUserSuccess() {
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userRepository).delete(user);
    }

    @Test
    void testDeleteUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void testGetMyProfileWithUsers() {
        UserModel user = new UserModel("currentuser", "password", "USER");
        user.setId(1L);
        user.setImage("/users/1/image");
        user.setEmail("current@example.com");

        when(userRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(user));

        ResponseEntity<UserDTO> response = userController.getMyProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("currentuser", response.getBody().getName());
        assertEquals("current@example.com", response.getBody().getEmail());
    }

    @Test
    void testGetMyProfileNoUsers() {
        when(userRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        ResponseEntity<UserDTO> response = userController.getMyProfile();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testChangeRoleMultipleRoles() {
        UserModel user = new UserModel("testuser", "password", "USER", "MODERATOR");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserModel.class))).thenReturn(user);

        ResponseEntity<UserDTO> response = userController.changeRole(1L, "ADMIN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, user.getRols().size());
        assertTrue(user.getRols().contains("ADMIN"));
        verify(userRepository).save(user);
    }

    // New tests for pagination and filters
    
    @Test
    void testListUsersWithRoleFilter() {
        UserModel admin = new UserModel("admin", "password", "ADMIN");
        admin.setId(1L);
        
        Page<UserModel> adminsPage = new PageImpl<>(List.of(admin));
        when(userRepository.findByRolsContaining(eq("ADMIN"), any(Pageable.class)))
            .thenReturn(adminsPage);

        ResponseEntity<Page<UserDTO>> response = userController.listUsers(0, 20, "ADMIN", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertTrue(response.getBody().getContent().get(0).getRoles().contains("ADMIN"));
    }

    @Test
    void testListUsersWithActiveFilter() {
        UserModel activeUser = new UserModel("activeuser", "password", "USER");
        activeUser.setId(1L);
        activeUser.setActive(true);
        
        Page<UserModel> activePage = new PageImpl<>(List.of(activeUser));
        when(userRepository.findByActive(eq(true), any(Pageable.class)))
            .thenReturn(activePage);

        ResponseEntity<Page<UserDTO>> response = userController.listUsers(0, 20, null, true, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertTrue(response.getBody().getContent().get(0).isActive());
    }

    @Test
    void testListUsersWithSearchFilter() {
        UserModel user = new UserModel("john", "password", "USER");
        user.setId(1L);
        user.setEmail("john@example.com");
        
        Page<UserModel> searchPage = new PageImpl<>(List.of(user));
        when(userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            eq("john"), eq("john"), any(Pageable.class)))
            .thenReturn(searchPage);

        ResponseEntity<Page<UserDTO>> response = userController.listUsers(0, 20, null, null, "john");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("john", response.getBody().getContent().get(0).getName());
    }

    @Test
    void testListUsersWithRoleAndActiveFilters() {
        UserModel admin = new UserModel("admin", "password", "ADMIN");
        admin.setId(1L);
        admin.setActive(true);
        
        Page<UserModel> filteredPage = new PageImpl<>(List.of(admin));
        when(userRepository.findByRolsContainingAndActive(eq("ADMIN"), eq(true), any(Pageable.class)))
            .thenReturn(filteredPage);

        ResponseEntity<Page<UserDTO>> response = userController.listUsers(0, 20, "ADMIN", true, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        UserDTO dto = response.getBody().getContent().get(0);
        assertTrue(dto.getRoles().contains("ADMIN"));
        assertTrue(dto.isActive());
    }

    @Test
    void testToggleActiveSuccess() {
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(1L);
        user.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserModel.class))).thenReturn(user);

        ResponseEntity<UserDTO> response = userController.toggleActive(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void testToggleActiveNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<UserDTO> response = userController.toggleActive(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUserSuccess() {
        UserModel user = new UserModel("oldname", "password", "USER");
        user.setId(1L);
        user.setEmail("old@example.com");

        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("newname");
        updateDTO.setEmail("new@example.com");
        updateDTO.setRoles(List.of("USER", "ADMIN"));
        updateDTO.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserModel.class))).thenReturn(user);

        ResponseEntity<UserDTO> response = userController.updateUser(1L, updateDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newname", user.getName());
        assertEquals("new@example.com", user.getEmail());
        assertTrue(user.getRols().contains("ADMIN"));
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUserNotFound() {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("newname");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<UserDTO> response = userController.updateUser(1L, updateDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUserSuccess() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("newuser");
        userDTO.setEmail("newuser@example.com");
        userDTO.setPassword("password123");
        userDTO.setRoles(List.of("USER"));

        UserModel createdUser = new UserModel("newuser", "encodedpassword", "USER");
        createdUser.setId(1L);
        createdUser.setEmail("newuser@example.com");

        when(userService.createUser(any(UserDTO.class))).thenReturn(createdUser);
        when(userRepository.findByName("newuser")).thenReturn(Optional.of(createdUser));

        ResponseEntity<UserDTO> response = userController.createUser(userDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newuser", response.getBody().getName());
        verify(userService).createUser(any(UserDTO.class));
    }

    @Test
    void testCreateUserAlreadyExists() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("existinguser");
        userDTO.setPassword("password123");

        doThrow(new IllegalStateException("User already exists"))
            .when(userService).createUser(any(UserDTO.class));

        ResponseEntity<UserDTO> response = userController.createUser(userDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(userService).createUser(any(UserDTO.class));
    }

    @Test
    void testCreateUserInvalidData() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(null);

        doThrow(new IllegalArgumentException("Invalid user payload"))
            .when(userService).createUser(any(UserDTO.class));

        ResponseEntity<UserDTO> response = userController.createUser(userDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService).createUser(any(UserDTO.class));
    }

    @Test
    void testGetUserByIdSuccess() {
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<UserDTO> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("testuser", response.getBody().getName());
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<UserDTO> response = userController.getUserById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testUploadAvatarSuccess() throws Exception {
        // Arrange
        String username = "testuser";
        String avatarUrl = "/api/v1/files/avatars/test-avatar.png";
        
        org.springframework.web.multipart.MultipartFile mockFile = mock(org.springframework.web.multipart.MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("avatar.png");
        when(mockFile.getSize()).thenReturn(1024L * 100); // 100KB
        when(mockFile.getContentType()).thenReturn("image/png");
        
        when(userAvatarService.uploadAvatar(eq(username), any())).thenReturn(avatarUrl);
        
        try (MockedStatic<org.springframework.security.core.context.SecurityContextHolder> mockedSecurityContextHolder = 
                mockStatic(org.springframework.security.core.context.SecurityContextHolder.class)) {
            org.springframework.security.core.context.SecurityContext securityContext = 
                mock(org.springframework.security.core.context.SecurityContext.class);
            org.springframework.security.core.Authentication authentication = 
                mock(org.springframework.security.core.Authentication.class);
            
            mockedSecurityContextHolder.when(org.springframework.security.core.context.SecurityContextHolder::getContext)
                .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            
            // Act
            ResponseEntity<?> response = userController.uploadAvatar(mockFile);
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> responseBody = (java.util.Map<String, Object>) response.getBody();
            assertEquals(true, responseBody.get("success"));
            assertEquals(avatarUrl, responseBody.get("avatarUrl"));
            
            verify(userAvatarService).uploadAvatar(eq(username), any());
        }
    }

    @Test
    void testUploadAvatarInvalidFileType() throws Exception {
        // Arrange
        String username = "testuser";
        
        org.springframework.web.multipart.MultipartFile mockFile = mock(org.springframework.web.multipart.MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("document.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        
        try (MockedStatic<org.springframework.security.core.context.SecurityContextHolder> mockedSecurityContextHolder = 
                mockStatic(org.springframework.security.core.context.SecurityContextHolder.class)) {
            org.springframework.security.core.context.SecurityContext securityContext = 
                mock(org.springframework.security.core.context.SecurityContext.class);
            org.springframework.security.core.Authentication authentication = 
                mock(org.springframework.security.core.Authentication.class);
            
            mockedSecurityContextHolder.when(org.springframework.security.core.context.SecurityContextHolder::getContext)
                .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            
            // Act
            ResponseEntity<?> response = userController.uploadAvatar(mockFile);
            
            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verify(userAvatarService, never()).uploadAvatar(any(), any());
        }
    }

    @Test
    void testUploadAvatarFileTooLarge() throws Exception {
        // Arrange
        String username = "testuser";
        
        org.springframework.web.multipart.MultipartFile mockFile = mock(org.springframework.web.multipart.MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("large.png");
        when(mockFile.getSize()).thenReturn(10L * 1024 * 1024); // 10MB
        when(mockFile.getContentType()).thenReturn("image/png");
        
        try (MockedStatic<org.springframework.security.core.context.SecurityContextHolder> mockedSecurityContextHolder = 
                mockStatic(org.springframework.security.core.context.SecurityContextHolder.class)) {
            org.springframework.security.core.context.SecurityContext securityContext = 
                mock(org.springframework.security.core.context.SecurityContext.class);
            org.springframework.security.core.Authentication authentication = 
                mock(org.springframework.security.core.Authentication.class);
            
            mockedSecurityContextHolder.when(org.springframework.security.core.context.SecurityContextHolder::getContext)
                .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            
            // Act
            ResponseEntity<?> response = userController.uploadAvatar(mockFile);
            
            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verify(userAvatarService, never()).uploadAvatar(any(), any());
        }
    }

    @Test
    void testDeleteAvatarSuccess() throws Exception {
        // Arrange
        String username = "testuser";
        
        doNothing().when(userAvatarService).deleteAvatar(username);
        
        try (MockedStatic<org.springframework.security.core.context.SecurityContextHolder> mockedSecurityContextHolder = 
                mockStatic(org.springframework.security.core.context.SecurityContextHolder.class)) {
            org.springframework.security.core.context.SecurityContext securityContext = 
                mock(org.springframework.security.core.context.SecurityContext.class);
            org.springframework.security.core.Authentication authentication = 
                mock(org.springframework.security.core.Authentication.class);
            
            mockedSecurityContextHolder.when(org.springframework.security.core.context.SecurityContextHolder::getContext)
                .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            
            // Act
            ResponseEntity<?> response = userController.deleteAvatar();
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> responseBody = (java.util.Map<String, Object>) response.getBody();
            assertEquals(true, responseBody.get("success"));
            
            verify(userAvatarService).deleteAvatar(username);
        }
    }
}