package com.tfg.tfg.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.storage.MinioStorageService;

/**
 * Integration tests for UserController
 * Tests real database interactions and full request/response cycles
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    private static final String TEST_USER_NAME = "testuser";
    private static final String TEST_USER_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String ROLE_USER = "USER";
    private static final String API_USERS_ME = "/api/v1/users/me";
    private static final String JSON_NAME = "$.name";
    private static final String JSON_EMAIL = "$.email";
    private static final String INACTIVE_USER_NAME = "inactiveuser";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserModelRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MinioStorageService storageService;

    private UserModel testUser;
    private UserModel inactiveUser;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        userRepository.deleteAll();

        // Create test user
        testUser = new UserModel(TEST_USER_NAME, TEST_PASSWORD, ROLE_USER);
        testUser.setEmail(TEST_USER_EMAIL);
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        // Create inactive user
        inactiveUser = new UserModel(INACTIVE_USER_NAME, TEST_PASSWORD, ROLE_USER);
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setActive(false);
        inactiveUser = userRepository.save(inactiveUser);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetCurrentUser() throws Exception {
        mockMvc.perform(get(API_USERS_ME))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_NAME, is(TEST_USER_NAME)))
                .andExpect(jsonPath(JSON_EMAIL, is(TEST_USER_EMAIL)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testUpdateCurrentUser() throws Exception {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("newemail@example.com");

        mockMvc.perform(put(API_USERS_ME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_EMAIL, is("newemail@example.com")));
    }

    @Test
    void testGetCurrentUserUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "inactiveuser", roles = "USER")
    void testGetCurrentUserInactiveReturns403() throws Exception {
        mockMvc.perform(get(API_USERS_ME))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("User account is deactivated")));
    }

    @Test
    @WithMockUser(username = "inactiveuser", roles = "USER")
    void testUpdateCurrentUserInactiveReturns403() throws Exception {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("newemail@example.com");

        mockMvc.perform(put(API_USERS_ME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("User account is deactivated")));
    }
}
