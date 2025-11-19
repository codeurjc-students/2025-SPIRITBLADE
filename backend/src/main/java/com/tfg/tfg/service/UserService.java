package com.tfg.tfg.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tfg.tfg.exception.UserAlreadyExistsException;
import com.tfg.tfg.exception.UserNotFoundException;
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

    /**
     * Get user by ID or throw exception if not found
     * @throws UserNotFoundException if user doesn't exist
     */
    public UserModel getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User with ID '" + id + "' not found"));
    }

    /**
     * Get user by username or throw exception if not found
     * @throws UserNotFoundException if user doesn't exist
     */
    public UserModel getUserByName(String username) {
        return userRepository.findByName(username)
            .orElseThrow(() -> new UserNotFoundException("User '" + username + "' not found"));
    }

    public Optional<UserModel> findFirstUser() {
        return userRepository.findFirstByOrderByIdAsc();
    }

    public Page<UserModel> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<UserModel> findBySearch(String search, Pageable pageable) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            search, search, pageable);
    }

    public Page<UserModel> findByRoleAndActive(String role, Boolean active, Pageable pageable) {
        return userRepository.findByRolsContainingAndActive(role, active, pageable);
    }

    public Page<UserModel> findByRole(String role, Pageable pageable) {
        return userRepository.findByRolsContaining(role, pageable);
    }

    public Page<UserModel> findByActive(Boolean active, Pageable pageable) {
        return userRepository.findByActive(active, pageable);
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

    /**
     * Update user or throw exception if not found
     * @throws UserNotFoundException if user doesn't exist
     */
    public UserModel updateUserOrThrow(Long id, UserDTO userDTO) {
        UserModel user = getUserById(id);
        
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
    }

    public Optional<UserModel> updateUserProfile(String username, UserDTO userDTO) {
        return userRepository.findByName(username).map(user -> {
            if (userDTO.getEmail() != null) {
                user.setEmail(userDTO.getEmail());
            }
            if (userDTO.getAvatarUrl() != null) {
                user.setAvatarUrl(userDTO.getAvatarUrl());
            }
            return userRepository.save(user);
        });
    }

    public Optional<UserModel> changeUserRole(Long id, List<String> roles) {
        return userRepository.findById(id).map(user -> {
            user.setRols(roles);
            return userRepository.save(user);
        });
    }

    /**
     * Delete user or throw exception if not found
     * @throws UserNotFoundException if user doesn't exist
     */
    public void deleteUserOrThrow(Long id) throws UserNotFoundException {
        try {
            UserModel user = getUserById(id);
            userRepository.delete(user);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new UserNotFoundException("User with ID '" + id + "' not found");
        }
    }

    public Optional<UserModel> toggleUserActive(Long id) {
        return userRepository.findById(id).map(user -> {
            user.setActive(!user.isActive());
            return userRepository.save(user);
        });
    }

    /**
     * Set user active status or throw exception if not found
     * @throws UserNotFoundException if user doesn't exist
     */
    public UserModel setUserActiveOrThrow(Long id, boolean active) {
        UserModel user = getUserById(id);
        user.setActive(active);
        return userRepository.save(user);
    }

    public Optional<UserModel> linkSummoner(String username, String puuid, String summonerName, String region) {
        return userRepository.findByName(username).map(user -> {
            user.setLinkedSummonerPuuid(puuid);
            user.setLinkedSummonerName(summonerName);
            user.setLinkedSummonerRegion(region);
            return userRepository.save(user);
        });
    }

    public Optional<UserModel> unlinkSummoner(String username) {
        return userRepository.findByName(username).map(user -> {
            user.setLinkedSummonerPuuid(null);
            user.setLinkedSummonerName(null);
            user.setLinkedSummonerRegion(null);
            return userRepository.save(user);
        });
    }

    public List<UserModel> findAllUsers() {
        return userRepository.findAll();
    }

    public long countUsers() {
        return userRepository.count();
    }

    public UserModel save(UserModel user) {
        return userRepository.save(user);
    }
}
