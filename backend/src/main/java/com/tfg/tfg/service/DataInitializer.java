package com.tfg.tfg.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserModelRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Create admin user if not exists
        if (userRepository.findByName("admin").isEmpty()) {
            UserModel admin = new UserModel("admin", passwordEncoder.encode("admin123"), "ADMIN");
            admin.setEmail("admin@example.com");
            userRepository.save(admin);
            System.out.println("Created default admin user: admin / admin123");
        }

        // Create regular user if not exists
        if (userRepository.findByName("user").isEmpty()) {
            UserModel user = new UserModel("user", passwordEncoder.encode("user123"), "USER");
            user.setEmail("user@example.com");
            userRepository.save(user);
            System.out.println("Created default user: user / user123");
        }
    }
}
