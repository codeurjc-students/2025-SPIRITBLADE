package com.tfg.tfg.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tfg.tfg.exception.UserAlreadyExistsException;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;

@Service
public class UserService {

    private final UserModelRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserModelRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<UserModel> findByName(String username) {
        return userRepository.findByName(username);
    }

    public UserModel createUser(UserDTO userDTO) {
        if (userDTO == null || userDTO.getName() == null || userDTO.getPassword() == null) {
            throw new IllegalArgumentException("User data is required. Username and password cannot be null");
        }

        if (userRepository.findByName(userDTO.getName()).isPresent()) {
            throw new UserAlreadyExistsException("User with username '" + userDTO.getName() + "' already exists");
        }

        UserModel user = new UserModel();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPass(passwordEncoder.encode(userDTO.getPassword()));
        
        // Use provided roles or default to USER
        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            user.setRols(userDTO.getRoles());
        } else {
            user.setRols(java.util.List.of("USER"));
        }
        
        user.setActive(true);

        return userRepository.save(user);
    }

    public Optional<UserModel> updateUser(Long id, UserDTO userDTO) {
        return userRepository.findById(id).map(user -> {
            if (userDTO.getName() != null) {
                user.setName(userDTO.getName());
            }
            if (userDTO.getEmail() != null) {
                user.setEmail(userDTO.getEmail());
            }
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                user.setPass(passwordEncoder.encode(userDTO.getPassword()));
            }
            if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
                user.setRols(userDTO.getRoles());
            }
            user.setActive(userDTO.isActive());
            
            return userRepository.save(user);
        });
    }

    public boolean deleteUser(Long id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return true;
        }).orElse(false);
    }

    public Optional<UserModel> toggleUserActive(Long id) {
        return userRepository.findById(id).map(user -> {
            user.setActive(!user.isActive());
            return userRepository.save(user);
        });
    }
}
