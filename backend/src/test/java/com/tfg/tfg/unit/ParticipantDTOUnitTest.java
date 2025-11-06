package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.ParticipantDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParticipantDTOUnitTest {

    @Test
    void testGettersAndSetters() {
        ParticipantDTO dto = new ParticipantDTO();
        
        dto.setSummonerName("TestPlayer");
        dto.setRiotIdGameName("GameName");
        dto.setRiotIdTagline("TAG");
        dto.setChampionName("Ahri");
        dto.setChampionIconUrl("http://icon.url");
        dto.setKills(10);
        dto.setDeaths(2);
        dto.setAssists(15);
        dto.setLevel(18);
        dto.setTotalMinionsKilled(200);
        dto.setGoldEarned(15000);
        dto.setTotalDamageDealtToChampions(25000);
        dto.setWin(true);
        dto.setTeamId(100);
        dto.setTeamPosition("MIDDLE");
        dto.setItem0(1001);
        dto.setItem1(1002);
        dto.setItem2(1003);
        dto.setItem3(1004);
        dto.setItem4(1005);
        dto.setItem5(1006);
        dto.setItem6(3340);
        
        assertEquals("TestPlayer", dto.getSummonerName());
        assertEquals("GameName", dto.getRiotIdGameName());
        assertEquals("TAG", dto.getRiotIdTagline());
        assertEquals("Ahri", dto.getChampionName());
        assertEquals("http://icon.url", dto.getChampionIconUrl());
        assertEquals(10, dto.getKills());
        assertEquals(2, dto.getDeaths());
        assertEquals(15, dto.getAssists());
        assertEquals(18, dto.getLevel());
        assertEquals(200, dto.getTotalMinionsKilled());
        assertEquals(15000, dto.getGoldEarned());
        assertEquals(25000, dto.getTotalDamageDealtToChampions());
        assertTrue(dto.getWin());
        assertEquals(100, dto.getTeamId());
        assertEquals("MIDDLE", dto.getTeamPosition());
        assertEquals(1001, dto.getItem0());
        assertEquals(1002, dto.getItem1());
        assertEquals(1003, dto.getItem2());
        assertEquals(1004, dto.getItem3());
        assertEquals(1005, dto.getItem4());
        assertEquals(1006, dto.getItem5());
        assertEquals(3340, dto.getItem6());
    }

    @Test
    void testKdaString() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setKills(10);
        dto.setDeaths(2);
        dto.setAssists(15);
        
        assertEquals("10/2/15", dto.getKda());
    }

    @Test
    void testKdaRatioWithDeaths() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setKills(10);
        dto.setDeaths(2);
        dto.setAssists(15);
        
        assertEquals(12.5, dto.getKdaRatio());
    }

    @Test
    void testKdaRatioWithZeroDeaths() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setKills(10);
        dto.setDeaths(0);
        dto.setAssists(15);
        
        assertEquals(25.0, dto.getKdaRatio());
    }

    @Test
    void testKdaRatioWithNullValues() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setDeaths(0);
        
        assertEquals(0.0, dto.getKdaRatio());
    }
}
