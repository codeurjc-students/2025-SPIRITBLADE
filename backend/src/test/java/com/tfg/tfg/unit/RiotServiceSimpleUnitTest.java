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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.dto.riot.*;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.repository.MatchEntityRepository;
import com.tfg.tfg.service.DataDragonService;
import com.tfg.tfg.service.RiotService;

@ExtendWith(MockitoExtension.class)
class RiotServiceSimpleUnitTest {

    @Mock
    private SummonerRepository summonerRepository;
    
    @Mock
    private MatchEntityRepository matchRepository;
    
    @Mock
    private DataDragonService dataDragonService;
    
    @Mock
    private RestTemplate restTemplate;
    
    private RiotService riotService;
    
    @BeforeEach
    void setUp() {
        riotService = new RiotService(summonerRepository, matchRepository, dataDragonService);
        // Inject mocked RestTemplate via reflection
        try {
            var field = RiotService.class.getDeclaredField("restTemplate");
            field.setAccessible(true);
            field.set(riotService, restTemplate);
            
            var apiKeyField = RiotService.class.getDeclaredField("apiKey");
            apiKeyField.setAccessible(true);
            apiKeyField.set(riotService, "test-api-key");
        } catch (Exception e) {
            fail("Failed to inject mocked dependencies: " + e.getMessage());
        }
    }
    
    @Test
    void testGetDataDragonService() {
        assertNotNull(riotService.getDataDragonService());
        assertEquals(dataDragonService, riotService.getDataDragonService());
    }
    
    @Test
    void testGetSummonerByName_InvalidFormat_FallbackToDatabase() {
        String invalidRiotId = "InvalidFormat";
        Summoner summoner = new Summoner();
        summoner.setName(invalidRiotId);
        summoner.setPuuid("test-puuid");
        summoner.setProfileIconId(1);
        
        when(summonerRepository.findByName(invalidRiotId)).thenReturn(Optional.of(summoner));
        when(dataDragonService.getProfileIconUrl(any())).thenReturn("http://icon.url");
        
        SummonerDTO result = riotService.getSummonerByName(invalidRiotId);
        
        assertNotNull(result);
        assertEquals(invalidRiotId, result.getName());
        verify(summonerRepository).findByName(invalidRiotId);
    }
    
