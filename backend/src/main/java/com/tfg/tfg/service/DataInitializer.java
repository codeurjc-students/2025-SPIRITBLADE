package com.tfg.tfg.service;

import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.MatchRepository;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.repository.UserModelRepository;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final String ADMIN_USERNAME = "admin";
    private static final String USER_USERNAME = "user";
    
    private final UserModelRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserModelRepository userRepository, 
                          PasswordEncoder passwordEncoder,
                          SummonerRepository summonerRepo,
                          MatchRepository matchRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        // Generate secure default passwords for development (should be externalized in production)
        String adminPassword = System.getenv("ADMIN_DEFAULT_PASSWORD");
        if (adminPassword == null) {
            adminPassword = generateSecurePassword(ADMIN_USERNAME);
        }
        
        String userPassword = System.getenv("USER_DEFAULT_PASSWORD");
        if (userPassword == null) {
            userPassword = generateSecurePassword(USER_USERNAME);
        }
        
        // Create admin user if not exists
        if (userRepository.findByName(ADMIN_USERNAME).isEmpty()) {
            UserModel admin = new UserModel(ADMIN_USERNAME, passwordEncoder.encode(adminPassword), "ADMIN");
            admin.setEmail("admin@example.com");
            userRepository.save(admin);
            logger.info("Created default admin user: {} / [password from env or generated]", ADMIN_USERNAME);
        }

        // Create regular user if not exists
        if (userRepository.findByName(USER_USERNAME).isEmpty()) {
            UserModel user = new UserModel(USER_USERNAME, passwordEncoder.encode(userPassword), "USER");
            user.setEmail("user@example.com");
            user.setActive(true);
            userRepository.save(user);
            logger.info("Created default user: {} / [password from env or generated]", USER_USERNAME);
        }

    }

    
    private String generateSecurePassword(String prefix) {
        // Generate a more secure password for development
        return prefix + "Secure" + System.currentTimeMillis() % 10000 + "!";
    }
}
