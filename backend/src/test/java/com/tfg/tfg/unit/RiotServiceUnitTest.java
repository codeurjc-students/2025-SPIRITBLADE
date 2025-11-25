package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
import com.tfg.tfg.repository.MatchRepository;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.service.DataDragonService;
import com.tfg.tfg.service.RankHistoryService;
import com.tfg.tfg.service.RiotService;

@ExtendWith(MockitoExtension.class)
class RiotServiceUnitTest {

    @Mock
    private SummonerRepository summonerRepository;
    
    @Mock
    private MatchRepository matchRepository;
    
    @Mock
    private DataDragonService dataDragonService;
    
    @Mock
    private RankHistoryService rankHistoryService;
    
    @Mock
    private RestTemplate restTemplate;
    
    private RiotService riotService;
    
    @BeforeEach
    void setUp() throws Exception {
        riotService = new RiotService(summonerRepository, matchRepository, dataDragonService, rankHistoryService);
        
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
    void testGetSummonerByNameUpdateExisting() {
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
        
        Summoner existingSummoner = new Summoner();
        existingSummoner.setPuuid(puuid);
        existingSummoner.setName("OldName");
        
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
        when(summonerRepository.findByPuuid(puuid)).thenReturn(Optional.of(existingSummoner));
        when(summonerRepository.save(any(Summoner.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        SummonerDTO result = riotService.getSummonerByName(riotId);
        
        // Then
        assertNotNull(result);
        assertEquals(riotId, result.getName());
        verify(summonerRepository).save(any(Summoner.class));
    }

    @Test
    void testGetSummonerByNameInvalidRiotIdFormat() {
        // Given - Riot ID without # separator
        String invalidRiotId = "TestPlayer";
        
        // When & Then - Should throw SummonerNotFoundException when not found in database
        SummonerNotFoundException exception = assertThrows(
            SummonerNotFoundException.class,
            () -> riotService.getSummonerByName(invalidRiotId)
        );
        
        assertTrue(exception.getMessage().contains("Invalid Riot ID format"));
        verify(summonerRepository).findByName(invalidRiotId);
    }

    @Test
    void testGetSummonerByNameDatabaseFallback() {
        // Given - Invalid Riot ID format, but exists in database
        String invalidRiotId = "TestPlayer";
        Summoner summonerEntity = new Summoner();
        summonerEntity.setName("TestPlayer");
        summonerEntity.setPuuid("test-puuid");
        summonerEntity.setLevel(50);
        
        when(summonerRepository.findByName(invalidRiotId)).thenReturn(Optional.of(summonerEntity));
        
        // When
        SummonerDTO result = riotService.getSummonerByName(invalidRiotId);
        
        // Then - Should return DTO from database
        assertNotNull(result);
        assertEquals("TestPlayer", result.getName());
        verify(summonerRepository).findByName(invalidRiotId);
    }

    @Test
    void testGetSummonerByNameGenericException() {
        // Given
        String riotId = "TestPlayer#EUW";
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(RiotAccountDTO.class),
            anyString(),
            anyString(),
            anyString()
        )).thenThrow(new RuntimeException("Network error"));
        
        // When & Then - Should throw RiotApiException
        assertThrows(com.tfg.tfg.exception.RiotApiException.class, () -> {
            riotService.getSummonerByName(riotId);
        });
    }

    @Test
    void testGetMatchHistoryWithNullMatchIds() {
        // Given
        String puuid = "test-puuid";
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(String[].class),
            anyString(),
            anyInt(),
            anyInt(),
            anyString()
        )).thenReturn(ResponseEntity.ok((String[]) null));
        
        // When
        var result = riotService.getMatchHistory(puuid, 0, 10);
        
        // Then - Should return empty list when matchIds is null
        assertNotNull(result);
        assertTrue(result.isEmpty());
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
    void testGetTopChampionMasteriesGenericException() {
        // Given
        String puuid = "test-puuid";
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(RiotChampionMasteryDTO[].class),
            anyString(),
            anyInt(),
            anyString()
        )).thenThrow(new RuntimeException("Network error"));
        
        // When
        List<RiotChampionMasteryDTO> result = riotService.getTopChampionMasteries(puuid, 5);
        
