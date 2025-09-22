package com.tfg.tfg.integration;

import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.service.SummonerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración del servicio Summoner usando la base de datos real (H2).
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(SummonerService.class)
public class SummonerServiceIntegrationTest {

    @Autowired
    private SummonerRepository summonerRepository;

    @Autowired
    private SummonerService summonerService;

    @BeforeEach
    public void setUp() {
        // Limpiar la base de datos antes de cada test
        summonerRepository.deleteAll();
        
        // Insertar datos de prueba
        Summoner summoner1 = new Summoner("riot-test-1", "IntegrationTestSummoner1", 60);
        summoner1.setTier("Silver");
        summoner1.setRank("I");
        
        Summoner summoner2 = new Summoner("riot-test-2", "IntegrationTestSummoner2", 90);
        summoner2.setTier("Gold");
        summoner2.setRank("IV");
        
        summonerRepository.save(summoner1);
        summonerRepository.save(summoner2);
    }

    @Test
    public void testFindByName_WithRealDatabase_ReturnsCorrectSummoner() {
        // Act
        Optional<Summoner> result = summonerService.findByName("IntegrationTestSummoner1");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("IntegrationTestSummoner1", result.get().getName());
        assertEquals("Silver", result.get().getTier());
        assertEquals("I", result.get().getRank());
        assertEquals(60, result.get().getLevel());
    }

    @Test
    public void testFindAll_WithRealDatabase_ReturnsAllSummoners() {
        // Act
        List<Summoner> result = summonerService.findAll();

        // Assert
        assertEquals(2, result.size());
        
        // Verificar que ambos summoners están presentes
        boolean summoner1Found = result.stream()
            .anyMatch(s -> s.getName().equals("IntegrationTestSummoner1"));
        boolean summoner2Found = result.stream()
            .anyMatch(s -> s.getName().equals("IntegrationTestSummoner2"));
            
        assertTrue(summoner1Found);
        assertTrue(summoner2Found);
    }

    @Test
    public void testSaveAndFind_WithRealDatabase_PersistsCorrectly() {
        // Arrange
        Summoner newSummoner = new Summoner("riot-new-integration", "NewIntegrationSummoner", 45);
        newSummoner.setTier("Bronze");
        newSummoner.setRank("II");

        // Act - Save
        Summoner savedSummoner = summonerService.save(newSummoner);
        
        // Assert - Save result
        assertNotNull(savedSummoner.getId());
        assertEquals("NewIntegrationSummoner", savedSummoner.getName());
        
        // Act - Find the saved summoner
        Optional<Summoner> foundSummoner = summonerService.findByName("NewIntegrationSummoner");
        
        // Assert - Find result
        assertTrue(foundSummoner.isPresent());
        assertEquals("Bronze", foundSummoner.get().getTier());
        assertEquals("II", foundSummoner.get().getRank());
        assertEquals(45, foundSummoner.get().getLevel());
        
        // Verify total count increased
        List<Summoner> allSummoners = summonerService.findAll();
        assertEquals(3, allSummoners.size()); // 2 from setUp + 1 new
    }

    @Test
    public void testFindByName_NonExistent_WithRealDatabase_ReturnsEmpty() {
        // Act
        Optional<Summoner> result = summonerService.findByName("NonExistentSummonerName");

        // Assert
        assertFalse(result.isPresent());
    }
}