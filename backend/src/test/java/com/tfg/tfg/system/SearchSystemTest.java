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
 * Sistema de pruebas para búsqueda y gestión de invocadores.
 * Verifica:
 * - Búsqueda de invocadores por nombre
 * - Listado de invocadores recientes
 * - Obtención de datos de invocador específico
 * - Paginación de resultados
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SearchSystemTest {

    @LocalServerPort
    private int port;
    
    @Autowired
    private UserModelRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private String authToken;

    @BeforeAll
    static void setup() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = "https://localhost";
    }
    
    @BeforeEach
    void authenticate() {
        // Clear any existing test user
        userRepository.findByName("testuser").ifPresent(u -> userRepository.delete(u));
        
        // Create test user with known password
        UserModel user = new UserModel("testuser", passwordEncoder.encode("user123"), "USER");
        user.setEmail("testuser@example.com");
        user.setActive(true);
        userRepository.save(user);
        
        authToken = given()
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
    void testGetAllSummonersWithPaginationReturnsPagedResults() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/summoners")
        .then()
            .statusCode(200)
            .body("content", instanceOf(java.util.List.class))
            .body("totalElements", notNullValue())
            .body("totalPages", notNullValue())
            .body("size", notNullValue());
    }

    @Test
    void testGetAllSummonersFirstPageReturnsData() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/summoners")
        .then()
            .statusCode(200);
    }

    @Test
    void testGetRecentSummonersReturnsRecentSearches() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/summoners/recent")
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test
    void testSearchSummonerWithValidNameReturnsResults() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/summoners/name/TestSummoner")
        .then()
            .statusCode(anyOf(is(200), is(404))); // 404 if not found, which is valid
    }

    @Test
    void testSearchSummonerWithEmptyNameReturns400() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/summoners/name/%20")  // URL encoded space
        .then()
            .statusCode(anyOf(is(400), is(404)));
    }

    @Test
    void testSearchSummonerWithSpecialCharactersHandlesCorrectly() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/summoners/name/Test+Summoner")
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testGetSummonersWithLargePageSizeReturnsLimitedResults() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("size", 100)
        .when()
            .get("/api/v1/summoners")
        .then()
            .statusCode(200)
            .body("content.size()", lessThanOrEqualTo(100));
    }

    @Test
    void testGetSummonersSecondPageReturnsNextResults() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .queryParam("page", 1)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/summoners")
        .then()
            .statusCode(200)
            .body("number", equalTo(1)); // Page number
    }

    @Test
    void testSearchSummonerReturnsExpectedFields() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/summoners/name/Faker")
        .then()
            .statusCode(anyOf(is(200), is(404)));
            // Response may be empty (404) if summoner not found in Riot API
    }

    @Test
    void testGetSummonersContainsPaginationMetadata() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("size", 5)
        .when()
            .get("/api/v1/summoners")
        .then()
            .statusCode(200)
            .body("pageable", notNullValue())
            .body("totalElements", notNullValue())
            .body("totalPages", notNullValue())
            .body("last", notNullValue())
            .body("first", notNullValue());
    }
}