        // Then - Returns empty list on generic exception
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
    void testGetMatchHistoryCacheFresh() {
        // Given
        String puuid = "test-puuid";
        Summoner summoner = new Summoner();
        summoner.setPuuid(puuid);
        
        MatchEntity recentMatch = new MatchEntity();
        recentMatch.setMatchId("EUW1_123");
        
        when(summonerRepository.findByPuuid(puuid)).thenReturn(Optional.of(summoner));
        when(matchRepository.findRecentMatchesBySummoner(any(), any())).thenReturn(List.of(recentMatch));
        
        // Mock the API call for freshness check - same match ID
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(String[].class),
            eq(puuid),
            eq(0),
            eq(1),
            anyString()
        )).thenReturn(ResponseEntity.ok(new String[]{"EUW1_123"}));
        
        // When
        var result = riotService.getMatchHistory(puuid, 0, 5);
        
        // Then - Should return cached matches without API call for details
        assertNotNull(result);
        // Since cache is fresh, it should use cached matches
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
    void testGetMatchHistoryFetchFromAPI() {
        // Given
        String puuid = "test-puuid";
        String[] matchIds = {"EUW1_123", "EUW1_456"};
        
        // Mock cache as not fresh (no summoner in DB)
        when(summonerRepository.findByPuuid(puuid)).thenReturn(Optional.empty());
        
        // Mock API call for match IDs
        when(restTemplate.exchange(
            contains("/lol/match/v5/matches/by-puuid/"),
            eq(HttpMethod.GET),
            isNull(),
            eq(String[].class),
            eq(puuid),
            eq(0),
            eq(10),
            anyString()
        )).thenReturn(ResponseEntity.ok(matchIds));
        
        // Mock match not in cache
        when(matchRepository.findByMatchId("EUW1_123")).thenReturn(Optional.empty());
        when(matchRepository.findByMatchId("EUW1_456")).thenReturn(Optional.empty());
        
        // Mock API call for match details
        RiotMatchDTO riotMatch = new RiotMatchDTO();
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        metadata.setMatchId("EUW1_123");
        riotMatch.setMetadata(metadata);
        
        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        info.setGameCreation(1640995200000L); // Some timestamp
        info.setGameDuration(1800L);
        info.setGameMode("CLASSIC");
        info.setQueueId(420);
        
        RiotMatchDTO.ParticipantDTO participant = new RiotMatchDTO.ParticipantDTO();
        participant.setPuuid(puuid);
        participant.setWin(true);
        participant.setKills(10);
        participant.setDeaths(5);
        participant.setAssists(15);
        participant.setChampionName("Ahri");
        participant.setChampionId(103);
        participant.setTeamPosition("MIDDLE");
        participant.setTotalDamageDealtToChampions(25000);
        participant.setGoldEarned(12000);
        participant.setChampLevel(16);
        participant.setSummonerName("TestPlayer");
        
        info.setParticipants(List.of(participant));
        riotMatch.setInfo(info);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(RiotMatchDTO.class),
            eq("EUW1_123"),
            anyString()
        )).thenReturn(ResponseEntity.ok(riotMatch));
        
        // Mock for second match
        RiotMatchDTO riotMatch2 = new RiotMatchDTO();
        RiotMatchDTO.MetadataDTO metadata2 = new RiotMatchDTO.MetadataDTO();
        metadata2.setMatchId("EUW1_456");
        riotMatch2.setMetadata(metadata2);
        
        RiotMatchDTO.InfoDTO info2 = new RiotMatchDTO.InfoDTO();
        info2.setGameCreation(1640995200000L);
        info2.setGameDuration(1800L);
        info2.setGameMode("CLASSIC");
        info2.setQueueId(420);
        
        RiotMatchDTO.ParticipantDTO participant2 = new RiotMatchDTO.ParticipantDTO();
        participant2.setPuuid(puuid);
        participant2.setWin(false);
        participant2.setKills(5);
        participant2.setDeaths(10);
        participant2.setAssists(8);
        participant2.setChampionName("Jinx");
        participant2.setChampionId(222);
        participant2.setTeamPosition("BOTTOM");
        participant2.setTotalDamageDealtToChampions(20000);
        participant2.setGoldEarned(10000);
        participant2.setChampLevel(14);
        participant2.setSummonerName("TestPlayer");
        
        info2.setParticipants(List.of(participant2));
        riotMatch2.setInfo(info2);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(RiotMatchDTO.class),
            eq("EUW1_456"),
            anyString()
        )).thenReturn(ResponseEntity.ok(riotMatch2));
        
        // When
        var result = riotService.getMatchHistory(puuid, 0, 10);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Verify that fetchMatchDetails and fetchAndAddMatchToHistory were called
    }

    @Test
    void testGetMatchDetailsNullBody() {
        // Given
        String matchId = "EUW1_123";
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(RiotMatchDTO.class),
            anyString(),
            anyString()
        )).thenReturn(ResponseEntity.ok(null));
        
        // When
        var result = riotService.getMatchDetails(matchId);
        
        // Then
        assertNull(result);
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
    void testSaveMatchToDatabaseMatchAlreadyExists() {
        // Given - Mock match already exists scenario
        RiotMatchDTO riotMatch = new RiotMatchDTO();
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        metadata.setMatchId("EUW1_12345");
        riotMatch.setMetadata(metadata);

        Summoner summoner = new Summoner();
        summoner.setPuuid("test-puuid");
        when(summonerRepository.findByPuuid("test-puuid")).thenReturn(Optional.of(summoner));
        when(matchRepository.findByMatchId("EUW1_12345")).thenReturn(Optional.of(new MatchEntity()));

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
    void testSaveMatchToDatabaseParticipantNotFound() {
        // Given - Mock participant not found scenario
        RiotMatchDTO riotMatch = new RiotMatchDTO();
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        metadata.setMatchId("EUW1_12345");
        riotMatch.setMetadata(metadata);

        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        info.setParticipants(Collections.emptyList()); // No participants
        riotMatch.setInfo(info);

        Summoner summoner = new Summoner();
        summoner.setPuuid("test-puuid");
        when(summonerRepository.findByPuuid("test-puuid")).thenReturn(Optional.of(summoner));
        when(matchRepository.findByMatchId("EUW1_12345")).thenReturn(Optional.empty());

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
        // Given - Mock successful save scenario
        RiotMatchDTO riotMatch = new RiotMatchDTO();
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        metadata.setMatchId("EUW1_12345");
        riotMatch.setMetadata(metadata);

        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        info.setGameCreation(1640995200000L); // Some timestamp
        info.setGameDuration(1800L);
        info.setGameMode("CLASSIC");
        info.setQueueId(420);

        // Create participant for the PUUID
        RiotMatchDTO.ParticipantDTO participant = new RiotMatchDTO.ParticipantDTO();
        participant.setPuuid("test-puuid");
        participant.setWin(true);
        participant.setKills(10);
        participant.setDeaths(5);
        participant.setAssists(15);
        participant.setChampionName("Ahri");
        participant.setChampionId(103);
        participant.setTeamPosition("MIDDLE");
        participant.setTotalDamageDealtToChampions(25000);
        participant.setGoldEarned(12000);
        participant.setChampLevel(16);
        participant.setSummonerName("TestPlayer");

        info.setParticipants(List.of(participant));
        riotMatch.setInfo(info);

        Summoner summoner = new Summoner();
        summoner.setPuuid("test-puuid");
        summoner.setId(1L);

        when(summonerRepository.findByPuuid("test-puuid")).thenReturn(Optional.of(summoner));
        when(matchRepository.findByMatchId("EUW1_12345")).thenReturn(Optional.empty());
        when(matchRepository.save(any(MatchEntity.class))).thenReturn(new MatchEntity());

        // When - Use reflection to invoke private method
        try {
            java.lang.reflect.Method method = RiotService.class.getDeclaredMethod(
                "saveMatchToDatabase",
                RiotMatchDTO.class,
                String.class
            );
            method.setAccessible(true);
            method.invoke(riotService, riotMatch, "test-puuid");

            // Then - Should save the match
            verify(matchRepository).save(any(MatchEntity.class));
        } catch (Exception e) {
            fail("Failed to invoke saveMatchToDatabase: " + e.getMessage());
        }
    }

    @Test
    void testSaveSummonerToDatabaseDataAccessException() {
        // Given
        SummonerDTO dto = new SummonerDTO();
        dto.setPuuid("test-puuid");
        dto.setName("TestPlayer#EUW");
        
        when(summonerRepository.findByPuuid("test-puuid")).thenReturn(Optional.empty());
        when(summonerRepository.save(any(Summoner.class))).thenThrow(new org.springframework.dao.DataAccessException("DB Error") {});
        
        // When - Use reflection to invoke private method
        try {
            java.lang.reflect.Method method = RiotService.class.getDeclaredMethod(
                "saveSummonerToDatabase", 
                SummonerDTO.class
            );
            method.setAccessible(true);
            method.invoke(riotService, dto);
            
            // Then - Should not throw, just log
            verify(summonerRepository).save(any(Summoner.class));
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }
}
