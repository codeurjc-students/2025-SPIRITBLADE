package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tfg.tfg.controller.DashboardController;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.entity.ChampionStat;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.repository.UserModelRepository;

@ExtendWith(MockitoExtension.class)
class DashboardControllerSimpleUnitTest {

    @Mock
    private SummonerRepository summonerRepository;

    @Mock
    private UserModelRepository userModelRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        dashboardController = new DashboardController(summonerRepository, userModelRepository);
    }

    @Test
    void testMyStatsWithNoSummoners() {
        when(summonerRepository.findAll()).thenReturn(List.of());

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("testuser");
            when(authentication.getName()).thenReturn("testuser");

            ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> result = response.getBody();
            assertNotNull(result);
            assertEquals("Unranked", result.get("currentRank"));
            assertEquals(0, result.get("lp7days"));
            assertEquals("Unknown", result.get("mainRole"));
            assertNull(result.get("favoriteChampion"));
            assertEquals("testuser", result.get("username"));
        }
    }

    @Test
    void testMyStatsWithSummoner() {
        Summoner summoner = new Summoner();
        summoner.setId(1L);
        summoner.setName("TestSummoner");
        summoner.setTier("Gold");
        summoner.setRank("II");

        ChampionStat championStat = new ChampionStat();
        championStat.setChampionId(101);
        summoner.setChampionStats(List.of(championStat));

        when(summonerRepository.findAll()).thenReturn(List.of(summoner));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("testuser");
            when(authentication.getName()).thenReturn("testuser");

            ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> result = response.getBody();
            assertNotNull(result);
            assertEquals("Gold II", result.get("currentRank"));
            assertEquals(42, result.get("lp7days"));
            assertEquals("Mid Lane", result.get("mainRole"));
            assertEquals(101, result.get("favoriteChampion"));
            assertEquals("testuser", result.get("username"));
        }
    }

    @Test
    void testMyStatsWithUnrankedSummoner() {
        Summoner summoner = new Summoner();
        summoner.setId(1L);
        summoner.setName("TestSummoner");
        summoner.setTier(null);
        summoner.setRank(null);
        summoner.setChampionStats(List.of());

        when(summonerRepository.findAll()).thenReturn(List.of(summoner));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> result = response.getBody();
            assertNotNull(result);
            assertEquals("Unranked", result.get("currentRank"));
            assertEquals("Guest", result.get("username"));
        }
    }

    @Test
    void testMyFavoritesWithNoSummoners() {
        when(summonerRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<SummonerDTO>> response = dashboardController.myFavorites();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<SummonerDTO> result = response.getBody();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testMyFavoritesWithMultipleSummoners() {
        Summoner summoner1 = new Summoner();
        summoner1.setId(1L);
        summoner1.setName("Summoner1");
        summoner1.setRiotId("riot1");
        summoner1.setTier("Gold");
        summoner1.setRank("II");

        Summoner summoner2 = new Summoner();
        summoner2.setId(2L);
        summoner2.setName("Summoner2");
        summoner2.setRiotId("riot2");
        summoner2.setTier("Silver");
        summoner2.setRank("I");

        Summoner summoner3 = new Summoner();
        summoner3.setId(3L);
        summoner3.setName("Summoner3");
        summoner3.setRiotId("riot3");
        summoner3.setTier("Bronze");
        summoner3.setRank("III");

        when(summonerRepository.findAll()).thenReturn(List.of(summoner1, summoner2, summoner3));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            ResponseEntity<List<SummonerDTO>> response = dashboardController.myFavorites();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<SummonerDTO> result = response.getBody();
            assertNotNull(result);
            assertEquals(2, result.size()); // Should return max 2 favorites
        }
    }

    @Test
    void testMyFavoritesWithAuthenticatedUser() {
        UserModel user = new UserModel();
        user.setName("testuser");

        Summoner ownSummoner = new Summoner();
        ownSummoner.setId(1L);
        ownSummoner.setName("testuser"); // Same name as user

        Summoner otherSummoner = new Summoner();
        otherSummoner.setId(2L);
        otherSummoner.setName("OtherSummoner");

        when(summonerRepository.findAll()).thenReturn(List.of(ownSummoner, otherSummoner));
        when(userModelRepository.findByName("testuser")).thenReturn(Optional.of(user));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("testuser");
            when(authentication.getName()).thenReturn("testuser");

            ResponseEntity<List<SummonerDTO>> response = dashboardController.myFavorites();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<SummonerDTO> result = response.getBody();
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("OtherSummoner", result.get(0).getName());
        }
    }

    @Test
    void testMyStatsWithSecurityException() {
        Summoner summoner = new Summoner();
        summoner.setId(1L);
        summoner.setName("TestSummoner");
        summoner.setChampionStats(List.of());

        when(summonerRepository.findAll()).thenReturn(List.of(summoner));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenThrow(new RuntimeException("Security error"));

            ResponseEntity<Map<String, Object>> response = dashboardController.myStats();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> result = response.getBody();
            assertNotNull(result);
            assertEquals("Guest", result.get("username"));
            assertNull(result.get("linkedSummoner"));
        }
    }
}