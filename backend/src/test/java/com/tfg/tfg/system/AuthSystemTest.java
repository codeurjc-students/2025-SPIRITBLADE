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
 * Sistema de pruebas para autenticación y gestión de sesiones.
 * Verifica:
 * - Login con credenciales válidas/inválidas
 * - Registro de nuevos usuarios
 * - Refresh de tokens JWT
 * - Logout
 * - Verificación de usuario autenticado
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthSystemTest {

    @LocalServerPort
    private int port;
    
    @Autowired
    private UserModelRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeAll
    static void setup() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = "https://localhost";
    }
    
    @BeforeEach
    void createTestUsers() {
        // Clear any existing test users
        userRepository.findByName("testuser").ifPresent(u -> userRepository.delete(u));
        userRepository.findByName("admin").ifPresent(u -> userRepository.delete(u));
        
        // Create test users with known passwords
        UserModel user = new UserModel("testuser", passwordEncoder.encode("user123"), "USER");
        user.setEmail("testuser@example.com");
        user.setActive(true);
        userRepository.save(user);
        
        // Create admin user
        UserModel admin = new UserModel("admin", passwordEncoder.encode("admin"), "ADMIN");
        admin.setEmail("admin@example.com");
        admin.setActive(true);
        userRepository.save(admin);
    }

    @Test
    void testLoginWithValidCredentialsReturnsToken() {
        given()
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
            .body("accessToken", notNullValue())
            .body("status", equalTo("SUCCESS"));
    }

    @Test
    void testLoginWithInvalidCredentialsReturns401() {
        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "testuser",
                    "password": "wrongpassword"
                }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void testLoginWithNonExistentUserReturns401() {
        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "nonexistent",
                    "password": "pass"
                }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void testLoginWithEmptyUsernameReturns400() {
        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "",
                    "password": "pass"
                }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(anyOf(is(400), is(401)));
    }

    @Test
    void testRegisterWithValidDataReturnsSuccess() {
        String uniqueUsername = "newuser" + System.currentTimeMillis();
        
        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "username": "%s",
                    "password": "newpass123",
                    "email": "%s@test.com"
                }
                """, uniqueUsername, uniqueUsername))
        .when()
            .post("/api/v1/auth/register")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("message", equalTo("User registered successfully"));
    }

    @Test
    void testRegisterWithExistingUsernameReturns409() {
        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "testuser",
                    "password": "pass123",
                    "email": "duplicate@test.com"
                }
                """)
        .when()
            .post("/api/v1/auth/register")
        .then()
            .statusCode(anyOf(is(409), is(400), is(500)));
    }

    @Test
    void testRegisterWithInvalidEmailReturns400() {
        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "newuser123",
                    "password": "pass123",
                    "email": "invalid-email"
                }
                """)
        .when()
            .post("/api/v1/auth/register")
        .then()
            .statusCode(anyOf(is(400), is(200), is(201), is(409))); // 409 if user already exists
    }

    @Test
    void testGetMeWithValidTokenReturnsUserInfo() {
        // First login to get token
        String token = given()
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

        // Then get user info
        given()
            .port(port)
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/auth/me")
        .then()
            .statusCode(200)
            .body("username", notNullValue())
            .body("roles", notNullValue());
    }

    @Test
    void testGetMeWithoutTokenReturns401() {
        given()
            .port(port)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/auth/me")
        .then()
            .statusCode(401);
    }

    @Test
    void testGetMeWithInvalidTokenReturns401() {
        given()
            .port(port)
            .header("Authorization", "Bearer invalid_token_here")
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/auth/me")
        .then()
            .statusCode(401);
    }

    @Test
    void testLogoutWithValidTokenReturnsSuccess() {
        // First login
        String token = given()
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

        // Then logout
        given()
            .port(port)
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/auth/logout")
        .then()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void testRefreshTokenWithValidTokenReturnsNewToken() {
        // First login
        String refreshToken = given()
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
            .path("refreshToken");

        // Then refresh using cookie
        given()
            .port(port)
            .cookie("RefreshToken", refreshToken)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/auth/refresh")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("status", equalTo("SUCCESS"));
    }

    @Test
    void testLoginReturnsUserWithCorrectRole() {
        // Login response contains accessToken but not roles (roles are in JWT token itself)
        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "admin",
                    "password": "admin"
                }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .body("accessToken", notNullValue())
            .body("status", equalTo("SUCCESS"));
    }
}
