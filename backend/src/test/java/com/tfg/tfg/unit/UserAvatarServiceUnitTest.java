package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.tfg.tfg.exception.InvalidFileException;
import com.tfg.tfg.exception.StorageException;
import com.tfg.tfg.exception.UserNotFoundException;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.UserAvatarService;
import com.tfg.tfg.service.storage.MinioStorageService;

@ExtendWith(MockitoExtension.class)
class UserAvatarServiceUnitTest {

    @Mock
    private MinioStorageService storageService;
    
    @Mock
    private UserModelRepository userRepository;
    
    @Mock
    private MultipartFile multipartFile;
    
    private UserAvatarService avatarService;
    
    @BeforeEach
    void setUp() {
        avatarService = new UserAvatarService(storageService, userRepository);
    }

    @Test
    void testUploadAvatarSuccess() throws IOException {
        // Given
        String username = "testuser";
        // PNG magic bytes
        byte[] content = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        
        UserModel user = new UserModel(username, "pass", "USER");
        when(userRepository.findByName(username)).thenReturn(Optional.of(user));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("avatar.png");
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(storageService.store(multipartFile, "avatars")).thenReturn("avatars/uuid.png");
        when(storageService.getPublicUrl("avatars/uuid.png")).thenReturn("/api/v1/files/avatars/uuid.png");
        when(userRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        String result = avatarService.uploadAvatar(username, multipartFile);
        
        // Then
        assertNotNull(result);
        assertEquals("/api/v1/files/avatars/uuid.png", result);
        verify(storageService).store(multipartFile, "avatars");
        verify(userRepository).save(argThat(u -> 
            u.getAvatarUrl() != null && u.getImage() != null
        ));
    }

    @Test
    void testUploadAvatarReplacesOldAvatar() throws IOException {
        // Given
        String username = "testuser";
        byte[] content = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        
        UserModel user = new UserModel(username, "pass", "USER");
        user.setAvatarUrl("/api/v1/files/avatars/old-uuid.png");
        
        when(userRepository.findByName(username)).thenReturn(Optional.of(user));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("avatar.png");
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(storageService.store(multipartFile, "avatars")).thenReturn("avatars/new-uuid.png");
        when(storageService.getPublicUrl("avatars/new-uuid.png")).thenReturn("/api/v1/files/avatars/new-uuid.png");
        when(userRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        String result = avatarService.uploadAvatar(username, multipartFile);
        
        // Then
        assertNotNull(result);
        verify(storageService).delete("avatars/old-uuid.png");
        verify(storageService).store(multipartFile, "avatars");
    }

    @Test
    void testUploadAvatarUserNotFound() throws IOException {
        // Given
        String username = "nonexistent";
        byte[] content = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("avatar.png");
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(userRepository.findByName(username)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            avatarService.uploadAvatar(username, multipartFile);
        });
    }

    @Test
    void testUploadAvatarEmptyFile() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(true);
        
        // When & Then
        assertThrows(InvalidFileException.class, () -> {
            avatarService.uploadAvatar("user", multipartFile);
        });
    }

    @Test
    void testUploadAvatarFileTooLarge() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(6L * 1024 * 1024); // 6MB
        
        // When & Then
        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> {
            avatarService.uploadAvatar("user", multipartFile);
        });
        assertTrue(ex.getMessage().contains("5MB"));
    }

    @Test
    void testUploadAvatarStorageError() throws IOException {
        // Given
        String username = "testuser";
        byte[] content = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        
        UserModel user = new UserModel(username, "pass", "USER");
        when(userRepository.findByName(username)).thenReturn(Optional.of(user));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("avatar.png");
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(storageService.store(multipartFile, "avatars")).thenThrow(new IOException("Storage full"));
        
        // When & Then
        assertThrows(StorageException.class, () -> {
            avatarService.uploadAvatar(username, multipartFile);
        });
    }

    @Test
    void testDeleteAvatarSuccess() throws IOException {
        // Given
        String username = "testuser";
        UserModel user = new UserModel(username, "pass", "USER");
        user.setAvatarUrl("/api/v1/files/avatars/uuid.png");
        user.setImage("/api/v1/files/avatars/uuid.png");
        
        when(userRepository.findByName(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        avatarService.deleteAvatar(username);
        
        // Then
        verify(storageService).delete("avatars/uuid.png");
        verify(userRepository).save(argThat(u -> 
            u.getAvatarUrl() == null && u.getImage() == null
        ));
    }

    @Test
    void testDeleteAvatarNoAvatar() throws IOException {
        // Given
        String username = "testuser";
        UserModel user = new UserModel(username, "pass", "USER");
        when(userRepository.findByName(username)).thenReturn(Optional.of(user));
        
        // When
        avatarService.deleteAvatar(username);
        
        // Then
        verify(storageService, never()).delete(anyString());
    }

    @Test
    void testDeleteAvatarUserNotFound() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByName(username)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            avatarService.deleteAvatar(username);
        });
    }

    @Test
    void testDeleteAvatarStorageError() throws IOException {
        // Given
        String username = "testuser";
        UserModel user = new UserModel(username, "pass", "USER");
        user.setAvatarUrl("/api/v1/files/avatars/uuid.png");
        
        when(userRepository.findByName(username)).thenReturn(Optional.of(user));
        doThrow(new IOException("Delete failed")).when(storageService).delete(anyString());
        
        // When & Then
        assertThrows(StorageException.class, () -> {
            avatarService.deleteAvatar(username);
        });
    }
}

