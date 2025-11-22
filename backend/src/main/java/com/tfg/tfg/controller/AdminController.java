package com.tfg.tfg.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.model.mapper.UserMapper;
import com.tfg.tfg.service.UserService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<UserModel> usersPage;
        
        // Apply filters using service layer
        if (search != null && !search.trim().isEmpty()) {
            usersPage = userService.findBySearch(search, pageable);
        } else if (role != null && active != null) {
            usersPage = userService.findByRoleAndActive(role, active, pageable);
        } else if (role != null) {
            usersPage = userService.findByRole(role, pageable);
        } else if (active != null) {
            usersPage = userService.findByActive(active, pageable);
        } else {
            usersPage = userService.findAll(pageable);
        }
        
        Page<UserDTO> dtoPage = usersPage.map(UserMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        // Exceptions are handled by GlobalExceptionHandler
        UserModel createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toDTO(createdUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        UserModel user = userService.getUserById(id);
        
        // Prevent actions on admin users
        if (user.getRols() != null && user.getRols().contains(ROLE_ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        // Prevent username from being changed by admin
        if (userDTO.getName() != null && !userDTO.getName().equals(user.getName())) {
            return ResponseEntity.status(400).build(); // Username cannot be changed
        }
        
        UserModel updatedUser = userService.updateUserOrThrow(id, userDTO);
        return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}/toggle-active")
    public ResponseEntity<UserDTO> toggleUserActive(@PathVariable Long id) {
        UserModel user = userService.getUserById(id);
        
        // Prevent actions on admin users
        if (user.getRols() != null && user.getRols().contains(ROLE_ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        Optional<UserModel> updatedUserOpt = userService.toggleUserActive(id);
        if (updatedUserOpt.isPresent()) {
            return ResponseEntity.ok(UserMapper.toDTO(updatedUserOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        UserModel user = userService.getUserById(id);
        
        // Prevent deletion of admin users
        if (user.getRols() != null && user.getRols().contains(ROLE_ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        userService.deleteUserOrThrow(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> systemStats() {
        Map<String, Object> stats = Map.of(
            "users", userService.countUsers()
        );
        return ResponseEntity.ok(stats);
    }
}
