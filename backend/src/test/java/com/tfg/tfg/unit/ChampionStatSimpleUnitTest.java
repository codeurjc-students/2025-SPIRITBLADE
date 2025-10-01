package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.entity.ChampionStat;
import com.tfg.tfg.model.entity.Summoner;

class ChampionStatSimpleUnitTest {

    @Test
    void testChampionStatCreation() {
        ChampionStat championStat = new ChampionStat();
        assertNotNull(championStat);
        assertNull(championStat.getId());
        assertNull(championStat.getChampionId());
        assertEquals(0, championStat.getGamesPlayed());
        assertEquals(0, championStat.getWins());
        assertEquals(0, championStat.getKills());
        assertEquals(0, championStat.getDeaths());
        assertEquals(0, championStat.getAssists());
        assertNull(championStat.getSummoner());
    }

    @Test
    void testChampionStatParameterizedConstructor() {
        Summoner summoner = new Summoner("riot#123", "TestSummoner", 30);
        ChampionStat championStat = new ChampionStat(101, summoner);
        
        assertEquals(101, championStat.getChampionId());
        assertEquals(summoner, championStat.getSummoner());
        assertEquals(0, championStat.getGamesPlayed());
        assertEquals(0, championStat.getWins());
    }

    @Test
    void testChampionStatSettersAndGetters() {
        ChampionStat championStat = new ChampionStat();
        Summoner summoner = new Summoner();
        
        championStat.setId(1L);
        championStat.setChampionId(103);
        championStat.setGamesPlayed(25);
        championStat.setWins(18);
        championStat.setKills(150);
        championStat.setDeaths(75);
        championStat.setAssists(200);
        championStat.setSummoner(summoner);
        
        assertEquals(1L, championStat.getId());
        assertEquals(103, championStat.getChampionId());
        assertEquals(25, championStat.getGamesPlayed());
        assertEquals(18, championStat.getWins());
        assertEquals(150, championStat.getKills());
        assertEquals(75, championStat.getDeaths());
        assertEquals(200, championStat.getAssists());
        assertEquals(summoner, championStat.getSummoner());
    }

    @Test
    void testChampionStatWithZeroValues() {
        ChampionStat championStat = new ChampionStat();
        championStat.setGamesPlayed(0);
        championStat.setWins(0);
        championStat.setKills(0);
        championStat.setDeaths(0);
        championStat.setAssists(0);
        
        assertEquals(0, championStat.getGamesPlayed());
        assertEquals(0, championStat.getWins());
        assertEquals(0, championStat.getKills());
        assertEquals(0, championStat.getDeaths());
        assertEquals(0, championStat.getAssists());
    }

    @Test
    void testChampionStatWithHighValues() {
        ChampionStat championStat = new ChampionStat();
        championStat.setGamesPlayed(1000);
        championStat.setWins(750);
        championStat.setKills(15000);
        championStat.setDeaths(8000);
        championStat.setAssists(20000);
        
        assertEquals(1000, championStat.getGamesPlayed());
        assertEquals(750, championStat.getWins());
        assertEquals(15000, championStat.getKills());
        assertEquals(8000, championStat.getDeaths());
        assertEquals(20000, championStat.getAssists());
    }

    @Test
    void testChampionStatWinRate() {
        ChampionStat championStat = new ChampionStat();
        championStat.setGamesPlayed(20);
        championStat.setWins(12);
        
        double winRate = (double) championStat.getWins() / championStat.getGamesPlayed();
        assertEquals(0.6, winRate, 0.01);
    }

    @Test
    void testChampionStatKDA() {
        ChampionStat championStat = new ChampionStat();
        championStat.setKills(100);
        championStat.setDeaths(50);
        championStat.setAssists(150);
        
        double kda = (double) (championStat.getKills() + championStat.getAssists()) / championStat.getDeaths();
        assertEquals(5.0, kda, 0.01);
    }

    @Test
    void testChampionStatPerfectKDA() {
        ChampionStat championStat = new ChampionStat();
        championStat.setKills(10);
        championStat.setDeaths(0);
        championStat.setAssists(5);
        
        // Perfect KDA when deaths = 0
        boolean isPerfectKDA = championStat.getDeaths() == 0;
        assertTrue(isPerfectKDA);
    }

    @Test
    void testChampionStatAssociation() {
        Summoner summoner = new Summoner("riot#456", "Player", 50);
        ChampionStat championStat = new ChampionStat(266, summoner);
        
        assertEquals(summoner, championStat.getSummoner());
        assertEquals("Player", championStat.getSummoner().getName());
    }

    @Test
    void testChampionStatNullChampionId() {
        ChampionStat championStat = new ChampionStat();
        championStat.setChampionId(null);
        
        assertNull(championStat.getChampionId());
    }
}