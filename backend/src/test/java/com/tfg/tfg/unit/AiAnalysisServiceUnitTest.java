package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.tfg.tfg.dto.AiAnalysisResponseDto;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.service.AiAnalysisService;

/**
 * Unit tests for AiAnalysisService.
 */
@ExtendWith(MockitoExtension.class)
class AiAnalysisServiceUnitTest {
    
    private AiAnalysisService service;
    private Summoner testSummoner;
    private List<MatchEntity> testMatches;
    
    @BeforeEach
    void setUp() {
        service = new AiAnalysisService();
        
        // Setup test summoner
        testSummoner = new Summoner();
        testSummoner.setId(1L);
        testSummoner.setName("TestPlayer");
        testSummoner.setPuuid("test-puuid");
        
        // Setup test matches
        testMatches = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MatchEntity match = new MatchEntity();
            match.setId((long) i);
            match.setMatchId("EUW1_" + i);
            match.setSummoner(testSummoner);
            match.setChampionName("Champion" + (i % 3));
            match.setRole(i % 2 == 0 ? "TOP" : "MID");
            match.setKills(5 + i);
            match.setDeaths(3);
            match.setAssists(7 + i);
            match.setWin(i % 3 != 0); // Win rate ~66%
            match.setGameDuration(1800L); // 30 minutes
            match.setTimestamp(LocalDateTime.now().minusDays(i));
            testMatches.add(match);
        }
    }
    
    @Test
    void testAnalyzePerformanceWithoutApiKey() {
        // API key not set
        ReflectionTestUtils.setField(service, "geminiApiKey", "");
        
        // Should throw exception when API key is missing
        assertThrows(IllegalStateException.class, () -> {
            service.analyzePerformance(testSummoner, testMatches);
        });
    }
    
    @Test
    void testAnalyzePerformanceWithEmptyMatches() throws IOException {
        // Set a dummy API key
        ReflectionTestUtils.setField(service, "geminiApiKey", "test-key");
        
        List<MatchEntity> emptyMatches = new ArrayList<>();
        
        AiAnalysisResponseDto result = service.analyzePerformance(testSummoner, emptyMatches);
        
        assertNotNull(result);
        assertEquals(0, result.getMatchesAnalyzed());
        assertEquals("TestPlayer", result.getSummonerName());
        assertTrue(result.getAnalysis().contains("Insufficient Match Data") || 
                   result.getAnalysis().contains("not enough matches"));
        assertNotNull(result.getGeneratedAt());
    }
    
    @Test
    void testBuildStatsPromptLogic() throws IOException {
        // We can't test the actual API call without a real key,
        // but we can verify the service doesn't crash with empty matches
        ReflectionTestUtils.setField(service, "geminiApiKey", "test-key");
        
        List<MatchEntity> emptyMatches = new ArrayList<>();
        AiAnalysisResponseDto result = service.analyzePerformance(testSummoner, emptyMatches);
        
        // Should return a fallback message for empty matches
        assertNotNull(result);
        assertFalse(result.getAnalysis().isEmpty());
    }
    
    @Test
    void testAnalysisResponseDtoCreation() {
        String analysisText = "Test analysis";
        LocalDateTime now = LocalDateTime.now();
        int matchCount = 20;
        String summonerName = "TestSummoner";
        
        AiAnalysisResponseDto dto = new AiAnalysisResponseDto(
            analysisText, 
            now, 
            matchCount, 
            summonerName
        );
        
        assertEquals(analysisText, dto.getAnalysis());
        assertEquals(now, dto.getGeneratedAt());
        assertEquals(matchCount, dto.getMatchesAnalyzed());
        assertEquals(summonerName, dto.getSummonerName());
    }
    
    @Test
    void testAnalysisResponseDtoSettersAndGetters() {
        AiAnalysisResponseDto dto = new AiAnalysisResponseDto();
        
        String analysisText = "Test analysis";
        LocalDateTime now = LocalDateTime.now();
        int matchCount = 15;
        String summonerName = "Player1";
        
        dto.setAnalysis(analysisText);
        dto.setGeneratedAt(now);
        dto.setMatchesAnalyzed(matchCount);
        dto.setSummonerName(summonerName);
        
        assertEquals(analysisText, dto.getAnalysis());
        assertEquals(now, dto.getGeneratedAt());
        assertEquals(matchCount, dto.getMatchesAnalyzed());
        assertEquals(summonerName, dto.getSummonerName());
    }
}
