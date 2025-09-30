package com.tfg.tfg.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.tfg.controller.LoginRestController;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.security.jwt.AuthResponse;
import com.tfg.tfg.security.jwt.LoginRequest;
import com.tfg.tfg.security.jwt.UserLoginService;
import com.tfg.tfg.service.UserService;

@WebMvcTest(LoginRestController.class)
class LoginRestControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserLoginService userLoginService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        userDTO = new UserDTO();
        userDTO.setName("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password");
    }

    @Test
    void testLoginSuccess() throws Exception {
        AuthResponse authResponse = new AuthResponse(AuthResponse.Status.SUCCESS, "Login successful");
        when(userLoginService.login(any(), any(LoginRequest.class)))
                .thenReturn(ResponseEntity.ok(authResponse));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void testLoginFailure() throws Exception {
        AuthResponse authResponse = new AuthResponse(AuthResponse.Status.FAILURE, "Invalid credentials");
        when(userLoginService.login(any(), any(LoginRequest.class)))
                .thenReturn(ResponseEntity.badRequest().body(authResponse));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void testRefreshTokenSuccess() throws Exception {
        AuthResponse authResponse = new AuthResponse(AuthResponse.Status.SUCCESS, "Token refreshed");
        when(userLoginService.refresh(any(), any(String.class)))
                .thenReturn(ResponseEntity.ok(authResponse));

        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Token refreshed"));
    }

    @Test
    void testLogout() throws Exception {
        when(userLoginService.logout(any())).thenReturn("Logged out successfully");

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void testRegisterSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"USER"})
    void testMeEndpointAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void testMeEndpointUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginWithInvalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterWithInvalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());
    }
}