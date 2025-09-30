package com.tfg.tfg.unit;

import com.tfg.tfg.controller.SummonerController;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del controlador Summoner con dobles del repositorio.
 */
@ExtendWith(MockitoExtension.class)
class SummonerControllerUnitTest {

    @Mock
    private SummonerRepository summonerRepository;

    @InjectMocks
    private SummonerController summonerController;

    @Test
    void testGetAllSummoners_ReturnsListOfSummoners() {
        // Arrange
        Summoner summoner1 = new Summoner("riot-1", "TestSummoner1", 50);
        summoner1.setTier("Gold");
        Summoner summoner2 = new Summoner("riot-2", "TestSummoner2", 75);
        summoner2.setTier("Platinum");
        
        List<Summoner> mockSummoners = Arrays.asList(summoner1, summoner2);
        when(summonerRepository.findAll()).thenReturn(mockSummoners);

        // Act
        ResponseEntity<?> response = summonerController.getAllSummoners();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(summonerRepository, times(1)).findAll();
    }

    @Test
    void testGetSummonerByName_ExistingSummoner_Returns200() {
        // Arrange
        String summonerName = "ExistingSummoner";
        Summoner mockSummoner = new Summoner("riot-existing", summonerName, 85);
        mockSummoner.setRank("III");
        
        when(summonerRepository.findByName(summonerName))
            .thenReturn(Optional.of(mockSummoner));

        // Act
        ResponseEntity<?> response = summonerController.getSummoner(summonerName);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(summonerRepository, times(1)).findByName(summonerName);
    }

    @Test
    void testGetSummonerByName_NonExistentSummoner_Returns404() {
        // Arrange
        String summonerName = "NonExistentSummoner";
        when(summonerRepository.findByName(anyString()))
            .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = summonerController.getSummoner(summonerName);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        verify(summonerRepository, times(1)).findByName(summonerName);
    }

    @Test
    void testGetById_ExistingSummoner_Returns200() {
        // Arrange
        Long summonerId = 1L;
        Summoner mockSummoner = new Summoner("riot-1", "TestSummoner", 60);
        
        when(summonerRepository.findById(summonerId))
            .thenReturn(Optional.of(mockSummoner));

        // Act
        ResponseEntity<?> response = summonerController.getById(summonerId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(summonerRepository, times(1)).findById(summonerId);
    }

    @Test
    void testGetById_NonExistentSummoner_Returns404() {
        // Arrange
        Long summonerId = 999L;
        when(summonerRepository.findById(anyLong()))
            .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = summonerController.getById(summonerId);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        verify(summonerRepository, times(1)).findById(summonerId);
    }
}