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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserModelRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void testCreateUserSuccess() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("newuser");
        userDTO.setEmail("newuser@example.com");
        userDTO.setPassword("password123");
        userDTO.setRoles(List.of("USER"));

        when(userRepository.findByName("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserModel result = userService.createUser(userDTO);

        assertNotNull(result);
        assertEquals("newuser", result.getName());
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getEncodedPassword());
        assertTrue(result.getRols().contains("USER"));
        assertTrue(result.isActive());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testCreateUserWithDefaultRole() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("newuser");
        userDTO.setEmail("newuser@example.com");
        userDTO.setPassword("password123");
        userDTO.setRoles(null); // No roles provided

        when(userRepository.findByName("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserModel result = userService.createUser(userDTO);

        assertNotNull(result);
        assertTrue(result.getRols().contains("USER"));
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testCreateUserAlreadyExists() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("existinguser");
        userDTO.setPassword("password123");

        UserModel existingUser = new UserModel("existinguser", "password", "USER");
        when(userRepository.findByName("existinguser")).thenReturn(Optional.of(existingUser));

        assertThrows(com.tfg.tfg.exception.UserAlreadyExistsException.class, () -> userService.createUser(userDTO));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUserWithNullName() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(null);
        userDTO.setPassword("password123");

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUserWithNullPassword() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("newuser");
        userDTO.setPassword(null);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUserWithNullDTO() {
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUserSuccess() {
        UserModel existingUser = new UserModel("oldname", "oldpassword", "USER");
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");

        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("newname");
        updateDTO.setEmail("new@example.com");
        updateDTO.setPassword("newpassword");
        updateDTO.setRoles(List.of("ADMIN"));
        updateDTO.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<UserModel> result = userService.updateUser(1L, updateDTO);

        assertTrue(result.isPresent());
        assertEquals("newname", result.get().getName());
        assertEquals("new@example.com", result.get().getEmail());
        assertEquals("encodedNewPassword", result.get().getEncodedPassword());
        assertTrue(result.get().getRols().contains("ADMIN"));
        assertFalse(result.get().isActive());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testUpdateUserPartialUpdate() {
        UserModel existingUser = new UserModel("oldname", "oldpassword", "USER");
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");

        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("newname");
        // Email, password, and roles not provided

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<UserModel> result = userService.updateUser(1L, updateDTO);

        assertTrue(result.isPresent());
        assertEquals("newname", result.get().getName());
        assertEquals("old@example.com", result.get().getEmail()); // Unchanged
        assertEquals("oldpassword", result.get().getEncodedPassword()); // Unchanged
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testUpdateUserNotFound() {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("newname");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.updateUser(1L, updateDTO);

        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUserWithEmptyPassword() {
        UserModel existingUser = new UserModel("oldname", "oldpassword", "USER");
        existingUser.setId(1L);

        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("newname");
        updateDTO.setPassword(""); // Empty password should not update

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<UserModel> result = userService.updateUser(1L, updateDTO);

        assertTrue(result.isPresent());
        assertEquals("oldpassword", result.get().getEncodedPassword()); // Password unchanged
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testDeleteUserSuccess() {
        UserModel existingUser = new UserModel("testuser", "password", "USER");
        existingUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userRepository).delete(existingUser);
    }

    @Test
    void testDeleteUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = userService.deleteUser(1L);

        assertFalse(result);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void testToggleUserActiveSuccess() {
        UserModel existingUser = new UserModel("testuser", "password", "USER");
        existingUser.setId(1L);
        existingUser.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<UserModel> result = userService.toggleUserActive(1L);

        assertTrue(result.isPresent());
        assertFalse(result.get().isActive());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testToggleUserActiveFromInactiveToActive() {
        UserModel existingUser = new UserModel("testuser", "password", "USER");
        existingUser.setId(1L);
        existingUser.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<UserModel> result = userService.toggleUserActive(1L);

        assertTrue(result.isPresent());
        assertTrue(result.get().isActive());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testToggleUserActiveNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.toggleUserActive(1L);

        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testFindByNameSuccess() {
        UserModel user = new UserModel("testuser", "password", "USER");
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));

        Optional<UserModel> result = userService.findByName("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getName());
        verify(userRepository).findByName("testuser");
    }

    @Test
    void testFindByNameNotFound() {
        when(userRepository.findByName("nonexistent")).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.findByName("nonexistent");

        assertFalse(result.isPresent());
        verify(userRepository).findByName("nonexistent");
    }
}
