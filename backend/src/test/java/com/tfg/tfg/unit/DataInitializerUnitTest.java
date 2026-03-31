package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.tfg.tfg.service.interfaces.IDataDragonService;
import com.tfg.tfg.service.interfaces.IStorageService;

@ExtendWith(MockitoExtension.class)
class DataInitializerUnitTest {

    @Mock
    private UserModelRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SummonerRepository summonerRepository;

    @Mock
    private IStorageService storageService;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private IDataDragonService dataDragonService;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer(
                userRepository,
                passwordEncoder,
                storageService,
                dataDragonService);
    }

    @Test
    void testInitCreatesAdminAndUserWhenNotExist() {
        // Given - No users exist
        when(userRepository.existsByName("admin")).thenReturn(false);
        when(userRepository.existsByName("user")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        // Mock save to return user with ID set (simulating DB behavior)
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> {
            UserModel user = invocation.getArgument(0);
            if (user.getId() == null) {
                // Simulate first save setting the ID
                user.setId(user.getName().equals("admin") ? 1L : 2L);
            }
            return user;
        });

        // When
        dataInitializer.init();

        // Then - Should save both users twice each (double-save pattern for image path)
        verify(userRepository, times(4)).save(any(UserModel.class));

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository, atLeast(2)).save(captor.capture());

        var savedUsers = captor.getAllValues();

        // Verify admin user (final save has image path)
        UserModel admin = savedUsers.stream()
                .filter(u -> "admin".equals(u.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals("admin", admin.getName());
        assertEquals("admin@example.com", admin.getEmail());
        assertTrue(admin.getRols().contains("ADMIN"));

        // Verify regular user (final save has image path)
        UserModel user = savedUsers.stream()
                .filter(u -> "user".equals(u.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals("user", user.getName());
        assertEquals("user@example.com", user.getEmail());
        assertTrue(user.getRols().contains("USER"));
        assertTrue(user.isActive());

        verify(dataDragonService).updateChampionDatabase();
    }

    @Test
    void testInitDoesNotCreateAdminWhenExists() {
        // Given - Admin already exists
        when(userRepository.existsByName("admin")).thenReturn(true);
        when(userRepository.existsByName("user")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        // Mock save to return user with ID set (simulating DB behavior)
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> {
            UserModel user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(2L); // User ID
            }
            return user;
        });

        // When
        dataInitializer.init();

        // Then - Should only create user (not admin), with double-save
        verify(userRepository, times(2)).save(any(UserModel.class));

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository, atLeastOnce()).save(captor.capture());

        // All saves should be for "user", not "admin"
        captor.getAllValues().forEach(savedUser -> {
            assertEquals("user", savedUser.getName());
            assertNotEquals("admin", savedUser.getName());
        });
    }

    @Test
    void testInitDoesNotCreateUserWhenExists() {
        // Given - User already exists
        when(userRepository.existsByName("admin")).thenReturn(false);
        when(userRepository.existsByName("user")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        // Mock save to return user with ID set (simulating DB behavior)
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> {
            UserModel user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(1L); // Admin ID
            }
            return user;
        });

        // When
        dataInitializer.init();

        // Then - Should only create admin (not user), with double-save
        verify(userRepository, times(2)).save(any(UserModel.class));

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository, atLeastOnce()).save(captor.capture());

        // All saves should be for "admin", not "user"
        captor.getAllValues().forEach(savedUser -> {
            assertEquals("admin", savedUser.getName());
            assertNotEquals("user", savedUser.getName());
        });
    }

    @Test
    void testInitDoesNotCreateUsersWhenBothExist() {
        // Given - Both users already exist
        when(userRepository.existsByName("admin")).thenReturn(true);
        when(userRepository.existsByName("user")).thenReturn(true);

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
                String.class);
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

    @Test
    void testSimpleMultipartFileWithNullContent() throws Exception {
        // Given
        byte[] nullContent = null;

        // When - Use reflection to create SimpleMultipartFile
        java.lang.reflect.Constructor<?> constructor = Class
                .forName("com.tfg.tfg.service.DataInitializer$SimpleMultipartFile")
                .getDeclaredConstructor(byte[].class, String.class, String.class);
        constructor.setAccessible(true);
        org.springframework.web.multipart.MultipartFile file = (org.springframework.web.multipart.MultipartFile) constructor
                .newInstance(nullContent, "test.txt", "text/plain");

        // Then
        assertEquals("test.txt", file.getName());
        assertEquals("test.txt", file.getOriginalFilename());
        assertEquals("text/plain", file.getContentType());
        assertTrue(file.isEmpty());
        assertEquals(0, file.getSize());
        assertNotNull(file.getBytes());
        assertEquals(0, file.getBytes().length);
    }

    @Test
    void testSimpleMultipartFileWithContent() throws Exception {
        // Given
        byte[] content = "Hello World".getBytes();

        // When - Use reflection to create SimpleMultipartFile
        java.lang.reflect.Constructor<?> constructor = Class
                .forName("com.tfg.tfg.service.DataInitializer$SimpleMultipartFile")
                .getDeclaredConstructor(byte[].class, String.class, String.class);
        constructor.setAccessible(true);
        org.springframework.web.multipart.MultipartFile file = (org.springframework.web.multipart.MultipartFile) constructor
                .newInstance(content, "hello.txt", "text/plain");

        // Then
        assertEquals("hello.txt", file.getName());
        assertEquals("hello.txt", file.getOriginalFilename());
        assertEquals("text/plain", file.getContentType());
        assertFalse(file.isEmpty());
        assertEquals(11, file.getSize());
        assertArrayEquals(content, file.getBytes());

        // Test InputStream
        try (java.io.InputStream is = file.getInputStream()) {
            byte[] readContent = is.readAllBytes();
            assertArrayEquals(content, readContent);
        }
    }

    @Test
    void testSimpleMultipartFileTransferTo() throws Exception {
        // Given
        byte[] content = "Test content".getBytes();

        // When - Use reflection to create SimpleMultipartFile
        java.lang.reflect.Constructor<?> constructor = Class
                .forName("com.tfg.tfg.service.DataInitializer$SimpleMultipartFile")
                .getDeclaredConstructor(byte[].class, String.class, String.class);
        constructor.setAccessible(true);
        org.springframework.web.multipart.MultipartFile file = (org.springframework.web.multipart.MultipartFile) constructor
                .newInstance(content, "test.txt", "text/plain");

        java.io.File tempFile = java.io.File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();

        // When
        file.transferTo(tempFile);

        // Then
        byte[] fileContent = java.nio.file.Files.readAllBytes(tempFile.toPath());
        assertArrayEquals(content, fileContent);
    }

    @Test
    void testDeduplicateSystemUser_removesExtraRows() {
        // Given - two duplicates for 'admin' with different IDs
        UserModel dup1 = new UserModel("admin", "pw", "ADMIN");
        dup1.setId(1L);
        UserModel dup2 = new UserModel("admin", "pw", "ADMIN");
        dup2.setId(2L);

        when(userRepository.findAllByName("admin")).thenReturn(java.util.List.of(dup1, dup2));
        when(userRepository.findAllByName("user")).thenReturn(java.util.List.of());
        when(userRepository.existsByName("admin")).thenReturn(true);
        when(userRepository.existsByName("user")).thenReturn(true);

        // When
        dataInitializer.init();

        // Then - the duplicate with higher ID should be deleted
        verify(userRepository).delete(dup2);
        // dup1 (lowest ID) should never be deleted
        verify(userRepository, never()).delete(dup1);
    }

    @Test
    void testResolvePassword_productionMode_noEnvVar() {
        // Call init() under production mode via system property
        System.setProperty("spring.profiles.active", "production");
        try {
            when(userRepository.findAllByName(anyString())).thenReturn(java.util.List.of());
            when(userRepository.existsByName("admin")).thenReturn(true);
            when(userRepository.existsByName("user")).thenReturn(true);

            // When - admin and user already exist, just trigger resolvePassword in production path
            dataInitializer.init();

            // Then - no users created, but init completed without error
            verify(userRepository, never()).save(any());
        } finally {
            System.clearProperty("spring.profiles.active");
        }
    }
}
