package com.tfg.tfg.service;

import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.storage.MinioStorageService;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final String ADMIN_USERNAME = "admin";
    private static final String USER_USERNAME = "user";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";
    private static final String DEFAULT_USER_PASSWORD = "pass";
    private static final String DEFAULT_AVATAR_FILENAME = "default-profile.png";
    private static final String DEFAULT_AVATAR_CONTENT_TYPE = "image/png";
    
    private final UserModelRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MinioStorageService minioStorageService;

    public DataInitializer(UserModelRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          MinioStorageService minioStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.minioStorageService = minioStorageService;
    }

    @PostConstruct
    public void init() {
        boolean isProduction = isProductionMode();
        
        String adminPassword = resolvePassword("ADMIN_DEFAULT_PASSWORD", ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD, isProduction);
        String userPassword = resolvePassword("USER_DEFAULT_PASSWORD", USER_USERNAME, DEFAULT_USER_PASSWORD, isProduction);
        
        createAdminUserIfNotExists(adminPassword, isProduction);
        createRegularUserIfNotExists(userPassword, isProduction);
    }

    private boolean isProductionMode() {
        String profile = System.getProperty("spring.profiles.active", "development");
        return "production".equalsIgnoreCase(profile);
    }

    private String resolvePassword(String envVarName, String username, String defaultPassword, boolean isProduction) {
        String password = System.getenv(envVarName);
        if (password == null) {
            if (isProduction) {
                password = generateSecurePassword(username);
                logger.warn("Production mode detected but {} not set! Using generated password.", envVarName);
            } else {
                password = defaultPassword;
                logger.info("Development mode: Using default password for {} user", username);
            }
        }
        return password;
    }

    private void createAdminUserIfNotExists(String adminPassword, boolean isProduction) {
        if (userRepository.findByName(ADMIN_USERNAME).isEmpty()) {
            UserModel admin = new UserModel(ADMIN_USERNAME, passwordEncoder.encode(adminPassword), "ADMIN");
            admin.setEmail("admin@example.com");
            admin = userRepository.save(admin);

            // Upload default avatar to storage (MinIO) and set avatarUrl
            try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("static/img/" + DEFAULT_AVATAR_FILENAME)) {
                if (is == null) {
                    logger.warn("Default avatar resource not found: {}", DEFAULT_AVATAR_FILENAME);
                } else {
                    byte[] bytes = is.readAllBytes();
                    MultipartFile multipartFile = new SimpleMultipartFile(bytes, DEFAULT_AVATAR_FILENAME, DEFAULT_AVATAR_CONTENT_TYPE);
                    String key = minioStorageService.store(multipartFile, "avatars");
                    String publicUrl = minioStorageService.getPublicUrl(key);
                    admin.setAvatarUrl(publicUrl);
                    userRepository.save(admin);
                }
            } catch (Exception e) {
                logger.warn("Failed to upload default avatar for admin user: {}", e.getMessage());
            }

            logUserCreation(ADMIN_USERNAME, adminPassword, isProduction);
        }
    }

    private void createRegularUserIfNotExists(String userPassword, boolean isProduction) {
        if (userRepository.findByName(USER_USERNAME).isEmpty()) {
            UserModel user = new UserModel(USER_USERNAME, passwordEncoder.encode(userPassword), "USER");
            user.setEmail("user@example.com");
            user.setActive(true);
            user = userRepository.save(user);

            // Upload default avatar to storage (MinIO) and set avatarUrl
            try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("static/img/" + DEFAULT_AVATAR_FILENAME)) {
                if (is == null) {
                    logger.warn("Default avatar resource not found: {}", DEFAULT_AVATAR_FILENAME);
                } else {
                    byte[] bytes = is.readAllBytes();
                    MultipartFile multipartFile = new SimpleMultipartFile(bytes, DEFAULT_AVATAR_FILENAME, DEFAULT_AVATAR_CONTENT_TYPE);
                    String key = minioStorageService.store(multipartFile, "avatars");
                    String publicUrl = minioStorageService.getPublicUrl(key);
                    user.setAvatarUrl(publicUrl);
                    userRepository.save(user);
                }
            } catch (Exception e) {
                logger.warn("Failed to upload default avatar for regular user: {}", e.getMessage());
            }

            logUserCreation(USER_USERNAME, userPassword, isProduction);
        }
    }

    private void logUserCreation(String username, String password, boolean isProduction) {
        if (isProduction) {
            logger.info("Created default user: {} / [password from environment]", username);
        } else {
            logger.info("Created default user: {} / Password: {}", username, password);
        }
    }

    
    private String generateSecurePassword(String prefix) {
        // Generate a more secure password for development
        return prefix + "Secure" + System.currentTimeMillis() % 10000 + "!";
    }

    /**
     * Lightweight MultipartFile implementation backed by a byte[] array.
     * Used only for creating default avatar files in DataInitializer.
     */
    private static class SimpleMultipartFile implements org.springframework.web.multipart.MultipartFile {
        private final byte[] content;
        private final String fileName;
        private final String contentType;

        public SimpleMultipartFile(byte[] content, String fileName, String contentType) {
            this.content = content == null ? new byte[0] : content;
            this.fileName = fileName;
            this.contentType = contentType;
        }

        @Override
        public String getName() { return fileName; }

        @Override
        public String getOriginalFilename() { return getName(); }

        @Override
        public String getContentType() { return contentType; }

        @Override
        public boolean isEmpty() { return content.length == 0; }

        @Override
        public long getSize() { return content.length; }

        @Override
        public byte[] getBytes() { return content; }

        @Override
        public java.io.InputStream getInputStream() { return new java.io.ByteArrayInputStream(content); }

        @Override
        public void transferTo(java.io.File dest) throws java.io.IOException, java.lang.IllegalStateException {
            Files.write(dest.toPath(), content);
        }
    }
}
