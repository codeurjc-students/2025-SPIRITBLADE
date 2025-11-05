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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserModelRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MinioStorageService storageService;

    private UserModel testUser;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        userRepository.deleteAll();

        // Create test user
        testUser = new UserModel("testuser", "password123", "USER");
        testUser.setEmail("test@example.com");
        testUser.setActive(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersWithoutFilters() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].name", is("testuser")))
                .andExpect(jsonPath("$.content[0].email", is("test@example.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersWithPagination() throws Exception {
        // Create more users
        for (int i = 0; i < 5; i++) {
            UserModel user = new UserModel("user" + i, "pass" + i, "USER");
            user.setEmail("user" + i + "@test.com");
            userRepository.save(user);
        }

        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(6)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersWithRoleFilter() throws Exception {
        // Create admin user
        UserModel admin = new UserModel("admin", "admin123", "ADMIN");
        admin.setEmail("admin@example.com");
        userRepository.save(admin);

        mockMvc.perform(get("/api/v1/users")
                .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("admin")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersWithActiveFilter() throws Exception {
        // Create inactive user
        UserModel inactive = new UserModel("inactive", "pass", "USER");
        inactive.setActive(false);
        userRepository.save(inactive);

        mockMvc.perform(get("/api/v1/users")
                .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("inactive")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersWithSearch() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].name", containsString("test")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersWithRoleAndActiveFilter() throws Exception {
        // Create active ADMIN
        UserModel admin = new UserModel("admin", "admin123", "ADMIN");
        admin.setEmail("admin@example.com");
        admin.setActive(true);
        userRepository.save(admin);

        mockMvc.perform(get("/api/v1/users")
                .param("role", "ADMIN")
                .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("admin")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testListUsersWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.name", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserByName() throws Exception {
        mockMvc.perform(get("/api/v1/users/name/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserByNameNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/name/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUser() throws Exception {
        UserDTO newUser = new UserDTO();
        newUser.setName("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("password123");
        newUser.setRoles(java.util.List.of("USER"));

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("newuser")))
                .andExpect(jsonPath("$.email", is("newuser@example.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser() throws Exception {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("updatedname");
        updateDTO.setEmail("updated@example.com");

        mockMvc.perform(put("/api/v1/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("updatedname")))
                .andExpect(jsonPath("$.email", is("updated@example.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserNotFound() throws Exception {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("updatedname");

        mockMvc.perform(put("/api/v1/users/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testToggleUserActive() throws Exception {
        // Toggle to inactive
        mockMvc.perform(put("/api/v1/users/" + testUser.getId() + "/toggle-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));

        // Toggle back to active
        mockMvc.perform(put("/api/v1/users/" + testUser.getId() + "/toggle-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + testUser.getId()))
                .andExpect(status().isNoContent());

        // Verify user is deleted
        mockMvc.perform(get("/api/v1/users/" + testUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUserNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/users/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetCurrentUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testUpdateCurrentUser() throws Exception {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("newemail@example.com");

        mockMvc.perform(put("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("newemail@example.com")));
    }

    @Test
    void testGetCurrentUserUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersEmptyResult() throws Exception {
        userRepository.deleteAll();

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchUsersByEmail() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .param("search", "test@example"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email", is("test@example.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserPartialData() throws Exception {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("onlyemail@example.com");
        // Don't set name - should keep existing

        mockMvc.perform(put("/api/v1/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("testuser"))) // Should remain unchanged
                .andExpect(jsonPath("$.email", is("onlyemail@example.com")));
    }
}
