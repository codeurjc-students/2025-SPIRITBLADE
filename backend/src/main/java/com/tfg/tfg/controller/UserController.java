package com.tfg.tfg.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.repository.UserModelRepository;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserModelRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserDTO>> listUsers() {
        List<UserDTO> list = userRepository.findAll().stream().map(u -> {
            UserDTO dto = new UserDTO();
            dto.id = u.getId();
            dto.name = u.getName();
            dto.image = u.getImage();
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<UserDTO> getByName(@PathVariable String name) {
        return userRepository.findByName(name).map(u -> {
            UserDTO dto = new UserDTO();
            dto.id = u.getId();
            dto.name = u.getName();
            dto.image = u.getImage();
            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/id/{id}/role/{role}")
    public ResponseEntity<?> changeRole(@PathVariable Long id, @PathVariable String role) {
        return userRepository.findById(id).map(u -> {
            u.setRols(java.util.List.of(role));
            userRepository.save(u);
            return ResponseEntity.ok().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/id/{id}/delete")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> {
            userRepository.delete(u);
            return ResponseEntity.ok().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile() {
        // Development stub: return first user if exists
        return userRepository.findAll().stream().findFirst().map(u -> {
            UserDTO dto = new UserDTO();
            dto.id = u.getId();
            dto.name = u.getName();
            dto.image = u.getImage();
            dto.email = u.getEmail();
            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
