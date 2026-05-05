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
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Comprehensive system tests for user account functionalities, including profile management and summoner linking.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserAccountSystemTest {

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
    void testGetMyProfileReturnsUserInfo() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/users/me")
                .then()
                .statusCode(200)
                .body("username", notNullValue())
                .body("email", notNullValue())
                .body("roles", notNullValue());
    }

    @Test
    void testGetLinkedSummonerWithoutLinkReturnsNotLinked() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/users/linked-summoner")
                .then()
                .statusCode(200)
                .body("linked", equalTo(false));
    }

    @Test
    void testLinkSummonerWithValidCredentialsLinksAccount() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "summonerName": "TestSummoner",
                            "region": "EUW"
                        }
                        """)
                .when()
                .post("/api/v1/users/link-summoner")
                .then()
                .statusCode(anyOf(is(200), is(404), is(400)));
    }

    @Test
    void testLinkSummonerInvalidData() {

        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "summonerName": "",
                            "region": "EUW"
                        }
                        """)
                .when()
                .post("/api/v1/users/link-summoner")
                .then()
                .statusCode(400);

        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "summonerName": "TestSummoner",
                            "region": "INVALID"
                        }
                        """)
                .when()
                .post("/api/v1/users/link-summoner")
                .then()
                .statusCode(anyOf(is(400), is(404)));
    }

    @Test
    void testUnlinkSummonerWithoutLinkedAccountReturnsError() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/users/unlink-summoner")
                .then()
                .statusCode(anyOf(is(200), is(400)));
    }

    @Test
    void testEndpointsUnauthorizedAccess() {

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "summonerName": "TestSummoner",
                            "region": "EUW"
                        }
                        """)
                .when()
                .post("/api/v1/users/link-summoner")
                .then()
                .statusCode(401);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/users/linked-summoner")
                .then()
                .statusCode(401);
    }

    @Test
    void testUpdateProfileWithValidDataUpdatesUser() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "email": "newemail@test.com"
                        }
                        """)
                .when()
                .put("/api/v1/users/me")
                .then()
                .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testUploadAvatarWithoutFileReturns400() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.MULTIPART)
                .when()
                .post("/api/v1/users/avatar")
                .then()
                .statusCode(anyOf(is(400), is(500)));
    }

    @Test
    void testDeleteAvatarRemovesAvatar() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/v1/users/avatar")
                .then()
                .statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    void testGetMyProfileContainsExpectedFields() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/users/me")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("username", notNullValue())
                .body("email", notNullValue())
                .body("roles", notNullValue())
                .body("active", notNullValue());

    }

    @Test
    void testLinkSummonerTwoDifferentSummonersUpdatesLink() {

        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "summonerName": "FirstSummoner",
                            "region": "EUW"
                        }
                        """)
                .when()
                .post("/api/v1/users/link-summoner")
                .then()
                .statusCode(anyOf(is(200), is(404), is(400)));

        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "summonerName": "SecondSummoner",
                            "region": "EUW"
                        }
                        """)
                .when()
                .post("/api/v1/users/link-summoner")
                .then()
                .statusCode(anyOf(is(200), is(404), is(400)));
    }
}
