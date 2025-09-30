package com.tfg.tfg.unit;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.tfg.tfg.controller.DashboardController;
import com.tfg.tfg.model.entity.ChampionStat;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.repository.UserModelRepository;

@WebMvcTest(DashboardController.class)
class DashboardControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SummonerRepository summonerRepository;

    @MockBean
    private UserModelRepository userModelRepository;

    private Summoner testSummoner;
    private UserModel testUser;

    @BeforeEach
    void setUp() {
        testSummoner = new Summoner();
        testSummoner.setId(1L);
        testSummoner.setName("TestSummoner");
        testSummoner.setTier("GOLD");
        testSummoner.setRank("II");

        ChampionStat championStat = new ChampionStat();
        championStat.setChampionId(1);
        testSummoner.setChampionStats(List.of(championStat));

        testUser = new UserModel("testUser", "password", "USER");
        testUser.setId(1L);
    }

    @Test
    @WithMockUser(username = "testUser")
    void testMyStatsWithSummoner() throws Exception {
        when(summonerRepository.findAll()).thenReturn(List.of(testSummoner));
        when(userModelRepository.findByName("testUser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/v1/dashboard/me/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRank").value("GOLD II"))
                .andExpect(jsonPath("$.lp7days").value(42))
                .andExpect(jsonPath("$.mainRole").value("Mid Lane"))
                .andExpect(jsonPath("$.favoriteChampion").value(1))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testMyStatsWithoutSummoner() throws Exception {
        when(summonerRepository.findAll()).thenReturn(List.of());
        when(userModelRepository.findByName("testUser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/v1/dashboard/me/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRank").value("Unranked"))
                .andExpect(jsonPath("$.lp7days").value(0))
                .andExpect(jsonPath("$.mainRole").value("Unknown"))
                .andExpect(jsonPath("$.favoriteChampion").doesNotExist())
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testMyStatsWithUnrankedSummoner() throws Exception {
        testSummoner.setTier(null);
        testSummoner.setRank(null);
        when(summonerRepository.findAll()).thenReturn(List.of(testSummoner));
        when(userModelRepository.findByName("testUser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/v1/dashboard/me/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRank").value("Unranked"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testMyFavoritesWithMultipleSummoners() throws Exception {
        Summoner summoner2 = new Summoner();
        summoner2.setId(2L);
        summoner2.setName("TestSummoner2");
        summoner2.setTier("SILVER");
        summoner2.setRank("I");
        summoner2.setRiotId("RIOT2");

        Summoner summoner3 = new Summoner();
        summoner3.setId(3L);
        summoner3.setName("TestSummoner3");
        summoner3.setTier("BRONZE");
        summoner3.setRank("III");
        summoner3.setRiotId("RIOT3");

        when(summonerRepository.findAll()).thenReturn(List.of(testSummoner, summoner2, summoner3));
        when(userModelRepository.findByName("testUser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/v1/dashboard/me/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("TestSummoner2"))
                .andExpect(jsonPath("$[1].name").value("TestSummoner3"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testMyFavoritesWithNoSummoners() throws Exception {
        when(summonerRepository.findAll()).thenReturn(List.of());
        when(userModelRepository.findByName("testUser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/v1/dashboard/me/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testMyFavoritesWithOnlyOneSummoner() throws Exception {
        when(summonerRepository.findAll()).thenReturn(List.of(testSummoner));
        when(userModelRepository.findByName("testUser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/v1/dashboard/me/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testMyStatsWithoutAuthentication() throws Exception {
        when(summonerRepository.findAll()).thenReturn(List.of(testSummoner));

        mockMvc.perform(get("/api/v1/dashboard/me/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Guest"));
    }
}