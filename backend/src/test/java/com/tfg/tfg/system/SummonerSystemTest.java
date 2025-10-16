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
class SummonerSystemTest {

    @LocalServerPort
    private int port;

    @BeforeAll
    static void setup() {
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    void testGetAllSummoners_ReturnsSeededData() {
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
}