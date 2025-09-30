package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.ChampionStat;

class SummonerSimpleUnitTest {

    @Test
    void testSummonerCreation() {
        Summoner summoner = new Summoner();
        assertNotNull(summoner);
        assertNull(summoner.getId());
        assertNotNull(summoner.getMatches());
        assertNotNull(summoner.getChampionStats());
        assertTrue(summoner.getMatches().isEmpty());
        assertTrue(summoner.getChampionStats().isEmpty());
    }

    @Test
    void testSummonerParameterizedConstructor() {
        Summoner summoner = new Summoner("riot#123", "TestSummoner", 50);
        
        assertEquals("riot#123", summoner.getRiotId());
        assertEquals("TestSummoner", summoner.getName());
        assertEquals(50, summoner.getLevel());
        assertNotNull(summoner.getMatches());
        assertNotNull(summoner.getChampionStats());
    }

    @Test
    void testSummonerSettersAndGetters() {
        Summoner summoner = new Summoner();
        summoner.setId(1L);
        summoner.setRiotId("riot#456");
        summoner.setName("Player1");
        summoner.setLevel(30);
        summoner.setProfileIconId(123);
        summoner.setTier("GOLD");
        summoner.setRank("II");
        summoner.setLp(75);
        
        assertEquals(1L, summoner.getId());
        assertEquals("riot#456", summoner.getRiotId());
        assertEquals("Player1", summoner.getName());
        assertEquals(30, summoner.getLevel());
        assertEquals(123, summoner.getProfileIconId());
        assertEquals("GOLD", summoner.getTier());
        assertEquals("II", summoner.getRank());
        assertEquals(75, summoner.getLp());
    }

    @Test
    void testSummonerMatches() {
        Summoner summoner = new Summoner();
        List<MatchEntity> matches = new ArrayList<>();
        summoner.setMatches(matches);
        
        assertEquals(matches, summoner.getMatches());
        assertTrue(summoner.getMatches().isEmpty());
    }

    @Test
    void testSummonerChampionStats() {
        Summoner summoner = new Summoner();
        List<ChampionStat> championStats = new ArrayList<>();
        summoner.setChampionStats(championStats);
        
        assertEquals(championStats, summoner.getChampionStats());
        assertTrue(summoner.getChampionStats().isEmpty());
    }

    @Test
    void testSummonerWithNullValues() {
        Summoner summoner = new Summoner();
        summoner.setRiotId(null);
        summoner.setName(null);
        summoner.setLevel(null);
        summoner.setTier(null);
        summoner.setRank(null);
        summoner.setLp(null);
        
        assertNull(summoner.getRiotId());
        assertNull(summoner.getName());
        assertNull(summoner.getLevel());
        assertNull(summoner.getTier());
        assertNull(summoner.getRank());
        assertNull(summoner.getLp());
    }

    @Test
    void testSummonerHighLevel() {
        Summoner summoner = new Summoner("riot#789", "HighLevel", 500);
        
        assertEquals(500, summoner.getLevel());
        assertTrue(summoner.getLevel() > 400);
    }

    @Test
    void testSummonerLowLevelRank() {
        Summoner summoner = new Summoner();
        summoner.setLevel(1);
        summoner.setTier("UNRANKED");
        summoner.setLp(0);
        
        assertEquals(1, summoner.getLevel());
        assertEquals("UNRANKED", summoner.getTier());
        assertEquals(0, summoner.getLp());
    }

    @Test
    void testSummonerHighTierRank() {
        Summoner summoner = new Summoner();
        summoner.setTier("CHALLENGER");
        summoner.setRank("I");
        summoner.setLp(1000);
        
        assertEquals("CHALLENGER", summoner.getTier());
        assertEquals("I", summoner.getRank());
        assertEquals(1000, summoner.getLp());
    }

    @Test
    void testSummonerNegativeLP() {
        Summoner summoner = new Summoner();
        summoner.setLp(-5);
        
        assertEquals(-5, summoner.getLp());
        assertTrue(summoner.getLp() < 0);
    }
}