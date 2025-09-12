package com.tfg.tfg.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;

@Service
public class UserService {

    @Autowired
    private UserModelRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<UserModel> findByName(String username) {
        return userRepository.findByName(username);
    }

    public void createUser(UserDTO userDTO) {
        if (userDTO == null || userDTO.getName() == null || userDTO.getPassword() == null) {
            throw new IllegalArgumentException("Invalid user payload");
        }

        if (userRepository.findByName(userDTO.getName()).isPresent()) {
            throw new IllegalStateException("User already exists");
        }

        UserModel user = new UserModel();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPass(passwordEncoder.encode(userDTO.getPassword()));
        user.setRols(java.util.List.of("USER"));

        userRepository.save(user);
    }
}
