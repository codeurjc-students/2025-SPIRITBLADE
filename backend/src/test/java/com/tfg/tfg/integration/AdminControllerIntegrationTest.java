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
 * Integration tests for AdminController
 * Tests admin-specific operations with real database interactions
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerIntegrationTest {

    private static final String API_ADMIN_USERS = "/api/v1/admin/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserModelRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MinioStorageService storageService;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        userRepository.deleteAll();

        // Create test users
        UserModel admin = new UserModel("admin", "password", "ADMIN");
        admin.setEmail("admin@example.com");
        admin.setActive(true);
        userRepository.save(admin);

        UserModel user = new UserModel("testuser", "password", "USER");
        user.setEmail("user@example.com");
        user.setActive(true);
        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testListUsersAsAdmin() throws Exception {
        mockMvc.perform(get(API_ADMIN_USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.List.class)))
                .andExpect(jsonPath("$.content.size()", greaterThan(0)));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testListUsersWithPagination() throws Exception {
        mockMvc.perform(get(API_ADMIN_USERS)
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", notNullValue()))
                .andExpect(jsonPath("$.totalElements", notNullValue()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testListUsersWithRoleFilter() throws Exception {
        mockMvc.perform(get(API_ADMIN_USERS)
                .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testListUsersWithActiveFilter() throws Exception {
        mockMvc.perform(get(API_ADMIN_USERS)
                .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testListUsersWithSearchFilter() throws Exception {
        mockMvc.perform(get(API_ADMIN_USERS)
                .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateUserAsAdmin() throws Exception {
        UserDTO newUser = new UserDTO();
        newUser.setName("newuser");
        newUser.setPassword("password123");
        newUser.setEmail("newuser@example.com");
        newUser.setRoles(java.util.List.of("USER"));

        mockMvc.perform(post(API_ADMIN_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("newuser")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateUserAsAdmin() throws Exception {
        UserModel user = userRepository.findByName("testuser").orElseThrow();
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("updated@example.com");

        mockMvc.perform(put(API_ADMIN_USERS + "/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("updated@example.com")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testToggleUserActiveAsAdmin() throws Exception {
        UserModel user = userRepository.findByName("testuser").orElseThrow();

        mockMvc.perform(put(API_ADMIN_USERS + "/" + user.getId() + "/toggle-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testDeleteUserAsAdmin() throws Exception {
        UserModel user = userRepository.findByName("testuser").orElseThrow();

        mockMvc.perform(delete(API_ADMIN_USERS + "/" + user.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testListUsersAsRegularUserReturns403() throws Exception {
        mockMvc.perform(get(API_ADMIN_USERS))
                .andExpect(status().isForbidden());
    }

    @Test
    void testListUsersUnauthorized() throws Exception {
        mockMvc.perform(get(API_ADMIN_USERS))
                .andExpect(status().isUnauthorized());
    }
}