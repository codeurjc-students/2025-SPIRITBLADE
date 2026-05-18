package com.tfg.tfg.controller;

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
import com.tfg.tfg.service.interfaces.IUserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final IUserService userService;

    public AdminController(IUserService userService) {
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
        UserModel createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toDTO(createdUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        UserModel user = userService.getUserById(id);
        
        if (user.getRols() != null && user.getRols().contains(ROLE_ADMIN)) {
            return ResponseEntity.status(403).build();
        }
        
        if (userDTO.getName() != null && !userDTO.getName().equals(user.getName())) {
            return ResponseEntity.status(400).build();
        }
        
        UserModel updatedUser = userService.updateUserOrThrow(id, userDTO);
        return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}/toggle-active")
    public ResponseEntity<UserDTO> toggleUserActive(@PathVariable Long id) {
        UserModel user = userService.getUserById(id);
        
        if (user.getRols() != null && user.getRols().contains(ROLE_ADMIN)) {
            return ResponseEntity.status(403).build();
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
        
        if (user.getRols() != null && user.getRols().contains(ROLE_ADMIN)) {
            return ResponseEntity.status(403).build();
        }
        
        userService.deleteUserOrThrow(id);
        return ResponseEntity.noContent().build();
    }
}
