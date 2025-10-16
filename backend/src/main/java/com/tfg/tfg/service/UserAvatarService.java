package com.tfg.tfg.service;

import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.storage.MinioStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Service for managing user profile images (avatars)
 */
@Service
public class UserAvatarService {

    private final MinioStorageService storageService;
    private final UserModelRepository userRepository;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/png"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public UserAvatarService(MinioStorageService storageService, UserModelRepository userRepository) {
        this.storageService = storageService;
        this.userRepository = userRepository;
    }

    /**
     * Upload and set user avatar
     * @param username The username
     * @param file The avatar image file
     * @return The public URL of the uploaded avatar
     * @throws IOException if upload fails
     * @throws IllegalArgumentException if file is invalid
     */
    public String uploadAvatar(String username, MultipartFile file) throws IOException {
        // Validate file
        validateAvatarFile(file);

        // Get user
        UserModel user = userRepository.findByName(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Delete old avatar if exists
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                deleteAvatarByUrl(user.getAvatarUrl());
            } catch (Exception e) {
                // Log but don't fail if old avatar deletion fails
                System.err.println("Failed to delete old avatar: " + e.getMessage());
            }
        }

        // Store new avatar
        String fileId = storageService.store(file, "avatars");
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
     * @throws IOException if deletion fails
     */
    public void deleteAvatar(String username) throws IOException {
        UserModel user = userRepository.findByName(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            deleteAvatarByUrl(user.getAvatarUrl());
            user.setAvatarUrl(null);
            user.setImage(null); // Also clear image field
            userRepository.save(user);
        }
    }

    /**
     * Validate avatar file
     * @param file The file to validate
     * @throws IllegalArgumentException if file is invalid
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid file type. Allowed types: " + String.join(", ", ALLOWED_CONTENT_TYPES)
            );
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB"
            );
        }

        // Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename is required");
        }
    }

    /**
     * Delete avatar file by URL
     * @param avatarUrl The avatar URL
     * @throws IOException if deletion fails
     */
    private void deleteAvatarByUrl(String avatarUrl) throws IOException {
        // Extract file identifier from URL
        // Assuming URL format: https://localhost:443/api/v1/files/{fileId}
        String fileId = avatarUrl.substring(avatarUrl.lastIndexOf("/files/") + 7);
        storageService.delete(fileId);
    }
}
