package com.tfg.tfg.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.tfg.tfg.repository.UserModelRepository;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private UserModelRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> listUsers() {
        List<UserDTO> users = userRepository.findAll().stream().map(u -> {
            UserDTO dto = new UserDTO();
            dto.id = u.getId();
            dto.name = u.getName();
            dto.email = u.getEmail();
            dto.roles = u.getRols();
            dto.active = u.isActive();
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{id}")
    public ResponseEntity<?> setUserActive(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        var opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        UserModel u = opt.get();
        u.setActive(active);
        userRepository.save(u);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> systemStats() {
        Map<String, Object> stats = Map.of(
            "users", userRepository.count()
        );
        return ResponseEntity.ok(stats);
    }
}
