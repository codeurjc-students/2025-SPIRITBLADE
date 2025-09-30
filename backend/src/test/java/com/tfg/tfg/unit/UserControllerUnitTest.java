package com.tfg.tfg.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.tfg.tfg.controller.UserController;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;

@WebMvcTest(UserController.class)
class UserControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserModelRepository userRepository;

    private UserModel testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserModel("testUser", "encodedPassword", "USER");
        testUser.setId(1L);
        testUser.setImage("test-image.jpg");
        testUser.setEmail("test@example.com");
    }

    @Test
    @WithMockUser
    void testListUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("testUser"))
                .andExpect(jsonPath("$[0].image").value("test-image.jpg"));

        verify(userRepository).findAll();
    }

    @Test
    @WithMockUser
    void testGetByName() throws Exception {
        when(userRepository.findByName("testUser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/v1/users/name/testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("testUser"))
                .andExpect(jsonPath("$.image").value("test-image.jpg"));

        verify(userRepository).findByName("testUser");
    }

    @Test
    @WithMockUser
    void testGetByNameNotFound() throws Exception {
        when(userRepository.findByName("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/name/nonexistent"))
                .andExpect(status().isNotFound());

        verify(userRepository).findByName("nonexistent");
    }

    @Test
    @WithMockUser
    void testChangeRole() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        mockMvc.perform(get("/api/v1/users/id/1/role/ADMIN"))
                .andExpect(status().isOk());

        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @WithMockUser
    void testChangeRoleUserNotFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/id/1/role/ADMIN"))
                .andExpect(status().isNotFound());

        verify(userRepository).findById(1L);
    }

    @Test
    @WithMockUser
    void testDeleteUser() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/v1/users/id/1/delete"))
                .andExpect(status().isOk());

        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    @WithMockUser
    void testDeleteUserNotFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/id/1/delete"))
                .andExpect(status().isNotFound());

        verify(userRepository).findById(1L);
    }

    @Test
    @WithMockUser
    void testGetMyProfile() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("testUser"))
                .andExpect(jsonPath("$.image").value("test-image.jpg"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userRepository).findAll();
    }

    @Test
    @WithMockUser
    void testGetMyProfileWhenNoUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isNotFound());

        verify(userRepository).findAll();
    }
}