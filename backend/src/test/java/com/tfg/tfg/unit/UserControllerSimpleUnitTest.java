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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tfg.tfg.controller.UserController;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;

@ExtendWith(MockitoExtension.class)
class UserControllerSimpleUnitTest {

    @Mock
    private UserModelRepository userRepository;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userRepository);
    }

    @Test
    void testListUsers() {
        UserModel user1 = new UserModel("user1", "password", "USER");
        user1.setId(1L);
        user1.setImage("/users/1/image");

        UserModel user2 = new UserModel("user2", "password", "USER");
        user2.setId(2L);
        user2.setImage("/users/2/image");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        ResponseEntity<List<UserDTO>> response = userController.listUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        UserDTO dto1 = response.getBody().get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("user1", dto1.getName());
        assertEquals("/users/1/image", dto1.getImage());
    }

    @Test
    void testListUsersEmpty() {
        when(userRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<UserDTO>> response = userController.listUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
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

        ResponseEntity<Object> response = userController.changeRole(1L, "ADMIN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(user.getRols().contains("ADMIN"));
        verify(userRepository).save(user);
    }

    @Test
    void testChangeRoleUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = userController.changeRole(1L, "ADMIN");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testDeleteUserSuccess() {
        UserModel user = new UserModel("testuser", "password", "USER");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<Object> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).delete(user);
    }

    @Test
    void testDeleteUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void testGetMyProfileWithUsers() {
        UserModel user = new UserModel("currentuser", "password", "USER");
        user.setId(1L);
        user.setImage("/users/1/image");
        user.setEmail("current@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user));

        ResponseEntity<UserDTO> response = userController.getMyProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("currentuser", response.getBody().getName());
        assertEquals("current@example.com", response.getBody().getEmail());
    }

    @Test
    void testGetMyProfileNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

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

        ResponseEntity<Object> response = userController.changeRole(1L, "ADMIN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, user.getRols().size());
        assertTrue(user.getRols().contains("ADMIN"));
        verify(userRepository).save(user);
    }
}