package com.tfg.tfg.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;

@Service
public class UserService {

    public Optional<UserModel> findByName(String username) {
        // Minimal stub: backend logic not implemented yet
        return Optional.empty();
    }

    public void createUser(UserDTO userDTO) {
        // Minimal stub: persist user later
    }
}
