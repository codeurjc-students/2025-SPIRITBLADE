package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.tfg.tfg.exception.SummonerNotFoundException;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.riot.RiotAccountDTO;
import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;
import com.tfg.tfg.model.dto.riot.RiotLeagueEntryDTO;
import com.tfg.tfg.model.dto.riot.RiotMatchDTO;
import com.tfg.tfg.model.dto.riot.RiotSummonerDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.MatchEntityRepository;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.service.DataDragonService;
import com.tfg.tfg.service.RiotService;

@ExtendWith(MockitoExtension.class)
class RiotServiceUnitTest {

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
    void setUp() throws Exception {
        riotService = new RiotService(summonerRepository, matchRepository, dataDragonService);
        
        // Inject mocked RestTemplate using reflection
        Field restTemplateField = RiotService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(riotService, restTemplate);
        
        // Inject API key
        Field apiKeyField = RiotService.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(riotService, "test-api-key");
    }

    @Test
    void testGetSummonerByNameSuccess() {
        // Given
        String riotId = "TestPlayer#EUW";
        String puuid = "test-puuid-123";
        
        RiotAccountDTO accountDTO = new RiotAccountDTO(puuid, "TestPlayer", "EUW");
        RiotSummonerDTO summonerDTO = new RiotSummonerDTO();
        summonerDTO.setId("summoner-id");
        summonerDTO.setPuuid(puuid);
        summonerDTO.setName("TestPlayer");
        summonerDTO.setSummonerLevel(100);
        summonerDTO.setProfileIconId(1);
        
        RiotLeagueEntryDTO rankedEntry = new RiotLeagueEntryDTO();
        rankedEntry.setQueueType("RANKED_SOLO_5x5");
        rankedEntry.setTier("GOLD");
        rankedEntry.setRank("II");
        rankedEntry.setLeaguePoints(50);
        rankedEntry.setWins(100);
        rankedEntry.setLosses(90);
        
        RiotLeagueEntryDTO[] leagueEntries = {rankedEntry};
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(RiotAccountDTO.class),
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(ResponseEntity.ok(accountDTO));
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(RiotSummonerDTO.class),
            anyString(),
            anyString()
        )).thenReturn(ResponseEntity.ok(summonerDTO));
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(RiotLeagueEntryDTO[].class),
            anyString(),
            anyString()
        )).thenReturn(ResponseEntity.ok(leagueEntries));
        
        when(dataDragonService.getProfileIconUrl(anyInt())).thenReturn("http://example.com/icon.png");
        when(summonerRepository.findByPuuid(puuid)).thenReturn(Optional.empty());
        when(summonerRepository.save(any(Summoner.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        SummonerDTO result = riotService.getSummonerByName(riotId);
        
        // Then
        assertNotNull(result);
        assertEquals(riotId, result.getName());
        assertEquals(100, result.getLevel());
        assertEquals("GOLD", result.getTier());
        verify(summonerRepository).save(any(Summoner.class));
    }

    @Test
    void testGetSummonerByNameNotFound() {
        // Given
        String riotId = "NonExistent#EUW";
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(RiotAccountDTO.class),
            anyString(),
            anyString(),
            anyString()
        )).thenThrow(HttpClientErrorException.NotFound.create(
            HttpStatus.NOT_FOUND, 
            "Not Found", 
            null, 
            null, 
            null
        ));
        
        // When & Then
        assertThrows(SummonerNotFoundException.class, () -> {
            riotService.getSummonerByName(riotId);
        });
    }

    @Test
    void testGetTopChampionMasteriesSuccess() {
        // Given
        String puuid = "test-puuid";
        RiotChampionMasteryDTO mastery = new RiotChampionMasteryDTO();
        mastery.setChampionId(103L);
        mastery.setChampionLevel(7);
        mastery.setChampionPoints(500000);
        RiotChampionMasteryDTO[] masteries = {mastery};
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(RiotChampionMasteryDTO[].class),
            anyString(),
            anyInt(),
            anyString()
        )).thenReturn(ResponseEntity.ok(masteries));
        
        when(dataDragonService.getChampionNameById(103L)).thenReturn("Ahri");
        when(dataDragonService.getChampionIconUrl(103L)).thenReturn("http://example.com/ahri.png");
        
        // When
        List<RiotChampionMasteryDTO> result = riotService.getTopChampionMasteries(puuid, 5);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(103L, result.get(0).getChampionId());
        assertEquals("Ahri", result.get(0).getChampionName());
    }

    @Test
    void testGetTopChampionMasteriesApiError() {
        // Given
        String puuid = "test-puuid";
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(RiotChampionMasteryDTO[].class),
            anyString(),
            anyInt(),
            anyString()
        )).thenThrow(HttpClientErrorException.Forbidden.create(
            HttpStatus.FORBIDDEN,
            "Forbidden",
            null,
            null,
            null
        ));
        
        // When
        List<RiotChampionMasteryDTO> result = riotService.getTopChampionMasteries(puuid, 5);
        
        // Then - Returns empty list on error, doesn't throw exception
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetDataDragonService() {
        // When
        DataDragonService result = riotService.getDataDragonService();
        
        // Then
        assertNotNull(result);
        assertEquals(dataDragonService, result);
    }

    @Test
    void testGetMatchHistorySuccess() {
        // Given
        String puuid = "test-puuid";
        String[] matchIds = {"EUW1_123", "EUW1_456"};
        
        RiotMatchDTO matchDTO = new RiotMatchDTO();
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        metadata.setMatchId("EUW1_123");
        metadata.setParticipants(List.of(puuid, "other-puuid"));
        matchDTO.setMetadata(metadata);
        
        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        info.setGameDuration(1800L);
        info.setGameCreation(System.currentTimeMillis());
        
        RiotMatchDTO.ParticipantDTO participant = new RiotMatchDTO.ParticipantDTO();
        participant.setPuuid(puuid);
        participant.setChampionName("Ahri");
        participant.setChampionId(103);
        participant.setWin(true);
        participant.setKills(10);
        participant.setDeaths(2);
        participant.setAssists(15);
        
        info.setParticipants(List.of(participant));
        matchDTO.setInfo(info);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(String[].class),
            anyString(),
            anyInt(),
            anyInt(),
            anyString()
        )).thenReturn(ResponseEntity.ok(matchIds));
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(RiotMatchDTO.class),
            anyString(),
            anyString()
        )).thenReturn(ResponseEntity.ok(matchDTO));
        
        when(dataDragonService.getChampionIconUrl(anyLong())).thenReturn("http://example.com/ahri.png");
        when(matchRepository.findByMatchId(anyString())).thenReturn(Optional.empty());
        
        // When
        var result = riotService.getMatchHistory(puuid, 0, 10);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetMatchHistoryEmptyResult() {
        // Given
        String puuid = "test-puuid";
        String[] emptyMatchIds = {};
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(String[].class),
            anyString(),
            anyInt(),
            anyInt(),
            anyString()
        )).thenReturn(ResponseEntity.ok(emptyMatchIds));
        
        // When
        var result = riotService.getMatchHistory(puuid, 0, 10);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMatchDetailsSuccess() {
        // Given
        String matchId = "EUW1_123";
        
        RiotMatchDTO matchDTO = new RiotMatchDTO();
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        metadata.setMatchId(matchId);
        metadata.setParticipants(List.of("puuid1", "puuid2"));
        matchDTO.setMetadata(metadata);
        
        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        info.setGameDuration(1800L);
        info.setGameCreation(System.currentTimeMillis());
        
        RiotMatchDTO.ParticipantDTO p1 = new RiotMatchDTO.ParticipantDTO();
        p1.setPuuid("puuid1");
        p1.setChampionName("Ahri");
        p1.setChampionId(103);
        p1.setTeamId(100);
        p1.setWin(true);
        
        RiotMatchDTO.TeamDTO team1 = new RiotMatchDTO.TeamDTO();
        team1.setTeamId(100);
        team1.setWin(true);
        
        info.setParticipants(List.of(p1));
        info.setTeams(List.of(team1));
        matchDTO.setInfo(info);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(RiotMatchDTO.class),
            anyString(),
            anyString()
        )).thenReturn(ResponseEntity.ok(matchDTO));
        
        when(dataDragonService.getChampionIconUrl(anyLong())).thenReturn("http://example.com/ahri.png");
        
        // When
        var result = riotService.getMatchDetails(matchId);
        
        // Then
        assertNotNull(result);
        assertEquals(matchId, result.getMatchId());
        assertFalse(result.getParticipants().isEmpty());
    }

    // ==================== Helper Methods Tests ====================
    
    @Test
    void testGetObjectiveKillsWithObjective() {
        // Given
        RiotMatchDTO.ObjectiveDTO objective = new RiotMatchDTO.ObjectiveDTO();
        objective.setKills(5);
        
        // When - Using reflection to call private method
        try {
            java.lang.reflect.Method method = RiotService.class.getDeclaredMethod(
                "getObjectiveKills", 
                RiotMatchDTO.ObjectiveDTO.class
            );
            method.setAccessible(true);
            int result = (int) method.invoke(riotService, objective);
            
            // Then
            assertEquals(5, result);
        } catch (Exception e) {
            fail("Failed to invoke getObjectiveKills: " + e.getMessage());
        }
    }
    
    @Test
    void testGetObjectiveKillsNullObjective() {
        // When - Using reflection to call private method
        try {
            java.lang.reflect.Method method = RiotService.class.getDeclaredMethod(
                "getObjectiveKills", 
                RiotMatchDTO.ObjectiveDTO.class
            );
            method.setAccessible(true);
            int result = (int) method.invoke(riotService, (Object) null);
            
            // Then
            assertEquals(0, result);
        } catch (Exception e) {
            fail("Failed to invoke getObjectiveKills: " + e.getMessage());
        }
    }
    
    @Test
    void testMapSummonerEntityToDTO() {
        // Given
        Summoner summoner = new Summoner();
        summoner.setName("TestPlayer#EUW");
        summoner.setPuuid("test-puuid");
        summoner.setLevel(100);
        summoner.setProfileIconId(1);
        summoner.setTier("GOLD");
        summoner.setRank("II");
        summoner.setLp(50);
        summoner.setWins(100);
        summoner.setLosses(90);
        
        when(dataDragonService.getProfileIconUrl(1)).thenReturn("http://example.com/icon.png");
        
        // When - Using reflection to call private method
        try {
            java.lang.reflect.Method method = RiotService.class.getDeclaredMethod(
                "mapSummonerEntityToDTO", 
                Summoner.class
            );
            method.setAccessible(true);
            SummonerDTO result = (SummonerDTO) method.invoke(riotService, summoner);
            
            // Then
            assertNotNull(result);
            assertEquals("TestPlayer#EUW", result.getName());
            assertEquals(100, result.getLevel());
            assertEquals("GOLD", result.getTier());
            assertEquals("II", result.getRank());
            assertEquals(50, result.getLp());
        } catch (Exception e) {
            fail("Failed to invoke mapSummonerEntityToDTO: " + e.getMessage());
        }
    }
    
    @Test
    void testFindParticipantByPuuidFound() {
        // Given
        String targetPuuid = "target-puuid";
        
        RiotMatchDTO matchDTO = new RiotMatchDTO();
        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        
        RiotMatchDTO.ParticipantDTO participant1 = new RiotMatchDTO.ParticipantDTO();
        participant1.setPuuid("other-puuid");
        
        RiotMatchDTO.ParticipantDTO participant2 = new RiotMatchDTO.ParticipantDTO();
        participant2.setPuuid(targetPuuid);
        participant2.setChampionName("Ahri");
        
        info.setParticipants(List.of(participant1, participant2));
        matchDTO.setInfo(info);
        
        // When - Using reflection to call private method
        try {
            java.lang.reflect.Method method = RiotService.class.getDeclaredMethod(
                "findParticipantByPuuid",
                RiotMatchDTO.class,
                String.class
            );
            method.setAccessible(true);
            RiotMatchDTO.ParticipantDTO result = (RiotMatchDTO.ParticipantDTO) method.invoke(
                riotService, 
                matchDTO, 
                targetPuuid
            );
            
            // Then
            assertNotNull(result);
            assertEquals("Ahri", result.getChampionName());
        } catch (Exception e) {
            fail("Failed to invoke findParticipantByPuuid: " + e.getMessage());
        }
    }
    
    @Test
    void testFindParticipantByPuuidNotFound() {
        // Given
        RiotMatchDTO matchDTO = new RiotMatchDTO();
        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        
        RiotMatchDTO.ParticipantDTO participant = new RiotMatchDTO.ParticipantDTO();
        participant.setPuuid("other-puuid");
        
        info.setParticipants(List.of(participant));
        matchDTO.setInfo(info);
        
        // When - Using reflection to call private method
        try {
            java.lang.reflect.Method method = RiotService.class.getDeclaredMethod(
                "findParticipantByPuuid",
                RiotMatchDTO.class,
                String.class
            );
            method.setAccessible(true);
            RiotMatchDTO.ParticipantDTO result = (RiotMatchDTO.ParticipantDTO) method.invoke(
                riotService, 
                matchDTO, 
                "non-existent-puuid"
            );
            
            // Then
            assertNull(result);
        } catch (Exception e) {
            fail("Failed to invoke findParticipantByPuuid: " + e.getMessage());
        }
    }
    
    @Test
    void testConvertMatchEntityToDTO() {
        // Given
        com.tfg.tfg.model.entity.MatchEntity matchEntity = new com.tfg.tfg.model.entity.MatchEntity();
        matchEntity.setMatchId("EUW1_123");
        matchEntity.setChampionName("Ahri");
        matchEntity.setChampionId(103);
        matchEntity.setWin(true);
        matchEntity.setKills(10);
        matchEntity.setDeaths(2);
        matchEntity.setAssists(15);
        matchEntity.setGameDuration(1800L);
        matchEntity.setTimestamp(java.time.LocalDateTime.now());
        
        when(dataDragonService.getChampionIconUrl(103L)).thenReturn("http://example.com/ahri.png");
        
        // When - Using reflection to call private method
        try {
            java.lang.reflect.Method method = RiotService.class.getDeclaredMethod(
                "convertMatchEntityToDTO",
                com.tfg.tfg.model.entity.MatchEntity.class
            );
            method.setAccessible(true);
            com.tfg.tfg.model.dto.MatchHistoryDTO result = 
                (com.tfg.tfg.model.dto.MatchHistoryDTO) method.invoke(riotService, matchEntity);
            
            // Then
            assertNotNull(result);
            assertEquals("EUW1_123", result.getMatchId());
            assertEquals("Ahri", result.getChampionName());
            assertEquals(true, result.getWin());
            assertEquals(10, result.getKills());
        } catch (Exception e) {
            fail("Failed to invoke convertMatchEntityToDTO: " + e.getMessage());
        }
    }
    
    @Test
    void testSaveMatchToDatabaseSummonerNotFound() {
        // Given - Mock summoner not found scenario
        RiotMatchDTO riotMatch = new RiotMatchDTO();
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        metadata.setMatchId("EUW1_12345");
        riotMatch.setMetadata(metadata);

        when(summonerRepository.findByPuuid("test-puuid")).thenReturn(Optional.empty());

        // When - Use reflection to invoke private method
        try {
            java.lang.reflect.Method method = RiotService.class.getDeclaredMethod(
                "saveMatchToDatabase", 
                RiotMatchDTO.class, 
                String.class
            );
            method.setAccessible(true);
            method.invoke(riotService, riotMatch, "test-puuid");

            // Then - Should not save anything
            verify(matchRepository, never()).save(any());
        } catch (Exception e) {
            fail("Failed to invoke saveMatchToDatabase: " + e.getMessage());
        }
    }

    @Test
    void testSaveMatchToDatabaseNullMatchId() {
        // Given - Mock match with null matchId
        RiotMatchDTO riotMatch = new RiotMatchDTO();
        riotMatch.setMetadata(null);  // Null metadata means null matchId

        Summoner summoner = new Summoner();
        summoner.setPuuid("test-puuid");
        when(summonerRepository.findByPuuid("test-puuid")).thenReturn(Optional.of(summoner));

        // When - Use reflection to invoke private method
        try {
            java.lang.reflect.Method method = RiotService.class.getDeclaredMethod(
                "saveMatchToDatabase", 
                RiotMatchDTO.class, 
                String.class
            );
            method.setAccessible(true);
            method.invoke(riotService, riotMatch, "test-puuid");

            // Then - Should not save anything
            verify(matchRepository, never()).save(any());
        } catch (Exception e) {
            fail("Failed to invoke saveMatchToDatabase: " + e.getMessage());
        }
    }

    @Test
    void testSaveMatchToDatabaseSuccess() {
        // Given - Full match with all data
        RiotMatchDTO riotMatch = new RiotMatchDTO();
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        metadata.setMatchId("EUW1_123456789");
        riotMatch.setMetadata(metadata);

        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        info.setGameCreation(1640000000000L);
        info.setGameDuration(1800L);
        info.setGameMode("CLASSIC");
        info.setQueueId(420);

        RiotMatchDTO.ParticipantDTO participant = new RiotMatchDTO.ParticipantDTO();
        participant.setPuuid("test-puuid");
        participant.setWin(true);
        participant.setKills(10);
        participant.setDeaths(2);
        participant.setAssists(8);
        participant.setChampionName("Ahri");
        participant.setChampionId(103);
        participant.setTeamPosition("MIDDLE");
        participant.setTotalDamageDealtToChampions(25000);
        participant.setGoldEarned(12000);
        participant.setChampLevel(16);
        participant.setSummonerName("TestSummoner");

        info.setParticipants(List.of(participant));
        riotMatch.setInfo(info);

        Summoner summoner = new Summoner();
        summoner.setPuuid("test-puuid");
        summoner.setLp(85);
        summoner.setTier("GOLD");
        summoner.setRank("II");
        
        when(summonerRepository.findByPuuid("test-puuid")).thenReturn(Optional.of(summoner));
        when(matchRepository.findByMatchId("EUW1_123456789")).thenReturn(Optional.empty());
        when(matchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When - Use reflection to invoke private method
        try {
            java.lang.reflect.Method method = RiotService.class.getDeclaredMethod(
                "saveMatchToDatabase", 
                RiotMatchDTO.class, 
                String.class
            );
            method.setAccessible(true);
            method.invoke(riotService, riotMatch, "test-puuid");

            // Then - Should save match with correct data
            ArgumentCaptor<MatchEntity> captor = ArgumentCaptor.forClass(MatchEntity.class);
            verify(matchRepository).save(captor.capture());
            
            MatchEntity saved = captor.getValue();
            assertEquals("EUW1_123456789", saved.getMatchId());
            assertEquals("Ahri", saved.getChampionName());
            assertEquals(true, saved.isWin());
            assertEquals(10, saved.getKills());
            assertEquals(85, saved.getLpAtMatch());
            assertEquals("GOLD", saved.getTierAtMatch());
        } catch (Exception e) {
            fail("Failed to invoke saveMatchToDatabase: " + e.getMessage());
        }
    }
}
