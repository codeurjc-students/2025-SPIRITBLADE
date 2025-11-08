package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.MatchRepository;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.DataInitializer;

@ExtendWith(MockitoExtension.class)
class DataInitializerUnitTest {

    @Mock
    private UserModelRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SummonerRepository summonerRepository;

    @Mock
    private MatchRepository matchRepository;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer(
            userRepository, 
            passwordEncoder
        );
    }

    @Test
    void testInitCreatesAdminAndUserWhenNotExist() {
        // Given - No users exist
        when(userRepository.findByName("admin")).thenReturn(Optional.empty());
        when(userRepository.findByName("user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataInitializer.init();

        // Then - Should create both users
        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository, times(2)).save(captor.capture());
        
        var savedUsers = captor.getAllValues();
        
        // Verify admin user
        UserModel admin = savedUsers.stream()
            .filter(u -> "admin".equals(u.getName()))
            .findFirst()
            .orElseThrow();
        assertEquals("admin", admin.getName());
        assertEquals("admin@example.com", admin.getEmail());
        assertTrue(admin.getRols().contains("ADMIN"));
        
        // Verify regular user
        UserModel user = savedUsers.stream()
            .filter(u -> "user".equals(u.getName()))
            .findFirst()
            .orElseThrow();
        assertEquals("user", user.getName());
        assertEquals("user@example.com", user.getEmail());
        assertTrue(user.getRols().contains("USER"));
        assertTrue(user.isActive());
    }

    @Test
    void testInitDoesNotCreateAdminWhenExists() {
        // Given - Admin already exists
        UserModel existingAdmin = new UserModel("admin", "encoded-password", "ADMIN");
        when(userRepository.findByName("admin")).thenReturn(Optional.of(existingAdmin));
        when(userRepository.findByName("user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataInitializer.init();

        // Then - Should only create user (not admin)
        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository, times(1)).save(captor.capture());
        
        UserModel savedUser = captor.getValue();
        assertEquals("user", savedUser.getName());
        assertNotEquals("admin", savedUser.getName());
    }

    @Test
    void testInitDoesNotCreateUserWhenExists() {
        // Given - User already exists
        UserModel existingUser = new UserModel("user", "encoded-password", "USER");
        when(userRepository.findByName("admin")).thenReturn(Optional.empty());
        when(userRepository.findByName("user")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataInitializer.init();

        // Then - Should only create admin (not user)
        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository, times(1)).save(captor.capture());
        
        UserModel savedAdmin = captor.getValue();
        assertEquals("admin", savedAdmin.getName());
        assertNotEquals("user", savedAdmin.getName());
    }

    @Test
    void testInitDoesNotCreateUsersWhenBothExist() {
        // Given - Both users already exist
        UserModel existingAdmin = new UserModel("admin", "encoded-password", "ADMIN");
        UserModel existingUser = new UserModel("user", "encoded-password", "USER");
        when(userRepository.findByName("admin")).thenReturn(Optional.of(existingAdmin));
        when(userRepository.findByName("user")).thenReturn(Optional.of(existingUser));

        // When
        dataInitializer.init();

        // Then - Should not create any users
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void testGenerateSecurePassword() throws Exception {
        // Given - Using reflection to test private method
        java.lang.reflect.Method method = DataInitializer.class.getDeclaredMethod(
            "generateSecurePassword", 
            String.class
        );
        method.setAccessible(true);

        // When
        String password = (String) method.invoke(dataInitializer, "testuser");

        // Then - Password should contain prefix and special char
        assertNotNull(password);
        assertTrue(password.startsWith("testuser"));
        assertTrue(password.contains("Secure"));
        assertTrue(password.endsWith("!"));
        assertTrue(password.length() > 10); // Should be reasonably long
    }

    @Test
    void testConstructor() {
        // Given & When - Constructor is called in setUp()
        
        // Then - DataInitializer should be initialized
        assertNotNull(dataInitializer);
    }
}
