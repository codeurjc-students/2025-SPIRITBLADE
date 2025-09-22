package com.tfg.tfg.unit;

import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.service.SummonerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del servicio Summoner con dobles de la base de datos.
 */
@ExtendWith(MockitoExtension.class)
public class SummonerServiceUnitTest {

    @Mock
    private SummonerRepository summonerRepository;

    @InjectMocks
    private SummonerService summonerService;

    @Test
    public void testFindByName_ExistingSummoner_ReturnsCorrectSummoner() {
        // Arrange
        String summonerName = "TestSummoner";
        Summoner mockSummoner = new Summoner("riot-123", summonerName, 100);
        mockSummoner.setTier("Gold");
        mockSummoner.setRank("II");
        
        when(summonerRepository.findByName(summonerName))
            .thenReturn(Optional.of(mockSummoner));

        // Act
        Optional<Summoner> result = summonerService.findByName(summonerName);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(summonerName, result.get().getName());
        assertEquals("Gold", result.get().getTier());
        verify(summonerRepository, times(1)).findByName(summonerName);
    }

    @Test
    public void testFindByName_NonExistentSummoner_ReturnsEmpty() {
        // Arrange
        String summonerName = "NonExistentSummoner";
        when(summonerRepository.findByName(anyString()))
            .thenReturn(Optional.empty());

        // Act
        Optional<Summoner> result = summonerService.findByName(summonerName);

        // Assert
        assertFalse(result.isPresent());
        verify(summonerRepository, times(1)).findByName(summonerName);
    }

    @Test
    public void testFindAll_ReturnsListOfSummoners() {
        // Arrange
        Summoner summoner1 = new Summoner("riot-1", "Summoner1", 50);
        Summoner summoner2 = new Summoner("riot-2", "Summoner2", 75);
        List<Summoner> mockSummoners = Arrays.asList(summoner1, summoner2);
        
        when(summonerRepository.findAll()).thenReturn(mockSummoners);

        // Act
        List<Summoner> result = summonerService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Summoner1", result.get(0).getName());
        assertEquals("Summoner2", result.get(1).getName());
        verify(summonerRepository, times(1)).findAll();
    }

    @Test
    public void testSave_NewSummoner_SavesSuccessfully() {
        // Arrange
        Summoner newSummoner = new Summoner("riot-new", "NewSummoner", 25);
        when(summonerRepository.save(any(Summoner.class))).thenReturn(newSummoner);

        // Act
        Summoner result = summonerService.save(newSummoner);

        // Assert
        assertNotNull(result);
        assertEquals("NewSummoner", result.getName());
        verify(summonerRepository, times(1)).save(newSummoner);
    }
}