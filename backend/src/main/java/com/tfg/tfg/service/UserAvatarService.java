package com.tfg.tfg.service;

import com.tfg.tfg.exception.InvalidFileException;
import com.tfg.tfg.exception.StorageException;
import com.tfg.tfg.exception.UserNotFoundException;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.storage.MinioStorageService;
import com.tfg.tfg.util.PngFileValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service for managing user profile images (avatars)
 */
@Service
public class UserAvatarService {

    private final MinioStorageService storageService;
    private final UserModelRepository userRepository;

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB

    public UserAvatarService(MinioStorageService storageService, UserModelRepository userRepository) {
        this.storageService = storageService;
        this.userRepository = userRepository;
    }

    /**
     * Upload and set user avatar
     * @param username The username
     * @param file The avatar image file
     * @return The public URL of the uploaded avatar
     * @throws StorageException if upload fails
     * @throws InvalidFileException if file is invalid
     */
    public String uploadAvatar(String username, MultipartFile file) {
        // Validate file
        validateAvatarFile(file);

        // Get user
        UserModel user = userRepository.findByName(username)
            .orElseThrow(() -> new UserNotFoundException("User '" + username + "' not found"));

        // Delete old avatar if exists
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                deleteAvatarByUrl(user.getAvatarUrl());
            } catch (Exception e) {
                // Log but don't fail if old avatar deletion fails
                // Old avatar will be orphaned but user can still upload new one
                org.slf4j.LoggerFactory.getLogger(UserAvatarService.class)
                    .warn("Failed to delete old avatar for user {}: {}", username, e.getMessage());
            }
        }

        // Store new avatar
        String fileId;
        try {
            fileId = storageService.store(file, "avatars");
        } catch (IOException e) {
            throw new StorageException("Failed to upload avatar file: " + e.getMessage(), e);
        }
        String publicUrl = storageService.getPublicUrl(fileId);

        // Update user with avatar URL (update both fields for compatibility)
        user.setAvatarUrl(publicUrl);
        user.setImage(publicUrl); // Also set image field for backward compatibility
        userRepository.save(user);

        return publicUrl;
    }

    /**
     * Delete user avatar
     * @param username The username
     * @throws StorageException if deletion fails
     */
    public void deleteAvatar(String username) {
        UserModel user = userRepository.findByName(username)
            .orElseThrow(() -> new UserNotFoundException("User '" + username + "' not found"));

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                deleteAvatarByUrl(user.getAvatarUrl());
            } catch (IOException e) {
                throw new StorageException("Failed to delete avatar file: " + e.getMessage(), e);
            }
            user.setAvatarUrl(null);
            user.setImage(null); // Also clear image field
            userRepository.save(user);
        }
    }

    /**
     * Validate avatar file
     * @param file The file to validate
     * @throws InvalidFileException if file is invalid
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                "File size exceeds maximum allowed size of 5MB"
            );
        }

        // Use centralized PNG validator for type and extension checks
        try {
            PngFileValidator.validatePngFile(file);
        } catch (IllegalArgumentException | IOException e) {
            throw new InvalidFileException(e.getMessage());
        }
    }

    /**
     * Delete avatar file by URL
     * @param avatarUrl The avatar URL
     * @throws IOException if deletion fails
     */
    private void deleteAvatarByUrl(String avatarUrl) throws IOException {
        // Extract file identifier from URL (expected to contain '/files/{fileId}')
        int idx = avatarUrl.lastIndexOf("/files/");
        if (idx < 0 || idx + 7 >= avatarUrl.length()) {
            throw new IOException("Invalid avatar URL format, cannot extract file identifier: " + avatarUrl);
        }
        String fileId = avatarUrl.substring(idx + 7);
        storageService.delete(fileId);
    }
}
