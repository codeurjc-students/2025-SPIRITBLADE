package com.tfg.tfg.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.tfg.controller.AdminController;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.config.TestSecurityConfig;

@WebMvcTest(AdminController.class)
class AdminControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserModelRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserModel testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserModel("testUser", "encodedPassword", "USER");
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setActive(true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("testUser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(userRepository).findAll();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testListUsersWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSetUserActive() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Map<String, Object> requestBody = Map.of("active", false);

        mockMvc.perform(patch("/api/v1/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSetUserActiveNotFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Map<String, Object> requestBody = Map.of("active", false);

        mockMvc.perform(patch("/api/v1/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound());

        verify(userRepository).findById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUserNotFound() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isNotFound());

        verify(userRepository).existsById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSystemStats() throws Exception {
        when(userRepository.count()).thenReturn(5L);

        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").value(5));

        verify(userRepository).count();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAdminEndpointsWithoutAdminRole() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\": false}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isForbidden());
    }
}