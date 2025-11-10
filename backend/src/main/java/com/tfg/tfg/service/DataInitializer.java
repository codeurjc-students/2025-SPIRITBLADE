package com.tfg.tfg.service;

import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.storage.MinioStorageService;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final String ADMIN_USERNAME = "admin";
    private static final String USER_USERNAME = "user";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";
    private static final String DEFAULT_USER_PASSWORD = "pass";
    
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
            try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("static/img/default-profile.png")) {
                String key = minioStorageService.store(is, "default-profile.png", "image/png", "avatars");
                String publicUrl = minioStorageService.getPublicUrl(key);
                admin.setAvatarUrl(publicUrl);
                userRepository.save(admin);
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
            try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("static/img/default-profile.png")) {
                String key = minioStorageService.store(is, "default-profile.png", "image/png", "avatars");
                String publicUrl = minioStorageService.getPublicUrl(key);
                user.setAvatarUrl(publicUrl);
                userRepository.save(user);
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
}
