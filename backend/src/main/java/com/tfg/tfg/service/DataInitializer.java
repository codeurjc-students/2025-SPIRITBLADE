package com.tfg.tfg.service;

import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;

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

    public DataInitializer(UserModelRepository userRepository, 
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        // Get active Spring profile (production, development, etc.)
        String profile = System.getProperty("spring.profiles.active", "development");
        boolean isProduction = "production".equalsIgnoreCase(profile);
        
        // Use environment variables in production, fixed passwords in development
        String adminPassword = System.getenv("ADMIN_DEFAULT_PASSWORD");
        if (adminPassword == null) {
            if (isProduction) {
                adminPassword = generateSecurePassword(ADMIN_USERNAME);
                logger.warn("Production mode detected but ADMIN_DEFAULT_PASSWORD not set! Using generated password.");
            } else {
                // Fixed password for development/demo
                adminPassword = DEFAULT_ADMIN_PASSWORD;
                logger.info("Development mode: Using default password for admin user");
            }
        }
        
        String userPassword = System.getenv("USER_DEFAULT_PASSWORD");
        if (userPassword == null) {
            if (isProduction) {
                userPassword = generateSecurePassword(USER_USERNAME);
                logger.warn("Production mode detected but USER_DEFAULT_PASSWORD not set! Using generated password.");
            } else {
                // Fixed password for development/demo
                userPassword = DEFAULT_USER_PASSWORD;
                logger.info("Development mode: Using default password for regular user");
            }
        }
        
        // Create admin user if not exists
        if (userRepository.findByName(ADMIN_USERNAME).isEmpty()) {
            UserModel admin = new UserModel(ADMIN_USERNAME, passwordEncoder.encode(adminPassword), "ADMIN");
            admin.setEmail("admin@example.com");
            userRepository.save(admin);
            if (isProduction) {
                logger.info("Created default admin user: {} / [password from environment]", ADMIN_USERNAME);
            } else {
                logger.info("Created default admin user: {} / Password: {}", ADMIN_USERNAME, adminPassword);
            }
        }

        // Create regular user if not exists
        if (userRepository.findByName(USER_USERNAME).isEmpty()) {
            UserModel user = new UserModel(USER_USERNAME, passwordEncoder.encode(userPassword), "USER");
            user.setEmail("user@example.com");
            user.setActive(true);
            userRepository.save(user);
            if (isProduction) {
                logger.info("Created default user: {} / [password from environment]", USER_USERNAME);
            } else {
                logger.info("Created default user: {} / Password: {}", USER_USERNAME, userPassword);
            }
        }

    }

    
    private String generateSecurePassword(String prefix) {
        // Generate a more secure password for development
        return prefix + "Secure" + System.currentTimeMillis() % 10000 + "!";
    }
}
