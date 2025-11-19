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
    private static final String ADMIN_NAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ROLE_USER = "USER";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String API_USERS = "/api/v1/users";
    private static final String API_USERS_ME = "/api/v1/users/me";
    private static final String JSON_CONTENT = "$.content";
    private static final String JSON_CONTENT_NAME = "$.content[0].name";
    private static final String JSON_NAME = "$.name";
    private static final String JSON_EMAIL = "$.email";

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
        testUser = new UserModel(TEST_USER_NAME, TEST_PASSWORD, ROLE_USER);
        testUser.setEmail(TEST_USER_EMAIL);
        testUser.setActive(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersWithoutFilters() throws Exception {
        mockMvc.perform(get(API_USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_CONTENT, hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath(JSON_CONTENT_NAME, is(TEST_USER_NAME)))
                .andExpect(jsonPath("$.content[0].email", is(TEST_USER_EMAIL)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersWithPagination() throws Exception {
        // Create more users
        for (int i = 0; i < 5; i++) {
            UserModel user = new UserModel("user" + i, "pass" + i, ROLE_USER);
            user.setEmail("user" + i + "@test.com");
            userRepository.save(user);
        }

        mockMvc.perform(get(API_USERS)
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_CONTENT, hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(6)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersWithRoleFilter() throws Exception {
        // Create admin user
        UserModel admin = new UserModel(ADMIN_NAME, ADMIN_PASSWORD, ROLE_ADMIN);
        admin.setEmail("admin@example.com");
        userRepository.save(admin);

        mockMvc.perform(get(API_USERS)
                .param("role", ROLE_ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_CONTENT, hasSize(1)))
                .andExpect(jsonPath(JSON_CONTENT_NAME, is(ADMIN_NAME)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersWithActiveFilter() throws Exception {
        // Create inactive user
        UserModel inactive = new UserModel("inactive", "pass", ROLE_USER);
        inactive.setActive(false);
        userRepository.save(inactive);

        mockMvc.perform(get(API_USERS)
                .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_CONTENT, hasSize(1)))
                .andExpect(jsonPath(JSON_CONTENT_NAME, is("inactive")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersWithSearch() throws Exception {
        mockMvc.perform(get(API_USERS)
                .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_CONTENT, hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath(JSON_CONTENT_NAME, containsString("test")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsersWithRoleAndActiveFilter() throws Exception {
        // Create active ADMIN
        UserModel admin = new UserModel(ADMIN_NAME, ADMIN_PASSWORD, ROLE_ADMIN);
        admin.setEmail("admin@example.com");
        admin.setActive(true);
        userRepository.save(admin);

        mockMvc.perform(get(API_USERS)
                .param("role", ROLE_ADMIN)
                .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_CONTENT, hasSize(1)))
                .andExpect(jsonPath(JSON_CONTENT_NAME, is(ADMIN_NAME)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testListUsersWithoutAdminRole() throws Exception {
        mockMvc.perform(get(API_USERS))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUser() throws Exception {
        UserDTO newUser = new UserDTO();
        newUser.setName("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword(TEST_PASSWORD);
        newUser.setRoles(java.util.List.of(ROLE_USER));

        mockMvc.perform(post(API_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(JSON_NAME, is("newuser")))
                .andExpect(jsonPath(JSON_EMAIL, is("newuser@example.com")));
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
}