    @Test
    void testGetSummonerByName_AccountNotFound() {
        String riotId = "Player#EUW";
        
        RiotAccountDTO accountDTO = new RiotAccountDTO();
        accountDTO.setPuuid(null);
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotAccountDTO.class), 
            anyString(), anyString(), anyString()))
            .thenReturn(ResponseEntity.ok(accountDTO));
        
        SummonerDTO result = riotService.getSummonerByName(riotId);
        
        assertNull(result);
    }
    
    @Test
    void testGetSummonerByName_Success() {
        String riotId = "Player#EUW";
        String puuid = "test-puuid";
        
        // Mock Account API response
        RiotAccountDTO accountDTO = new RiotAccountDTO();
        accountDTO.setPuuid(puuid);
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotAccountDTO.class), 
            eq("Player"), eq("EUW"), anyString()))
            .thenReturn(ResponseEntity.ok(accountDTO));
        
        // Mock Summoner API response
        RiotSummonerDTO summonerDTO = new RiotSummonerDTO();
        summonerDTO.setId("summoner-id");
        summonerDTO.setPuuid(puuid);
        summonerDTO.setSummonerLevel(100);
        summonerDTO.setProfileIconId(1);
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotSummonerDTO.class), 
            eq(puuid), anyString()))
            .thenReturn(ResponseEntity.ok(summonerDTO));
        
        // Mock League API response
        RiotLeagueEntryDTO leagueEntry = new RiotLeagueEntryDTO();
        leagueEntry.setQueueType("RANKED_SOLO_5x5");
        leagueEntry.setTier("GOLD");
        leagueEntry.setRank("II");
        leagueEntry.setLeaguePoints(50);
        leagueEntry.setWins(100);
        leagueEntry.setLosses(90);
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotLeagueEntryDTO[].class), 
            eq(puuid), anyString()))
            .thenReturn(ResponseEntity.ok(new RiotLeagueEntryDTO[]{leagueEntry}));
        
        when(dataDragonService.getProfileIconUrl(1)).thenReturn("http://icon.url");
        when(summonerRepository.findByPuuid(puuid)).thenReturn(Optional.empty());
        when(summonerRepository.save(any(Summoner.class))).thenAnswer(i -> i.getArgument(0));
        
        SummonerDTO result = riotService.getSummonerByName(riotId);
        
        assertNotNull(result);
        assertEquals(riotId, result.getName());
        assertEquals("GOLD", result.getTier());
        assertEquals("II", result.getRank());
        assertEquals(50, result.getLp());
        verify(summonerRepository).save(any(Summoner.class));
    }
    
    @Test
    void testGetSummonerByName_Unranked() {
        String riotId = "Unranked#EUW";
        String puuid = "test-puuid-unranked";
        
        RiotAccountDTO accountDTO = new RiotAccountDTO();
        accountDTO.setPuuid(puuid);
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotAccountDTO.class), 
            eq("Unranked"), eq("EUW"), anyString()))
            .thenReturn(ResponseEntity.ok(accountDTO));
        
        RiotSummonerDTO summonerDTO = new RiotSummonerDTO();
        summonerDTO.setId("summoner-id");
        summonerDTO.setPuuid(puuid);
        summonerDTO.setSummonerLevel(30);
        summonerDTO.setProfileIconId(1);
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotSummonerDTO.class), 
            eq(puuid), anyString()))
            .thenReturn(ResponseEntity.ok(summonerDTO));
        
        // Empty league entries (unranked)
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotLeagueEntryDTO[].class), 
            eq(puuid), anyString()))
            .thenReturn(ResponseEntity.ok(new RiotLeagueEntryDTO[]{}));
        
        when(dataDragonService.getProfileIconUrl(1)).thenReturn("http://icon.url");
        when(summonerRepository.findByPuuid(puuid)).thenReturn(Optional.empty());
        when(summonerRepository.save(any(Summoner.class))).thenAnswer(i -> i.getArgument(0));
        
        SummonerDTO result = riotService.getSummonerByName(riotId);
        
        assertNotNull(result);
        assertEquals("UNRANKED", result.getTier());
        assertEquals("", result.getRank());
        assertEquals(0, result.getLp());
    }
    
    @Test
    void testGetSummonerByName_ApiError_FallbackToDatabase() {
        String riotId = "Player#EUW";
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotAccountDTO.class), 
            anyString(), anyString(), anyString()))
            .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));
        
        Summoner cachedSummoner = new Summoner();
        cachedSummoner.setName(riotId);
        cachedSummoner.setPuuid("cached-puuid");
        cachedSummoner.setProfileIconId(1);
        
        when(summonerRepository.findByName(riotId)).thenReturn(Optional.of(cachedSummoner));
        when(dataDragonService.getProfileIconUrl(any())).thenReturn("http://icon.url");
        
        SummonerDTO result = riotService.getSummonerByName(riotId);
        
        assertNotNull(result);
        assertEquals(riotId, result.getName());
        verify(summonerRepository).findByName(riotId);
    }
    
    @Test
    void testGetTopChampionMasteries_Success() {
        String puuid = "test-puuid";
        
        RiotChampionMasteryDTO mastery = new RiotChampionMasteryDTO();
        mastery.setChampionId(1L);
        mastery.setChampionLevel(7);
        mastery.setChampionPoints(100000);
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotChampionMasteryDTO[].class), 
            eq(puuid), eq(3), anyString()))
            .thenReturn(ResponseEntity.ok(new RiotChampionMasteryDTO[]{mastery}));
        
        when(dataDragonService.getChampionNameById(1L)).thenReturn("TestChampion");
        when(dataDragonService.getChampionIconUrl(1L)).thenReturn("http://champion.icon");
        
        List<RiotChampionMasteryDTO> result = riotService.getTopChampionMasteries(puuid, 3);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TestChampion", result.get(0).getChampionName());
    }
    
    @Test
    void testGetTopChampionMasteries_EmptyResponse() {
        String puuid = "test-puuid";
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotChampionMasteryDTO[].class), 
            eq(puuid), eq(3), anyString()))
            .thenReturn(ResponseEntity.ok(new RiotChampionMasteryDTO[]{}));
        
        List<RiotChampionMasteryDTO> result = riotService.getTopChampionMasteries(puuid, 3);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetTopChampionMasteries_ApiError() {
        String puuid = "test-puuid";
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotChampionMasteryDTO[].class), 
            eq(puuid), eq(3), anyString()))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        List<RiotChampionMasteryDTO> result = riotService.getTopChampionMasteries(puuid, 3);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetMatchHistory_EmptyCache() {
        String puuid = "test-puuid";
        
        when(summonerRepository.findByPuuid(puuid)).thenReturn(Optional.empty());
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(String[].class), 
            eq(puuid), anyInt(), anyInt(), anyString()))
            .thenReturn(ResponseEntity.ok(new String[]{}));
        
        List<MatchHistoryDTO> result = riotService.getMatchHistory(puuid, 0, 5);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetMatchDetails_Success() {
        String matchId = "EUW1_123456";
        
        RiotMatchDTO riotMatch = new RiotMatchDTO();
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        metadata.setMatchId(matchId);
        riotMatch.setMetadata(metadata);
        
        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        info.setGameCreation(System.currentTimeMillis());
        info.setGameDuration(1800L);
        info.setGameMode("CLASSIC");
        info.setGameType("MATCHED_GAME");
        info.setQueueId(420);
        riotMatch.setInfo(info);
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotMatchDTO.class), 
            eq(matchId), anyString()))
            .thenReturn(ResponseEntity.ok(riotMatch));
        
        var result = riotService.getMatchDetails(matchId);
        
        assertNotNull(result);
        assertEquals(matchId, result.getMatchId());
        assertEquals("CLASSIC", result.getGameMode());
    }
    
    @Test
    void testGetMatchDetails_NotFound() {
        String matchId = "INVALID_MATCH";
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            isNull(), 
            eq(RiotMatchDTO.class), 
            eq(matchId), anyString()))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        var result = riotService.getMatchDetails(matchId);
        
        assertNull(result);
    }
}
