package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.MatchEntityRepository;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.service.MatchAnalysisService;

@ExtendWith(MockitoExtension.class)
class MatchAnalysisServiceSimpleUnitTest {

    @Mock
    private MatchEntityRepository matchRepository;
    
    @Mock
    private SummonerRepository summonerRepository;
    
    private MatchAnalysisService service;
    
    @BeforeEach
    void setUp() {
        service = new MatchAnalysisService(matchRepository, summonerRepository);
    }
    
    @Test
    void testGetMatchesForSummoner_Success() {
        String summonerName = "TestPlayer";
        Summoner summoner = new Summoner();
        summoner.setId(1L);
        summoner.setName(summonerName);
        
        MatchEntity match = new MatchEntity();
        match.setMatchId("EUW1_123");
        
        when(summonerRepository.findByName(summonerName)).thenReturn(Optional.of(summoner));
        when(matchRepository.findBySummoner(summoner)).thenReturn(List.of(match));
        
        List<MatchEntity> result = service.getMatchesForSummoner(summonerName);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(summonerRepository).findByName(summonerName);
        verify(matchRepository).findBySummoner(summoner);
    }
    
    @Test
    void testGetMatchesForSummoner_NotFound() {
        String summonerName = "NonExistent";
        
        when(summonerRepository.findByName(summonerName)).thenReturn(Optional.empty());
        
        List<MatchEntity> result = service.getMatchesForSummoner(summonerName);
        
        assertTrue(result.isEmpty());
        verify(matchRepository, never()).findBySummoner(any());
    }
    
    @Test
    void testGetMatchesBySummonerNameSimple_Success() {
        String summonerName = "TestPlayer";
        MatchEntity match = new MatchEntity();
        match.setMatchId("EUW1_123");
        
        when(matchRepository.findBySummonerName(summonerName)).thenReturn(List.of(match));
        
        List<MatchEntity> result = service.getMatchesBySummonerNameSimple(summonerName);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(matchRepository).findBySummonerName(summonerName);
    }
    
    @Test
    void testGetHighKillMatches_Success() {
        Long summonerId = 1L;
        int minKills = 10;
        
        MatchEntity match = new MatchEntity();
        match.setKills(15);
        
        when(matchRepository.findBySummonerIdWithMinKills(summonerId, minKills))
            .thenReturn(List.of(match));
        
        List<MatchEntity> result = service.getHighKillMatches(summonerId, minKills);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(15, result.get(0).getKills());
    }
    
    @Test
    void testGetWinRateForSummoner_Success() {
        Long summonerId = 1L;
        Object[] stats = {100L, 60L, 40L};
        
        when(matchRepository.getWinRateStats(summonerId)).thenReturn(stats);
        
        var result = service.getWinRateForSummoner(summonerId);
        
        assertNotNull(result);
        assertEquals(100, result.getTotalMatches());
        assertEquals(60, result.getWins());
        assertEquals(40, result.getLosses());
        assertEquals(60.0, result.getWinRate(), 0.01);
    }
    
    @Test
    void testGetWinRateForSummoner_NoData() {
        Long summonerId = 1L;
        
        when(matchRepository.getWinRateStats(summonerId)).thenReturn(null);
        
        var result = service.getWinRateForSummoner(summonerId);
        
        assertNotNull(result);
        assertEquals(0, result.getTotalMatches());
        assertEquals(0, result.getWins());
        assertEquals(0, result.getLosses());
        assertEquals(0.0, result.getWinRate(), 0.01);
    }
    
    @Test
    void testGetMatchesWithNotes_Success() {
        Long summonerId = 1L;
        MatchEntity match = new MatchEntity();
        
        when(matchRepository.findMatchesWithNotesBySummonerId(summonerId))
            .thenReturn(List.of(match));
        
        List<MatchEntity> result = service.getMatchesWithNotes(summonerId);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }
    
    @Test
    void testGetActiveSummoners_Success() {
        long minMatches = 10;
        Summoner summoner = new Summoner();
        summoner.setName("ActivePlayer");
        
        when(matchRepository.findActiveSummoners(minMatches))
            .thenReturn(List.of(summoner));
        
        List<Summoner> result = service.getActiveSummoners(minMatches);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ActivePlayer", result.get(0).getName());
    }
    
    @Test
    void testGetSummonerNameFromMatch_Success() {
        Long matchId = 1L;
        Summoner summoner = new Summoner();
        summoner.setName("TestPlayer");
        
        MatchEntity match = new MatchEntity();
        match.setId(matchId);
        match.setSummoner(summoner);
        
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        
        String result = service.getSummonerNameFromMatch(matchId);
        
        assertEquals("TestPlayer", result);
    }
    
    @Test
    void testGetSummonerNameFromMatch_NotFound() {
        Long matchId = 999L;
        
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());
        
        String result = service.getSummonerNameFromMatch(matchId);
        
        assertNull(result);
    }
    
    @Test
    void testGetSummonerNameFromMatch_NullSummoner() {
        Long matchId = 1L;
        MatchEntity match = new MatchEntity();
        match.setId(matchId);
        match.setSummoner(null);
        
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        
        String result = service.getSummonerNameFromMatch(matchId);
        
        assertNull(result);
    }
    
    @Test
    void testWinRateStats_WinRateCalculation() {
        var stats = new MatchAnalysisService.WinRateStats(100, 75, 25);
        
        assertEquals(100, stats.getTotalMatches());
        assertEquals(75, stats.getWins());
        assertEquals(25, stats.getLosses());
        assertEquals(75.0, stats.getWinRate(), 0.01);
    }
    
    @Test
    void testWinRateStats_ZeroMatches() {
        var stats = new MatchAnalysisService.WinRateStats(0, 0, 0);
        
        assertEquals(0.0, stats.getWinRate(), 0.01);
    }
}
