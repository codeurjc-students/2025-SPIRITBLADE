package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.RankHistoryDTO;
import com.tfg.tfg.model.entity.RankHistory;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.mapper.RankHistoryMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RankHistoryMapperTest {

    @Test
    void testToDTONullEntity() {
        RankHistoryDTO result = RankHistoryMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    void testToDTOValidEntity() {
        Summoner summoner = new Summoner();
        summoner.setName("TestSummoner");

        RankHistory entity = new RankHistory();
        entity.setId(1L);
        entity.setSummoner(summoner);
        entity.setTimestamp(LocalDateTime.of(2023, 1, 1, 12, 0));
        entity.setTier("GOLD");
        entity.setRank("II");
        entity.setLeaguePoints(75);
        entity.setWins(50);
        entity.setLosses(30);
        entity.setQueueType("RANKED_SOLO_5x5");
        entity.setLpChange(15);

        RankHistoryDTO result = RankHistoryMapper.toDTO(entity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TestSummoner", result.getSummonerName());
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0), result.getTimestamp());
        assertEquals("GOLD", result.getTier());
        assertEquals("II", result.getRank());
        assertEquals(75, result.getLeaguePoints());
        assertEquals(50, result.getWins());
        assertEquals(30, result.getLosses());
        assertEquals("RANKED_SOLO_5x5", result.getQueueType());
        assertEquals(15, result.getLpChange());
    }

    @Test
    void testToDTONullSummoner() {
        RankHistory entity = new RankHistory();
        entity.setId(1L);
        entity.setTier("GOLD");

        RankHistoryDTO result = RankHistoryMapper.toDTO(entity);

        assertNotNull(result);
        assertNull(result.getSummonerName());
    }

    @Test
    void testToSimpleDTONullEntity() {
        RankHistoryDTO result = RankHistoryMapper.toSimpleDTO(null);
        assertNull(result);
    }

    @Test
    void testToSimpleDTOValidEntity() {
        RankHistory entity = new RankHistory();
        entity.setTimestamp(LocalDateTime.of(2023, 1, 1, 12, 0));
        entity.setTier("GOLD");
        entity.setRank("II");
        entity.setLeaguePoints(75);
        entity.setWins(50);
        entity.setLosses(30);

        RankHistoryDTO result = RankHistoryMapper.toSimpleDTO(entity);

        assertNotNull(result);
        assertEquals("2023-01-01T12:00", result.getDate()); // String representation
        assertEquals("GOLD", result.getTier());
        assertEquals("II", result.getRank());
        assertEquals(75, result.getLeaguePoints());
        assertEquals(50, result.getWins());
        assertEquals(30, result.getLosses());
        // Other fields should be null
        assertNull(result.getId());
        assertNull(result.getSummonerName());
        assertNull(result.getQueueType());
        assertNull(result.getLpChange());
    }

    @Test
    void testToSimpleDTONullTimestamp() {
        RankHistory entity = new RankHistory();
        entity.setTier("GOLD");

        RankHistoryDTO result = RankHistoryMapper.toSimpleDTO(entity);

        assertNotNull(result);
        assertNull(result.getTimestamp());
    }
}