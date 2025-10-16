package com.tfg.tfg.unit;

import com.tfg.tfg.controller.SummonerController;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.service.DataDragonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SummonerControllerSimpleUnitTest {

    @Mock
    private SummonerRepository summonerRepository;
    
    @Mock
    private RiotService riotService;

    @Mock
    private DataDragonService dataDragonService;

    private SummonerController summonerController;

    private Summoner testSummoner;

    @BeforeEach
    void setUp() {
        summonerController = new SummonerController(summonerRepository, riotService);
        
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

        // Mock DataDragonService
        when(riotService.getDataDragonService()).thenReturn(dataDragonService);
        when(dataDragonService.getProfileIconUrl(1234)).thenReturn("http://test.url/icon/1234.png");
    }

    @Test
    void testGetAllSummoners_Success() {
        // Given
        org.springframework.data.domain.Page<Summoner> summonersPage = 
            new org.springframework.data.domain.PageImpl<>(Arrays.asList(testSummoner));
        when(summonerRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(summonersPage);

        // When
        ResponseEntity<org.springframework.data.domain.Page<SummonerDTO>> result = 
            summonerController.getAllSummoners(0, 20);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
        
        SummonerDTO dto = result.getBody().getContent().get(0);
        assertEquals(testSummoner.getId(), dto.getId());
        assertEquals(testSummoner.getName(), dto.getName());
        assertEquals(testSummoner.getLevel(), dto.getLevel());
        assertEquals(testSummoner.getTier(), dto.getTier());
        assertEquals(testSummoner.getRank(), dto.getRank());
        assertEquals(testSummoner.getLp(), dto.getLp());
        
        verify(summonerRepository).findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void testGetAllSummoners_EmptyList() {
        // Given
        org.springframework.data.domain.Page<Summoner> emptyPage = 
            new org.springframework.data.domain.PageImpl<>(Collections.emptyList());
        when(summonerRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(emptyPage);

        // When
        ResponseEntity<org.springframework.data.domain.Page<SummonerDTO>> result = 
            summonerController.getAllSummoners(0, 20);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getContent().isEmpty());
        verify(summonerRepository).findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void testGetSummonerByName_Found() {
        // Given
        String summonerName = "TestSummoner";
        SummonerDTO expectedDto = new SummonerDTO();
        expectedDto.setId(testSummoner.getId());
        expectedDto.setRiotId(testSummoner.getRiotId());
        expectedDto.setName(testSummoner.getName());
        expectedDto.setLevel(testSummoner.getLevel());
        expectedDto.setProfileIconId(testSummoner.getProfileIconId());
        expectedDto.setTier(testSummoner.getTier());
        expectedDto.setRank(testSummoner.getRank());
        expectedDto.setLp(testSummoner.getLp());
        
        when(riotService.getSummonerByName(summonerName)).thenReturn(expectedDto);

        // When
        ResponseEntity<SummonerDTO> result = summonerController.getSummoner(summonerName);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testSummoner.getId(), result.getBody().getId());
        assertEquals(testSummoner.getName(), result.getBody().getName());
        verify(riotService).getSummonerByName(summonerName);
    }

    @Test
    void testGetSummonerByName_NotFound() {
        // Given
        String summonerName = "NonExistentSummoner";
        when(riotService.getSummonerByName(summonerName)).thenReturn(null);

        // When
        ResponseEntity<SummonerDTO> result = summonerController.getSummoner(summonerName);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(riotService).getSummonerByName(summonerName);
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

    // Tests for getMatches() and getChampionStats() endpoints removed - these endpoints no longer exist
    // Match and champion stats data is now fetched from Riot API in real-time, not stored locally

    @Test
    void testGetAllSummoners_WithPagination() {
        // Given
        Summoner summoner2 = new Summoner();
        summoner2.setId(2L);
        summoner2.setName("Summoner2");
        
        Page<Summoner> summonersPage = new PageImpl<>(
            Arrays.asList(testSummoner, summoner2),
            org.springframework.data.domain.PageRequest.of(0, 2),
            2
        );
        when(summonerRepository.findAll(org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(summonersPage);

        // When
        ResponseEntity<Page<SummonerDTO>> result = summonerController.getAllSummoners(0, 2);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().getContent().size());
        assertEquals(2, result.getBody().getTotalElements());
        assertEquals(1, result.getBody().getTotalPages());
    }

    @Test
    void testGetAllSummoners_SecondPage() {
        // Given
        Page<Summoner> emptyPage = new PageImpl<>(
            Collections.emptyList(),
            org.springframework.data.domain.PageRequest.of(1, 20),
            1
        );
        when(summonerRepository.findAll(org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(emptyPage);

        // When
        ResponseEntity<Page<SummonerDTO>> result = summonerController.getAllSummoners(1, 20);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getContent().isEmpty());
        assertEquals(1, result.getBody().getTotalElements());
    }
}