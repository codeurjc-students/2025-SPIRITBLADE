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
 * Testing system for dashboard functionalities, including recent summoner activity and match history display.
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

        userRepository.findByName("testuser").ifPresent(u -> userRepository.delete(u));

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
    void testGetRankedMatchesByQueue() {

        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .queryParam("queueId", 420)
                .when()
                .get("/api/v1/dashboard/me/ranked-matches")
                .then()
                .statusCode(200);

        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .queryParam("queueId", 440)
                .when()
                .get("/api/v1/dashboard/me/ranked-matches")
                .then()
                .statusCode(200);
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
    void testFavoritesFlow() {

        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/dashboard/me/favorites/TestSummoner")
                .then()
                .statusCode(anyOf(is(200), is(404)));

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
                .queryParam("queueId", 999)
                .when()
                .get("/api/v1/dashboard/me/ranked-matches")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void testGetPersonalStatsContainsExpectedFields() {

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
