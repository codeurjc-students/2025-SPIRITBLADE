package com.tfg.tfg.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<List<UserDTO>> listUsers() {
        List<UserDTO> users = userService.findAllUsers().stream()
            .map(UserMapper::toDTO)
            .toList();

        return ResponseEntity.ok(users);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{id}")
    public ResponseEntity<Void> setUserActive(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        UserModel user = userService.getUserById(id);
        
        // Prevent actions on admin users
        if (user.getRols() != null && user.getRols().contains(ROLE_ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        userService.setUserActiveOrThrow(id, active);
        return ResponseEntity.ok().build();
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
