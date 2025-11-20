package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.mapper.MatchMapper;
import com.tfg.tfg.service.DataDragonService;
import com.tfg.tfg.service.RankHistoryService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class MatchMapperTest {

    @Mock
    private DataDragonService dataDragonService;

    @Mock
    private RankHistoryService rankHistoryService;

    @Test
    void testToEntityNullDTO() {
        MatchEntity result = MatchMapper.toEntity(null, new Summoner());
        assertNull(result);
    }

    @Test
    void testToEntityValidDTO() {
        Summoner summoner = new Summoner();
        summoner.setId(1L);

        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setMatchId("EUW1_123456789");
        dto.setChampionName("TestChampion");
        dto.setWin(true);
        dto.setKills(10);
        dto.setDeaths(5);
        dto.setAssists(15);
        dto.setGameDuration(1800L);
        dto.setGameTimestamp(1700000000L);
        dto.setQueueId(420);

        MatchEntity result = MatchMapper.toEntity(dto, summoner);

        assertNotNull(result);
        assertEquals("EUW1_123456789", result.getMatchId());
        assertEquals(summoner, result.getSummoner());
        assertEquals("TestChampion", result.getChampionName());
        assertTrue(result.isWin());
        assertEquals(10, result.getKills());
        assertEquals(5, result.getDeaths());
        assertEquals(15, result.getAssists());
        assertEquals(1800L, result.getGameDuration());
        assertEquals(420, result.getQueueId());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void testToEntityWithNullValues() {
        Summoner summoner = new Summoner();

        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setMatchId("EUW1_123456789");
        dto.setChampionName("TestChampion");
        // Leave win, kills, deaths, assists null

        MatchEntity result = MatchMapper.toEntity(dto, summoner);

        assertNotNull(result);
        assertEquals("TestChampion", result.getChampionName());
        assertFalse(result.isWin()); // null should become false
        assertEquals(0, result.getKills()); // null should become 0
        assertEquals(0, result.getDeaths()); // null should become 0
        assertEquals(0, result.getAssists()); // null should become 0
    }

    @Test
    void testToEntityUpdateExisting() {
        Summoner summoner = new Summoner();
        summoner.setId(1L);

        MatchEntity existing = new MatchEntity();
        existing.setId(100L);
        existing.setMatchId("old_id");

        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setMatchId("EUW1_123456789");
        dto.setChampionName("TestChampion");
        dto.setWin(true);

        MatchEntity result = MatchMapper.toEntity(existing, dto, summoner);

        assertNotNull(result);
        assertEquals(100L, result.getId()); // Should preserve existing ID
        assertEquals("EUW1_123456789", result.getMatchId()); // Should update
        assertEquals(summoner, result.getSummoner());
        assertEquals("TestChampion", result.getChampionName());
        assertTrue(result.isWin());
    }

    @Test
    void testToDTONullEntity() {
        MatchHistoryDTO result = MatchMapper.toDTO(null, dataDragonService, rankHistoryService);
        assertNull(result);
    }

    @Test
    void testToDTOValidEntity() {
        lenient().when(dataDragonService.getChampionIconUrl(anyLong())).thenReturn("http://example.com/champion.png");
        lenient().when(rankHistoryService.getLpForMatch(1L)).thenReturn(Optional.of(1500));
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 1, 12, 0);
        MatchEntity entity = new MatchEntity();
        entity.setId(1L);
        entity.setMatchId("EUW1_123456789");
        entity.setChampionName("TestChampion");
        entity.setChampionId(1);
        entity.setWin(true);
        entity.setKills(10);
        entity.setDeaths(5);
        entity.setAssists(15);
        entity.setGameDuration(1800L);
        entity.setQueueId(420);
        entity.setTimestamp(timestamp);

        MatchHistoryDTO result = MatchMapper.toDTO(entity, dataDragonService, rankHistoryService);

        assertNotNull(result);
        assertEquals("EUW1_123456789", result.getMatchId());
        assertEquals("TestChampion", result.getChampionName());
        assertEquals("http://example.com/champion.png", result.getChampionIconUrl());
        assertTrue(result.getWin());
        assertEquals(10, result.getKills());
        assertEquals(5, result.getDeaths());
        assertEquals(15, result.getAssists());
        assertEquals(1800L, result.getGameDuration());
        assertEquals(timestamp, LocalDateTime.ofEpochSecond(result.getGameTimestamp(), 0, java.time.ZoneOffset.UTC));
        assertEquals(Integer.valueOf(420), result.getQueueId());
        assertEquals(Integer.valueOf(1500), result.getLpAtMatch());
    }

    @Test
    void testToDTONullServices() {
        MatchEntity entity = new MatchEntity();
        entity.setId(1L);
        entity.setMatchId("EUW1_123456789");
        entity.setChampionName("TestChampion");
        entity.setWin(true);

        MatchHistoryDTO result = MatchMapper.toDTO(entity, null, null);

        assertNotNull(result);
        assertEquals("EUW1_123456789", result.getMatchId());
        assertEquals("TestChampion", result.getChampionName());
        assertNull(result.getChampionIconUrl());
        assertNull(result.getLpAtMatch());
    }

    @Test
    void testToDTODataDragonServiceException() {
        lenient().when(dataDragonService.getChampionIconUrl(anyLong())).thenThrow(new RuntimeException("Service error"));

        MatchEntity entity = new MatchEntity();
        entity.setId(1L);
        entity.setChampionId(1);
        entity.setChampionName("TestChampion");

        MatchHistoryDTO result = MatchMapper.toDTO(entity, dataDragonService, null);

        assertNotNull(result);
        assertEquals("TestChampion", result.getChampionName());
        // Should not throw exception, icon URL should be null or empty
    }

    @Test
    void testToDTONullTimestamp() {
        MatchEntity entity = new MatchEntity();
        entity.setId(1L);
        entity.setMatchId("EUW1_123456789");

        MatchHistoryDTO result = MatchMapper.toDTO(entity, dataDragonService, rankHistoryService);

        assertNotNull(result);
        assertNull(result.getGameTimestamp());
    }
}