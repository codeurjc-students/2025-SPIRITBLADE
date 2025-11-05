package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
class UserServiceSimpleUnitTest {

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
    void testFindByNameExists() {
        UserModel user = new UserModel("testuser", "password", "USER");
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));

        Optional<UserModel> result = userService.findByName("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getName());
        verify(userRepository).findByName("testuser");
    }

    @Test
    void testFindByNameNotExists() {
        when(userRepository.findByName("nonexistent")).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.findByName("nonexistent");

        assertFalse(result.isPresent());
        verify(userRepository).findByName("nonexistent");
    }

    @Test
    void testCreateUserSuccess() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("newuser");
        userDTO.setEmail("new@example.com");
        userDTO.setPassword("plainpassword");

        when(userRepository.findByName("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainpassword")).thenReturn("encodedpassword");

        userService.createUser(userDTO);

        verify(userRepository).findByName("newuser");
        verify(passwordEncoder).encode("plainpassword");
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testCreateUserAlreadyExists() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("existinguser");
        userDTO.setPassword("password");

        UserModel existingUser = new UserModel("existinguser", "password", "USER");
        when(userRepository.findByName("existinguser")).thenReturn(Optional.of(existingUser));

        assertThrows(com.tfg.tfg.exception.UserAlreadyExistsException.class, () -> userService.createUser(userDTO));
        verify(userRepository).findByName("existinguser");
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void testCreateUserNullPayload() {
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(null));
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void testCreateUserNullName() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(null);
        userDTO.setPassword("password");

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void testCreateUserNullPassword() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("testuser");
        userDTO.setPassword(null);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void testCreateUserWithoutEmail() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("userwithoutemail");
        userDTO.setPassword("password");
        userDTO.setEmail(null);

        when(userRepository.findByName("userwithoutemail")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedpassword");

        userService.createUser(userDTO);

        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testPasswordEncoding() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("testuser");
        userDTO.setPassword("plaintext");

        when(userRepository.findByName("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plaintext")).thenReturn("encrypted_plaintext");

        userService.createUser(userDTO);

        verify(passwordEncoder).encode("plaintext");
    }

    @Test
    void testUserRoleAssignment() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("testuser");
        userDTO.setPassword("password");

        when(userRepository.findByName("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedpassword");

        userService.createUser(userDTO);

        // Verify that save is called with a user that has USER role
        verify(userRepository).save(argThat(user -> 
            user.getRols().contains("USER") && user.getRols().size() == 1
        ));
    }
}