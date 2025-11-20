package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.mapper.SummonerMapper;
import com.tfg.tfg.service.DataDragonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SummonerMapperTest {

    @Mock
    private DataDragonService dataDragonService;

    @Test
    void testToDTONullSummoner() {
        SummonerDTO result = SummonerMapper.toDTO(null, dataDragonService);
        assertNull(result);
    }

    @Test
    void testToDTOValidSummoner() {
        lenient().when(dataDragonService.getProfileIconUrl(1234)).thenReturn("http://example.com/profileicon.png");

        Summoner summoner = new Summoner();
        summoner.setId(1L);
        summoner.setRiotId("test_riot_id");
        summoner.setPuuid("test_puuid");
        summoner.setName("TestSummoner");
        summoner.setLevel(50);
        summoner.setProfileIconId(1234);
        summoner.setTier("GOLD");
        summoner.setRank("II");
        summoner.setLp(75);
        summoner.setWins(100);
        summoner.setLosses(80);
        summoner.setLastSearchedAt(LocalDateTime.of(2023, 1, 1, 12, 0));

        SummonerDTO result = SummonerMapper.toDTO(summoner, dataDragonService);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test_riot_id", result.getRiotId());
        assertEquals("test_puuid", result.getPuuid());
        assertEquals("TestSummoner", result.getName());
        assertEquals(50, result.getLevel());
        assertEquals("http://example.com/profileicon.png", result.getProfileIconUrl());
        assertEquals("GOLD", result.getTier());
        assertEquals("II", result.getRank());
        assertEquals(75, result.getLp());
        assertEquals(100, result.getWins());
        assertEquals(80, result.getLosses());
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0), result.getLastSearchedAt());
    }

    @Test
    void testToDTONullDataDragonService() {
        Summoner summoner = new Summoner();
        summoner.setId(1L);
        summoner.setName("TestSummoner");
        summoner.setProfileIconId(1234);

        SummonerDTO result = SummonerMapper.toDTO(summoner, null);

        assertNotNull(result);
        assertEquals("TestSummoner", result.getName());
        assertNull(result.getProfileIconUrl());
    }

    @Test
    void testToEntityNullDTO() {
        Summoner result = SummonerMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void testToEntityValidDTO() {
        SummonerDTO dto = new SummonerDTO();
        dto.setRiotId("test_riot_id");
        dto.setPuuid("test_puuid");
        dto.setName("TestSummoner");
        dto.setLevel(50);
        dto.setProfileIconId(1234);
        dto.setTier("GOLD");
        dto.setRank("II");
        dto.setLp(75);
        dto.setWins(100);
        dto.setLosses(80);

        Summoner result = SummonerMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals("test_riot_id", result.getRiotId());
        assertEquals("test_puuid", result.getPuuid());
        assertEquals("TestSummoner", result.getName());
        assertEquals(50, result.getLevel());
        assertEquals(1234, result.getProfileIconId());
        assertEquals("GOLD", result.getTier());
        assertEquals("II", result.getRank());
        assertEquals(75, result.getLp());
        assertEquals(100, result.getWins());
        assertEquals(80, result.getLosses());
        assertNull(result.getLastSearchedAt()); // Should not be set
    }

    @Test
    void testToEntityWithNullFields() {
        SummonerDTO dto = new SummonerDTO();
        dto.setName("TestSummoner");
        // Leave other fields null

        Summoner result = SummonerMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals("TestSummoner", result.getName());
        assertNull(result.getRiotId());
        assertNull(result.getPuuid());
        assertNull(result.getLevel());
        assertNull(result.getProfileIconId());
        assertNull(result.getTier());
        assertNull(result.getRank());
        assertNull(result.getLp());
        assertNull(result.getWins());
        assertNull(result.getLosses());
    }
}