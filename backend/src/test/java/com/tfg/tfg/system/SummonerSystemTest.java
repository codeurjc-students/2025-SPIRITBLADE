package com.tfg.tfg.system;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Sistema de pruebas de la API REST para la entidad principal (Summoner).
 * Verifica que los datos de ejemplo se recuperan correctamente.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SummonerSystemTest {

    @LocalServerPort
    private int port;

    @BeforeAll
    public static void setup() {
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void testGetAllSummoners_ReturnsSeededData() {
        given()
            .port(port)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/summoners")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].name", notNullValue())
            .body("[0].riotId", notNullValue());
    }

    @Test
    public void testGetSummonerByName_ReturnsCorrectData() {
        // First get all summoners to find a valid name
        String summonerName = given()
            .port(port)
        .when()
            .get("/api/v1/summoners")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .extract()
            .path("[0].name");

        // Then test getting specific summoner by name
        given()
            .port(port)
            .pathParam("name", summonerName)
        .when()
            .get("/api/v1/summoners/name/{name}")
        .then()
            .statusCode(200)
            .body("name", equalTo(summonerName))
            .body("riotId", notNullValue())
            .body("level", notNullValue());
    }

    @Test
    public void testGetSummonerMatches_ReturnsMatchData() {
        // Get first summoner ID
        Integer summonerId = given()
            .port(port)
        .when()
            .get("/api/v1/summoners")
        .then()
            .statusCode(200)
            .extract()
            .path("[0].id");

        // Test getting matches for that summoner
        given()
            .port(port)
            .pathParam("id", summonerId)
        .when()
            .get("/api/v1/summoners/{id}/matches")
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test
    public void testGetSummonerChampionStats_ReturnsStatsData() {
        // Get first summoner ID
        Integer summonerId = given()
            .port(port)
        .when()
            .get("/api/v1/summoners")
        .then()
            .statusCode(200)
            .extract()
            .path("[0].id");

        // Test getting champion stats for that summoner
        given()
            .port(port)
            .pathParam("id", summonerId)
        .when()
            .get("/api/v1/summoners/{id}/champion-stats")
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }
}