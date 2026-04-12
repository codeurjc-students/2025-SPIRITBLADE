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

        when(userRepository.existsByName("admin")).thenReturn(false);
        when(userRepository.existsByName("user")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> {
            UserModel user = invocation.getArgument(0);
            if (user.getId() == null) {

                user.setId(user.getName().equals("admin") ? 1L : 2L);
            }
            return user;
        });

        dataInitializer.init();

        verify(userRepository, times(4)).save(any(UserModel.class));

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository, atLeast(2)).save(captor.capture());

        var savedUsers = captor.getAllValues();

        UserModel admin = savedUsers.stream()
                .filter(u -> "admin".equals(u.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals("admin", admin.getName());
        assertEquals("admin@example.com", admin.getEmail());
        assertTrue(admin.getRols().contains("ADMIN"));

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

        when(userRepository.existsByName("admin")).thenReturn(true);
        when(userRepository.existsByName("user")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> {
            UserModel user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(2L);
            }
            return user;
        });

        dataInitializer.init();

        verify(userRepository, times(2)).save(any(UserModel.class));

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository, atLeastOnce()).save(captor.capture());

        captor.getAllValues().forEach(savedUser -> {
            assertEquals("user", savedUser.getName());
            assertNotEquals("admin", savedUser.getName());
        });
    }

    @Test
    void testInitDoesNotCreateUserWhenExists() {

        when(userRepository.existsByName("admin")).thenReturn(false);
        when(userRepository.existsByName("user")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> {
            UserModel user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(1L);
            }
            return user;
        });

        dataInitializer.init();

        verify(userRepository, times(2)).save(any(UserModel.class));

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository, atLeastOnce()).save(captor.capture());

        captor.getAllValues().forEach(savedUser -> {
            assertEquals("admin", savedUser.getName());
            assertNotEquals("user", savedUser.getName());
        });
    }

    @Test
    void testInitDoesNotCreateUsersWhenBothExist() {

        when(userRepository.existsByName("admin")).thenReturn(true);
        when(userRepository.existsByName("user")).thenReturn(true);

        dataInitializer.init();

        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void testGenerateSecurePassword() throws Exception {

        java.lang.reflect.Method method = DataInitializer.class.getDeclaredMethod(
                "generateSecurePassword",
                String.class);
        method.setAccessible(true);

        String password = (String) method.invoke(dataInitializer, "testuser");

        assertNotNull(password);
        assertTrue(password.startsWith("testuser"));
        assertTrue(password.contains("Secure"));
        assertTrue(password.endsWith("!"));
        assertTrue(password.length() > 10);
    }

    @Test
    void testConstructor() {

        assertNotNull(dataInitializer);
    }

    @Test
    void testSimpleMultipartFileWithNullContent() throws Exception {

        byte[] nullContent = null;

        java.lang.reflect.Constructor<?> constructor = Class
                .forName("com.tfg.tfg.service.DataInitializer$SimpleMultipartFile")
                .getDeclaredConstructor(byte[].class, String.class, String.class);
        constructor.setAccessible(true);
        MultipartFile file = (MultipartFile) constructor
                .newInstance(nullContent, "test.txt", "text/plain");

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

        byte[] content = "Hello World".getBytes();

        java.lang.reflect.Constructor<?> constructor = Class
                .forName("com.tfg.tfg.service.DataInitializer$SimpleMultipartFile")
                .getDeclaredConstructor(byte[].class, String.class, String.class);
        constructor.setAccessible(true);
        MultipartFile file = (MultipartFile) constructor
                .newInstance(content, "hello.txt", "text/plain");

        assertEquals("hello.txt", file.getName());
        assertEquals("hello.txt", file.getOriginalFilename());
        assertEquals("text/plain", file.getContentType());
        assertFalse(file.isEmpty());
        assertEquals(11, file.getSize());
        assertArrayEquals(content, file.getBytes());

        try (java.io.InputStream is = file.getInputStream()) {
            byte[] readContent = is.readAllBytes();
            assertArrayEquals(content, readContent);
        }
    }

    @Test
    void testSimpleMultipartFileTransferTo() throws Exception {

        byte[] content = "Test content".getBytes();

        java.lang.reflect.Constructor<?> constructor = Class
                .forName("com.tfg.tfg.service.DataInitializer$SimpleMultipartFile")
                .getDeclaredConstructor(byte[].class, String.class, String.class);
        constructor.setAccessible(true);
        MultipartFile file = (MultipartFile) constructor
                .newInstance(content, "test.txt", "text/plain");

        java.io.File tempFile = java.io.File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();

        file.transferTo(tempFile);

        byte[] fileContent = java.nio.file.Files.readAllBytes(tempFile.toPath());
        assertArrayEquals(content, fileContent);
    }

    @Test
    void testDeduplicateSystemUserremovesExtraRows() {

        UserModel dup1 = new UserModel("admin", "pw", "ADMIN");
        dup1.setId(1L);
        UserModel dup2 = new UserModel("admin", "pw", "ADMIN");
        dup2.setId(2L);

        when(userRepository.findAllByName("admin")).thenReturn(java.util.List.of(dup1, dup2));
        when(userRepository.findAllByName("user")).thenReturn(java.util.List.of());
        when(userRepository.existsByName("admin")).thenReturn(true);
        when(userRepository.existsByName("user")).thenReturn(true);

        dataInitializer.init();

        verify(userRepository).delete(dup2);

        verify(userRepository, never()).delete(dup1);
    }

    @Test
    void testResolvePasswordproductionModenoEnvVar() {

        System.setProperty("spring.profiles.active", "production");
        try {
            when(userRepository.findAllByName(anyString())).thenReturn(java.util.List.of());
            when(userRepository.existsByName("admin")).thenReturn(true);
            when(userRepository.existsByName("user")).thenReturn(true);

            dataInitializer.init();

            verify(userRepository, never()).save(any());
        } finally {
            System.clearProperty("spring.profiles.active");
        }
    }
}
