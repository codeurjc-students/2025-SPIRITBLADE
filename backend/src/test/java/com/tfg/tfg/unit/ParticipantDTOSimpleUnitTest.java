package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.ParticipantDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantDTOSimpleUnitTest {

    @Test
    void testDefaultConstructor() {
        ParticipantDTO dto = new ParticipantDTO();
        assertNotNull(dto);
        assertNull(dto.getSummonerName());
    }

    @Test
    void testBasicFields() {
        ParticipantDTO dto = new ParticipantDTO();
        
        dto.setSummonerName("TestPlayer");
        dto.setRiotIdGameName("TestPlayer");
        dto.setRiotIdTagline("EUW");
        dto.setChampionName("Ahri");
        dto.setChampionIconUrl("https://icon.url");
        dto.setKills(10);
        dto.setDeaths(3);
        dto.setAssists(15);
        dto.setLevel(18);
        dto.setTotalMinionsKilled(200);
        dto.setGoldEarned(15000);
        dto.setTotalDamageDealtToChampions(35000);
        dto.setWin(true);
        dto.setTeamId(100);
        dto.setTeamPosition("MIDDLE");
        
        assertEquals("TestPlayer", dto.getSummonerName());
        assertEquals("TestPlayer", dto.getRiotIdGameName());
        assertEquals("EUW", dto.getRiotIdTagline());
        assertEquals("Ahri", dto.getChampionName());
        assertEquals("https://icon.url", dto.getChampionIconUrl());
        assertEquals(10, dto.getKills());
        assertEquals(3, dto.getDeaths());
        assertEquals(15, dto.getAssists());
        assertEquals(18, dto.getLevel());
        assertEquals(200, dto.getTotalMinionsKilled());
        assertEquals(15000, dto.getGoldEarned());
        assertEquals(35000, dto.getTotalDamageDealtToChampions());
        assertTrue(dto.getWin());
        assertEquals(100, dto.getTeamId());
        assertEquals("MIDDLE", dto.getTeamPosition());
    }

    @Test
    void testItems() {
        ParticipantDTO dto = new ParticipantDTO();
        
        dto.setItem0(3089); // Rabadon's
        dto.setItem1(3020); // Sorcerer's Shoes
        dto.setItem2(3165); // Morellonomicon
        dto.setItem3(3135); // Void Staff
        dto.setItem4(3157); // Zhonya's
        dto.setItem5(3102); // Banshee's
        dto.setItem6(3340); // Trinket
        
        assertEquals(3089, dto.getItem0());
        assertEquals(3020, dto.getItem1());
        assertEquals(3165, dto.getItem2());
        assertEquals(3135, dto.getItem3());
        assertEquals(3157, dto.getItem4());
        assertEquals(3102, dto.getItem5());
        assertEquals(3340, dto.getItem6());
    }

    @Test
    void testGetKda() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setKills(10);
        dto.setDeaths(5);
        dto.setAssists(8);
        
        String kda = dto.getKda();
        assertEquals("10/5/8", kda);
    }

    @Test
    void testGetKdaRatio_WithDeaths() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setKills(10);
        dto.setDeaths(5);
        dto.setAssists(15);
        
        Double kdaRatio = dto.getKdaRatio();
        assertEquals(5.0, kdaRatio); // (10+15)/5 = 5.0
    }

    @Test
    void testGetKdaRatio_WithoutDeaths() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setKills(10);
        dto.setDeaths(0);
        dto.setAssists(15);
        
        Double kdaRatio = dto.getKdaRatio();
        assertEquals(25.0, kdaRatio); // Perfect KDA: 10+15
    }

    @Test
    void testGetKdaRatio_NullDeaths() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setKills(5);
        dto.setDeaths(null);
        dto.setAssists(10);
        
        Double kdaRatio = dto.getKdaRatio();
        assertEquals(15.0, kdaRatio); // 5+10
    }

    @Test
    void testGetKdaRatio_AllZero() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setKills(0);
        dto.setDeaths(0);
        dto.setAssists(0);
        
        Double kdaRatio = dto.getKdaRatio();
        assertEquals(0.0, kdaRatio);
    }

    @Test
    void testGetKdaRatio_NullValues() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setKills(null);
        dto.setDeaths(5);
        dto.setAssists(null);
        
        Double kdaRatio = dto.getKdaRatio();
        assertEquals(0.0, kdaRatio);
    }

    @Test
    void testTeamPositions() {
        ParticipantDTO top = new ParticipantDTO();
        top.setTeamPosition("TOP");
        assertEquals("TOP", top.getTeamPosition());
        
        ParticipantDTO jungle = new ParticipantDTO();
        jungle.setTeamPosition("JUNGLE");
        assertEquals("JUNGLE", jungle.getTeamPosition());
        
        ParticipantDTO middle = new ParticipantDTO();
        middle.setTeamPosition("MIDDLE");
        assertEquals("MIDDLE", middle.getTeamPosition());
        
        ParticipantDTO bottom = new ParticipantDTO();
        bottom.setTeamPosition("BOTTOM");
        assertEquals("BOTTOM", bottom.getTeamPosition());
        
        ParticipantDTO utility = new ParticipantDTO();
        utility.setTeamPosition("UTILITY");
        assertEquals("UTILITY", utility.getTeamPosition());
    }

    @Test
    void testCompleteParticipant() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setSummonerName("Faker");
        dto.setRiotIdGameName("Hide on bush");
        dto.setRiotIdTagline("KR1");
        dto.setChampionName("LeBlanc");
        dto.setChampionIconUrl("https://ddragon.leagueoflegends.com/cdn/13.21.1/img/champion/Leblanc.png");
        dto.setKills(12);
        dto.setDeaths(2);
        dto.setAssists(10);
        dto.setLevel(18);
        dto.setTotalMinionsKilled(250);
        dto.setGoldEarned(18000);
        dto.setTotalDamageDealtToChampions(45000);
        dto.setWin(true);
        dto.setTeamId(100);
        dto.setTeamPosition("MIDDLE");
        dto.setItem0(3089);
        dto.setItem1(3020);
        dto.setItem2(3165);
        dto.setItem3(3135);
        dto.setItem4(3157);
        dto.setItem5(3102);
        dto.setItem6(3340);
        
        assertEquals("12/2/10", dto.getKda());
        assertEquals(11.0, dto.getKdaRatio()); // (12+10)/2 = 11.0
        assertTrue(dto.getWin());
        assertEquals("MIDDLE", dto.getTeamPosition());
    }

    @Test
    void testSupportParticipant() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setSummonerName("KeriaSupport");
        dto.setChampionName("Thresh");
        dto.setKills(1);
        dto.setDeaths(3);
        dto.setAssists(25);
        dto.setTeamPosition("UTILITY");
        
        assertEquals("1/3/25", dto.getKda());
        assertEquals(8.666666666666666, dto.getKdaRatio(), 0.01); // (1+25)/3
        assertEquals("UTILITY", dto.getTeamPosition());
    }
}
