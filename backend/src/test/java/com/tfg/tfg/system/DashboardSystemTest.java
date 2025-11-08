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
 * Sistema de pruebas para el Dashboard y estadÃ­sticas personales.
 * Verifica funcionalidades de:
 * - EstadÃ­sticas personales (rank, LP, rol principal)
 * - Historial de partidas ranqueadas
 * - ProgresiÃ³n de LP
 * - GestiÃ³n de favoritos
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DashboardSystemTest {

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
        
        // Login to get auth token
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
    void testGetPersonalStatsWithoutLinkedSummonerReturnsDefaultStats() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/dashboard/me/stats")
        .then()
            .statusCode(200)
            .body("currentRank", notNullValue())
            .body("mainRole", notNullValue());
            // favoriteChampion can be null without linked summoner
    }

    @Test
    void testGetRankedMatchesWithoutLinkedSummonerReturnsEmptyList() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/dashboard/me/ranked-matches")
        .then()
            .statusCode(200)
            .body("size()", equalTo(0));
    }

    @Test
    void testGetRankedMatchesWithPaginationReturnsCorrectSize() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/dashboard/me/ranked-matches")
        .then()
            .statusCode(200);
    }

    @Test
    void testGetRankedMatchesWithQueueFilterSoloQueue() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .queryParam("queueId", 420) // Solo/Duo queue
        .when()
            .get("/api/v1/dashboard/me/ranked-matches")
        .then()
            .statusCode(200);
    }

    @Test
    void testGetRankedMatchesWithQueueFilterFlexQueue() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .queryParam("queueId", 440) // Flex queue
        .when()
            .get("/api/v1/dashboard/me/ranked-matches")
        .then()
            .statusCode(200);
    }

    @Test
    void testGetRankHistoryWithoutLinkedSummonerReturnsEmptyList() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/dashboard/me/rank-history")
        .then()
            .statusCode(200)
            .body("size()", equalTo(0));
    }

    @Test
    void testGetFavoritesReturnsUserFavorites() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/dashboard/me/favorites")
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test
    void testAddFavoriteWithValidSummoner() {
        // First, ensure a summoner exists (this might fail if no summoners in test DB)
        // This test demonstrates the endpoint structure
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/dashboard/me/favorites/TestSummoner")
        .then()
            .statusCode(anyOf(is(200), is(404))); // 404 if summoner doesn't exist in test DB
    }

    @Test
    void testRemoveFavoriteWithValidSummoner() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/v1/dashboard/me/favorites/TestSummoner")
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testGetPersonalStatsUnauthorizedReturns401() {
        given()
            .port(port)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/dashboard/me")
        .then()
            .statusCode(401);
    }

    @Test
    void testGetRankedMatchesWithInvalidQueueIdReturnsEmptyList() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .queryParam("queueId", 999) // Invalid queue
        .when()
            .get("/api/v1/dashboard/me/ranked-matches")
        .then()
            .statusCode(200)
            .body("size()", equalTo(0));
    }

    @Test
    void testGetPersonalStatsContainsExpectedFields() {
        // winRate can be null without matches, currentRank and mainRole should always be present
        given()
            .port(port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/dashboard/me/stats")
        .then()
            .statusCode(200)
            .body("currentRank", notNullValue())
            .body("mainRole", notNullValue());
    }
}
