package com.tfg.tfg.unit;

import com.tfg.tfg.controller.SummonerController;
import com.tfg.tfg.model.dto.ChampionStatDTO;
import com.tfg.tfg.model.dto.MatchDTO;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.entity.ChampionStat;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummonerControllerSimpleUnitTest {

    @Mock
    private SummonerRepository summonerRepository;

    private SummonerController summonerController;

    private Summoner testSummoner;
    private MatchEntity testMatch;
    private ChampionStat testChampionStat;

    @BeforeEach
    void setUp() {
        summonerController = new SummonerController(summonerRepository);
        
        // Setup test entities
        testSummoner = new Summoner();
        testSummoner.setId(1L);
        testSummoner.setRiotId("testRiotId");
        testSummoner.setName("TestSummoner");
        testSummoner.setLevel(30);
        testSummoner.setProfileIconId(1234);
        testSummoner.setTier("GOLD");
        testSummoner.setRank("II");
        testSummoner.setLp(1500);

        testMatch = new MatchEntity();
        testMatch.setId(1L);
        testMatch.setMatchId("MATCH123");
        testMatch.setTimestamp(LocalDateTime.now());
        testMatch.setWin(true);
        testMatch.setKills(10);
        testMatch.setDeaths(3);
        testMatch.setAssists(15);

        testChampionStat = new ChampionStat();
        testChampionStat.setId(1L);
        testChampionStat.setChampionId(123);
        testChampionStat.setGamesPlayed(25);
        testChampionStat.setWins(18);
        testChampionStat.setKills(150);
        testChampionStat.setDeaths(75);
        testChampionStat.setAssists(200);

        testSummoner.setMatches(Arrays.asList(testMatch));
        testSummoner.setChampionStats(Arrays.asList(testChampionStat));
    }

    @Test
    void testGetAllSummoners_Success() {
        // Given
        List<Summoner> summoners = Arrays.asList(testSummoner);
        when(summonerRepository.findAll()).thenReturn(summoners);

        // When
        ResponseEntity<List<SummonerDTO>> result = summonerController.getAllSummoners();

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        
        SummonerDTO dto = result.getBody().get(0);
        assertEquals(testSummoner.getId(), dto.getId());
        assertEquals(testSummoner.getName(), dto.getName());
        assertEquals(testSummoner.getLevel(), dto.getLevel());
        assertEquals(testSummoner.getTier(), dto.getTier());
        assertEquals(testSummoner.getRank(), dto.getRank());
        assertEquals(testSummoner.getLp(), dto.getLp());
        
        verify(summonerRepository).findAll();
    }

    @Test
    void testGetAllSummoners_EmptyList() {
        // Given
        when(summonerRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<SummonerDTO>> result = summonerController.getAllSummoners();

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
        verify(summonerRepository).findAll();
    }

    @Test
    void testGetSummonerByName_Found() {
        // Given
        String summonerName = "TestSummoner";
        when(summonerRepository.findByName(summonerName)).thenReturn(Optional.of(testSummoner));

        // When
        ResponseEntity<SummonerDTO> result = summonerController.getSummoner(summonerName);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testSummoner.getId(), result.getBody().getId());
        assertEquals(testSummoner.getName(), result.getBody().getName());
        verify(summonerRepository).findByName(summonerName);
    }

    @Test
    void testGetSummonerByName_NotFound() {
        // Given
        String summonerName = "NonExistentSummoner";
        when(summonerRepository.findByName(summonerName)).thenReturn(Optional.empty());

        // When
        ResponseEntity<SummonerDTO> result = summonerController.getSummoner(summonerName);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(summonerRepository).findByName(summonerName);
    }

    @Test
    void testGetById_Found() {
        // Given
        Long summonerId = 1L;
        when(summonerRepository.findById(summonerId)).thenReturn(Optional.of(testSummoner));

        // When
        ResponseEntity<SummonerDTO> result = summonerController.getById(summonerId);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testSummoner.getId(), result.getBody().getId());
        assertEquals(testSummoner.getName(), result.getBody().getName());
        verify(summonerRepository).findById(summonerId);
    }

    @Test
    void testGetById_NotFound() {
        // Given
        Long summonerId = 999L;
        when(summonerRepository.findById(summonerId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<SummonerDTO> result = summonerController.getById(summonerId);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(summonerRepository).findById(summonerId);
    }

    @Test
    void testGetMatches_Found() {
        // Given
        Long summonerId = 1L;
        when(summonerRepository.findById(summonerId)).thenReturn(Optional.of(testSummoner));

        // When
        ResponseEntity<List<MatchDTO>> result = summonerController.getMatches(summonerId);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        
        MatchDTO matchDTO = result.getBody().get(0);
        assertEquals(testMatch.getId(), matchDTO.getId());
        assertEquals(testMatch.getMatchId(), matchDTO.getMatchId());
        assertEquals(testMatch.isWin(), matchDTO.isWin());
        assertEquals(testMatch.getKills(), matchDTO.getKills());
        assertEquals(testMatch.getDeaths(), matchDTO.getDeaths());
        assertEquals(testMatch.getAssists(), matchDTO.getAssists());
        
        verify(summonerRepository).findById(summonerId);
    }

    @Test
    void testGetMatches_SummonerNotFound() {
        // Given
        Long summonerId = 999L;
        when(summonerRepository.findById(summonerId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<List<MatchDTO>> result = summonerController.getMatches(summonerId);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(summonerRepository).findById(summonerId);
    }

    @Test
    void testGetChampionStats_Found() {
        // Given
        Long summonerId = 1L;
        when(summonerRepository.findById(summonerId)).thenReturn(Optional.of(testSummoner));

        // When
        ResponseEntity<List<ChampionStatDTO>> result = summonerController.getChampionStats(summonerId);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        
        ChampionStatDTO championStatDTO = result.getBody().get(0);
        assertEquals(testChampionStat.getId(), championStatDTO.getId());
        assertEquals(testChampionStat.getChampionId(), championStatDTO.getChampionId());
        assertEquals(testChampionStat.getGamesPlayed(), championStatDTO.getGamesPlayed());
        assertEquals(testChampionStat.getWins(), championStatDTO.getWins());
        assertEquals(testChampionStat.getKills(), championStatDTO.getKills());
        assertEquals(testChampionStat.getDeaths(), championStatDTO.getDeaths());
        assertEquals(testChampionStat.getAssists(), championStatDTO.getAssists());
        
        verify(summonerRepository).findById(summonerId);
    }

    @Test
    void testGetChampionStats_SummonerNotFound() {
        // Given
        Long summonerId = 999L;
        when(summonerRepository.findById(summonerId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<List<ChampionStatDTO>> result = summonerController.getChampionStats(summonerId);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(summonerRepository).findById(summonerId);
    }

    @Test
    void testGetMatches_EmptyMatches() {
        // Given
        Long summonerId = 1L;
        testSummoner.setMatches(Collections.emptyList());
        when(summonerRepository.findById(summonerId)).thenReturn(Optional.of(testSummoner));

        // When
        ResponseEntity<List<MatchDTO>> result = summonerController.getMatches(summonerId);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
        verify(summonerRepository).findById(summonerId);
    }

    @Test
    void testGetChampionStats_EmptyChampionStats() {
        // Given
        Long summonerId = 1L;
        testSummoner.setChampionStats(Collections.emptyList());
        when(summonerRepository.findById(summonerId)).thenReturn(Optional.of(testSummoner));

        // When
        ResponseEntity<List<ChampionStatDTO>> result = summonerController.getChampionStats(summonerId);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
        verify(summonerRepository).findById(summonerId);
    }
}