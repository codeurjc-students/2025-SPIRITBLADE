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
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import com.tfg.tfg.dto.AiAnalysisResponseDto;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.service.AiAnalysisService;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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

    @Test
    void testBuildStatsPromptWithMatchesUsesAllFields() throws Exception {
        // Call private buildStatsPrompt to exercise the large prompt-building logic
        java.lang.reflect.Method m = AiAnalysisService.class.getDeclaredMethod("buildStatsPrompt", Summoner.class, List.class);
        m.setAccessible(true);

        String prompt = (String) m.invoke(service, testSummoner, testMatches);

        assertNotNull(prompt);
        assertTrue(prompt.contains("TestPlayer"));
        assertTrue(prompt.contains("Total Matches"));
        // Should include at least the first match block and a champion name
        assertTrue(prompt.contains("MATCH 1"));
        assertTrue(prompt.contains("Champion0") || prompt.contains("Champion1"));
        // KDA appears formatted
        assertTrue(prompt.contains("KDA") || prompt.toLowerCase().contains("kda"));
    }

    @Test
    void testCallGeminiApi_successAndErrorPaths() throws Exception {
        // Prepare a mock WebClient chain
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(uriSpec).when(webClient).post();
        doReturn(uriSpec).when(uriSpec).uri(anyString());
        doReturn(uriSpec).when(uriSpec).header(anyString(), anyString());
        // Use doReturn to avoid generic capture issues with Mockito
        doReturn(headersSpec).when(uriSpec).bodyValue(any());
        doReturn(responseSpec).when(headersSpec).retrieve();

        // Successful response
        String successJson = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"AI-OUT\"}]},\"finishReason\":\"STOP\"}]}";
    doReturn(Mono.just(successJson)).when(responseSpec).bodyToMono(String.class);

        ReflectionTestUtils.setField(service, "webClient", webClient);
        ReflectionTestUtils.setField(service, "geminiApiKey", "key");

        java.lang.reflect.Method m = AiAnalysisService.class.getDeclaredMethod("callGeminiApi", String.class);
        m.setAccessible(true);

        String out = (String) m.invoke(service, "prompt");
        assertEquals("AI-OUT", out);

        // Error response should throw IOException
        String errJson = "{\"error\":{\"message\":\"bad key\"}}";
    doReturn(Mono.just(errJson)).when(responseSpec).bodyToMono(String.class);

        Exception ex = assertThrows(Exception.class, () -> {
            try {
                m.invoke(service, "prompt2");
            } catch (java.lang.reflect.InvocationTargetException ite) {
                // unwrap
                throw ite.getCause();
            }
        });
        assertTrue(ex.getMessage().contains("Gemini API error") || ex.getMessage().contains("bad key"));
    }
}
