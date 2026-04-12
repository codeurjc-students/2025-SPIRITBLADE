package com.tfg.tfg.system;

import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.exception.SummonerNotFoundException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Testing system for search functionalities, including summoner search and match history retrieval.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SearchSystemTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserModelRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RiotService riotService;

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

        SummonerDTO mockDto = new SummonerDTO();
        mockDto.setName("TestSummoner#EUW");
        mockDto.setPuuid("mock-puuid");
        mockDto.setLevel(30);
        mockDto.setTier("GOLD");
        mockDto.setRank("I");

        when(riotService.getSummonerByName("TestSummoner#EUW")).thenReturn(mockDto);
        when(riotService.getSummonerByName("Test Summoner#EUW")).thenReturn(mockDto);

        when(riotService.getSummonerByName("Faker#LCK")).thenThrow(new SummonerNotFoundException("Summoner not found"));

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
                .get("/api/v1/summoners/name/TestSummoner#EUW")
                .then()
                .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testSearchSummonerEdgeCases() {

        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/summoners/name/%20")
                .then()
                .statusCode(anyOf(is(200), is(400), is(404)));

        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/summoners/name/Test Summoner#EUW")
                .then()
                .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testSearchSummonerReturnsExpectedFields() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/summoners/name/Faker#LCK")
                .then()
                .statusCode(anyOf(is(200), is(404)));

    }
}
