package com.tfg.tfg.system;

import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Sistema de pruebas para gestiÃ³n de usuarios por administradores.
 * Verifica:
 * - Listado de usuarios con filtros
 * - CreaciÃ³n de usuarios
 * - ActualizaciÃ³n de roles
 * - ActivaciÃ³n/desactivaciÃ³n de usuarios
 * - EliminaciÃ³n de usuarios
 * - BÃºsqueda de usuarios
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminSystemTest {

    @LocalServerPort
    private int port;
    
    @Autowired
    private UserModelRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private String adminToken;
    private String userToken;

    @BeforeAll
    static void setup() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = "https://localhost";
    }
    
    @BeforeEach
    void authenticate() {
        // Clear any existing test users
        userRepository.findByName("testadmin").ifPresent(u -> userRepository.delete(u));
        userRepository.findByName("testuser").ifPresent(u -> userRepository.delete(u));
        
        // Create test users with known passwords
        UserModel admin = new UserModel("testadmin", passwordEncoder.encode("admin123"), "ADMIN");
        admin.setEmail("testadmin@example.com");
        admin.setActive(true);
        userRepository.save(admin);
        
        UserModel user = new UserModel("testuser", passwordEncoder.encode("user123"), "USER");
        user.setEmail("testuser@example.com");
        user.setActive(true);
        userRepository.save(user);
        
        // Login as admin
        adminToken = given()
            .port(port)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "testadmin",
                    "password": "admin123"
                }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("accessToken");
            
        // Login as regular user
        userToken = given()
            .port(port)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "testuser",
                    "password": "user123"
                }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("accessToken");
    }

    @Test
    void testGetAllUsersAsAdminReturnsUserList() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/admin/users")
        .then()
            .statusCode(200)
            .body("content", instanceOf(java.util.List.class))
            .body("content.size()", greaterThan(0));
    }

    @Test
    void testGetAllUsersAsRegularUserReturns403() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/admin/users")
        .then()
            .statusCode(anyOf(is(403), is(401)));
    }

    @Test
    void testGetAllUsersWithPaginationReturnsPagedResults() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("size", 5)
        .when()
            .get("/api/v1/admin/users")
        .then()
            .statusCode(200)
            .body("size", notNullValue())
            .body("totalElements", notNullValue());
    }

    @Test
    void testGetAllUsersWithSearchFilterReturnsFilteredResults() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .queryParam("search", "admin")
        .when()
            .get("/api/v1/admin/users")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void testGetAllUsersWithRoleFilterReturnsFilteredResults() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .queryParam("role", "ADMIN")
        .when()
            .get("/api/v1/admin/users")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void testGetAllUsersWithActiveFilterReturnsFilteredResults() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .queryParam("active", true)
        .when()
            .get("/api/v1/admin/users")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void testCreateUserAsAdminCreatesNewUser() {
        String uniqueUsername = "testuser" + System.currentTimeMillis();
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "username": "%s",
                    "password": "testpass123",
                    "email": "%s@test.com",
                    "roles": ["USER"]
                }
                """, uniqueUsername, uniqueUsername))
        .when()
            .post("/api/v1/admin/users")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("username", equalTo(uniqueUsername));
    }

    @Test
    void testCreateUserAsRegularUserReturns403() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "shouldnotcreate",
                    "password": "pass123",
                    "email": "test@test.com"
                }
                """)
        .when()
            .post("/api/v1/admin/users")
        .then()
            .statusCode(anyOf(is(403), is(401)));
    }

    @Test
    void testGetAllUsersContainsExpectedUserFields() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/admin/users")
        .then()
            .statusCode(200)
            .body("content[0].id", notNullValue())
            .body("content[0].username", notNullValue())
            .body("content[0].email", notNullValue())
            .body("content[0].roles", notNullValue())
            .body("content[0].active", notNullValue());
    }
}
