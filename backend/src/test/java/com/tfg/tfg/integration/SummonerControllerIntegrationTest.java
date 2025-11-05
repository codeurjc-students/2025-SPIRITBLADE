package com.tfg.tfg.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.tfg.model.dto.MatchDetailDTO;
import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.MatchRepository;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.service.DataDragonService;

/**
 * Strategic Integration Tests for SummonerController
 * Goal: Increase coverage from 59% to 80%+ by testing all branches
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SummonerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SummonerRepository summonerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @MockitoBean
    private RiotService riotService;

    @MockitoBean
    private DataDragonService dataDragonService;

    @BeforeEach
    void setup() {
        // Clean up in correct order (matches first due to foreign key)
        matchRepository.deleteAll();
        summonerRepository.deleteAll();
        
        // Mock DataDragonService
        when(riotService.getDataDragonService()).thenReturn(dataDragonService);
    }

    @Test
    @WithMockUser
    void testGetAllSummoners_EmptyDatabase() throws Exception {
        mockMvc.perform(get("/api/v1/summoners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)))
                .andExpect(jsonPath("$.totalPages", equalTo(0)));
    }

    @Test
    @WithMockUser
    void testGetAllSummoners_WithPagination() throws Exception {
        // Create 25 summoners
        for (int i = 0; i < 25; i++) {
            Summoner s = new Summoner();
            s.setName("Summoner" + i);
            s.setPuuid("puuid-" + i);
            s.setTier("GOLD");
            s.setRank("III");
            s.setLp(50 + i);
            s.setLastSearchedAt(LocalDateTime.now().minusDays(i));
            summonerRepository.save(s);
        }

        // Page 0 with size 20
        mockMvc.perform(get("/api/v1/summoners")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(20)))
                .andExpect(jsonPath("$.totalElements", equalTo(25)))
                .andExpect(jsonPath("$.totalPages", equalTo(2)))
                .andExpect(jsonPath("$.number", equalTo(0)))
                .andExpect(jsonPath("$.size", equalTo(20)));

        // Page 1 with size 20
        mockMvc.perform(get("/api/v1/summoners")
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", equalTo(25)))
                .andExpect(jsonPath("$.number", equalTo(1)));
    }

    @Test
    @WithMockUser
    void testGetAllSummoners_SortedByLastSearchedAt() throws Exception {
        Summoner oldest = new Summoner();
        oldest.setName("Oldest");
        oldest.setPuuid("puuid-old");
        oldest.setTier("GOLD");
        oldest.setLastSearchedAt(LocalDateTime.now().minusDays(10));
        summonerRepository.save(oldest);

        Summoner newest = new Summoner();
        newest.setName("Newest");
        newest.setPuuid("puuid-new");
        newest.setTier("PLATINUM");
        newest.setLastSearchedAt(LocalDateTime.now());
        summonerRepository.save(newest);

        mockMvc.perform(get("/api/v1/summoners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", equalTo("Newest")))
                .andExpect(jsonPath("$.content[1].name", equalTo("Oldest")));
    }

    @Test
    @WithMockUser
    void testGetRecentSearches_EmptyDatabase() throws Exception {
        mockMvc.perform(get("/api/v1/summoners/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser
    void testGetRecentSearches_WithSummoners() throws Exception {
        // Create 12 summoners (should return only 10 most recent)
        for (int i = 0; i < 12; i++) {
            Summoner s = new Summoner();
            s.setName("Recent" + i);
            s.setPuuid("puuid-" + i);
            s.setTier("DIAMOND");
            s.setLastSearchedAt(LocalDateTime.now().minusHours(i));
            summonerRepository.save(s);
        }

        mockMvc.perform(get("/api/v1/summoners/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].name", equalTo("Recent0"))) // Most recent
                .andExpect(jsonPath("$[9].name", equalTo("Recent9")));
    }

    @Test
    @WithMockUser
    void testGetRecentSearches_FiltersNullLastSearchedAt() throws Exception {
        // Create summoner WITH lastSearchedAt
        Summoner withDate = new Summoner();
        withDate.setName("WithDate");
        withDate.setPuuid("puuid-1");
        withDate.setTier("GOLD");
        withDate.setLastSearchedAt(LocalDateTime.now());
        summonerRepository.save(withDate);

        // Create summoner WITHOUT lastSearchedAt (should be filtered)
        Summoner withoutDate = new Summoner();
        withoutDate.setName("WithoutDate");
        withoutDate.setPuuid("puuid-2");
        withoutDate.setTier("GOLD");
        withoutDate.setLastSearchedAt(null);
        summonerRepository.save(withoutDate);

        mockMvc.perform(get("/api/v1/summoners/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo("WithDate")));
    }

    @Test
    void testGetSummonerByName_Success() throws Exception {
        SummonerDTO mockDTO = new SummonerDTO();
        mockDTO.setName("ProPlayer");
        mockDTO.setTier("CHALLENGER");
        mockDTO.setRank("I");
        mockDTO.setLp(1500);

        when(riotService.getSummonerByName("ProPlayer")).thenReturn(mockDTO);

        mockMvc.perform(get("/api/v1/summoners/name/ProPlayer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("ProPlayer")))
                .andExpect(jsonPath("$.tier", equalTo("CHALLENGER")))
                .andExpect(jsonPath("$.lp", equalTo(1500)));

        verify(riotService).getSummonerByName("ProPlayer");
    }

    @Test
    void testGetSummonerByName_NotFound() throws Exception {
        when(riotService.getSummonerByName("NonExistent")).thenReturn(null);

        mockMvc.perform(get("/api/v1/summoners/name/NonExistent"))
                .andExpect(status().isNotFound());

        verify(riotService).getSummonerByName("NonExistent");
    }

    @Test
    void testGetTopChampions_Success() throws Exception {
        SummonerDTO mockSummoner = new SummonerDTO();
        mockSummoner.setName("TestPlayer");
        mockSummoner.setPuuid("test-puuid-123");

        RiotChampionMasteryDTO mastery1 = new RiotChampionMasteryDTO();
        mastery1.setChampionName("Ahri");
        mastery1.setChampionLevel(7);
        mastery1.setChampionPoints(250000);

        RiotChampionMasteryDTO mastery2 = new RiotChampionMasteryDTO();
        mastery2.setChampionName("Zed");
        mastery2.setChampionLevel(6);
        mastery2.setChampionPoints(150000);

        when(riotService.getSummonerByName("TestPlayer")).thenReturn(mockSummoner);
        when(riotService.getTopChampionMasteries("test-puuid-123", 3))
                .thenReturn(List.of(mastery1, mastery2));

        mockMvc.perform(get("/api/v1/summoners/name/TestPlayer/masteries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].championName", equalTo("Ahri")))
                .andExpect(jsonPath("$[0].championLevel", equalTo(7)))
                .andExpect(jsonPath("$[1].championName", equalTo("Zed")));

        verify(riotService).getSummonerByName("TestPlayer");
        verify(riotService).getTopChampionMasteries("test-puuid-123", 3);
    }

    @Test
    void testGetTopChampions_SummonerNotFound() throws Exception {
        when(riotService.getSummonerByName("NonExistent")).thenReturn(null);

        mockMvc.perform(get("/api/v1/summoners/name/NonExistent/masteries"))
                .andExpect(status().isNotFound());

        verify(riotService).getSummonerByName("NonExistent");
        verify(riotService, never()).getTopChampionMasteries(anyString(), anyInt());
    }

    @Test
    void testGetTopChampions_SummonerWithoutPuuid() throws Exception {
        SummonerDTO mockSummoner = new SummonerDTO();
        mockSummoner.setName("NoPuuid");
        mockSummoner.setPuuid(null);

        when(riotService.getSummonerByName("NoPuuid")).thenReturn(mockSummoner);

        mockMvc.perform(get("/api/v1/summoners/name/NoPuuid/masteries"))
                .andExpect(status().isNotFound());

        verify(riotService).getSummonerByName("NoPuuid");
        verify(riotService, never()).getTopChampionMasteries(anyString(), anyInt());
    }

    @Test
    void testGetRecentMatches_Success() throws Exception {
        SummonerDTO mockSummoner = new SummonerDTO();
        mockSummoner.setName("TestPlayer");
        mockSummoner.setPuuid("test-puuid-123");

        MatchHistoryDTO match1 = new MatchHistoryDTO();
        match1.setMatchId("EUW1_123");
        match1.setChampionName("Ahri");

        MatchHistoryDTO match2 = new MatchHistoryDTO();
        match2.setMatchId("EUW1_124");
        match2.setChampionName("Zed");

        when(riotService.getSummonerByName("TestPlayer")).thenReturn(mockSummoner);
        when(riotService.getMatchHistory("test-puuid-123", 0, 5))
                .thenReturn(List.of(match1, match2));

        mockMvc.perform(get("/api/v1/summoners/name/TestPlayer/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].matchId", equalTo("EUW1_123")))
                .andExpect(jsonPath("$[1].matchId", equalTo("EUW1_124")));

        verify(riotService).getMatchHistory("test-puuid-123", 0, 5);
    }

    @Test
    void testGetRecentMatches_WithPagination() throws Exception {
        SummonerDTO mockSummoner = new SummonerDTO();
        mockSummoner.setName("TestPlayer");
        mockSummoner.setPuuid("test-puuid-123");

        when(riotService.getSummonerByName("TestPlayer")).thenReturn(mockSummoner);
        when(riotService.getMatchHistory("test-puuid-123", 10, 5))
                .thenReturn(List.of());

        // Page 2 with size 5 means start = 2 * 5 = 10
        mockMvc.perform(get("/api/v1/summoners/name/TestPlayer/matches")
                .param("page", "2")
                .param("size", "5"))
                .andExpect(status().isOk());

        verify(riotService).getMatchHistory("test-puuid-123", 10, 5);
    }

    @Test
    void testGetRecentMatches_SummonerNotFound() throws Exception {
        when(riotService.getSummonerByName("NonExistent")).thenReturn(null);

        mockMvc.perform(get("/api/v1/summoners/name/NonExistent/matches"))
                .andExpect(status().isNotFound());

        verify(riotService, never()).getMatchHistory(anyString(), anyInt(), anyInt());
    }

    @Test
    void testGetRecentMatches_SummonerWithoutPuuid() throws Exception {
        SummonerDTO mockSummoner = new SummonerDTO();
        mockSummoner.setName("NoPuuid");
        mockSummoner.setPuuid(null);

        when(riotService.getSummonerByName("NoPuuid")).thenReturn(mockSummoner);

        mockMvc.perform(get("/api/v1/summoners/name/NoPuuid/matches"))
                .andExpect(status().isNotFound());

        verify(riotService, never()).getMatchHistory(anyString(), anyInt(), anyInt());
    }

    @Test
    @WithMockUser
    void testGetMatchDetails_Success() throws Exception {
        MatchDetailDTO mockDetail = new MatchDetailDTO();
        mockDetail.setMatchId("EUW1_123456");
        mockDetail.setGameDuration(1850L);

        when(riotService.getMatchDetails("EUW1_123456")).thenReturn(mockDetail);

        mockMvc.perform(get("/api/v1/summoners/matches/EUW1_123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId", equalTo("EUW1_123456")))
                .andExpect(jsonPath("$.gameDuration", equalTo(1850)));

        verify(riotService).getMatchDetails("EUW1_123456");
    }

    @Test
    @WithMockUser
    void testGetMatchDetails_NotFound() throws Exception {
        when(riotService.getMatchDetails("EUW1_INVALID")).thenReturn(null);

        mockMvc.perform(get("/api/v1/summoners/matches/EUW1_INVALID"))
                .andExpect(status().isNotFound());

        verify(riotService).getMatchDetails("EUW1_INVALID");
    }

    @Test
    @WithMockUser
    void testGetById_Success() throws Exception {
        Summoner summoner = new Summoner();
        summoner.setName("FoundById");
        summoner.setPuuid("puuid-by-id");
        summoner.setTier("PLATINUM");
        summoner.setRank("II");
        summoner.setLp(75);
        Summoner saved = summonerRepository.save(summoner);

        mockMvc.perform(get("/api/v1/summoners/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("FoundById")))
                .andExpect(jsonPath("$.tier", equalTo("PLATINUM")));
    }

    @Test
    @WithMockUser
    void testGetById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/summoners/99999"))
                .andExpect(status().isNotFound());
    }
}
