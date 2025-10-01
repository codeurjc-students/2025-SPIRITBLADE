package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.entity.ChampionStat;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;

class SummonerUnitTest {

    private Summoner summoner;

    @BeforeEach
    void setUp() {
        summoner = new Summoner();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(summoner);
        assertNull(summoner.getId());
        assertNull(summoner.getRiotId());
        assertNull(summoner.getName());
        assertNull(summoner.getLevel());
        assertNotNull(summoner.getMatches());
        assertNotNull(summoner.getChampionStats());
        assertTrue(summoner.getMatches().isEmpty());
        assertTrue(summoner.getChampionStats().isEmpty());
    }

    @Test
    void testParameterizedConstructor() {
        Summoner paramSummoner = new Summoner("RIOT123", "TestPlayer", 30);
        
        assertEquals("RIOT123", paramSummoner.getRiotId());
        assertEquals("TestPlayer", paramSummoner.getName());
        assertEquals(30, paramSummoner.getLevel());
        assertNotNull(paramSummoner.getMatches());
        assertNotNull(paramSummoner.getChampionStats());
    }

    @Test
    void testSettersAndGetters() {
        summoner.setId(1L);
        summoner.setRiotId("RIOT456");
        summoner.setName("PlayerOne");
        summoner.setLevel(25);
        summoner.setProfileIconId(1234);
        summoner.setTier("GOLD");
        summoner.setRank("III");
        summoner.setLp(1500);

        assertEquals(1L, summoner.getId());
        assertEquals("RIOT456", summoner.getRiotId());
        assertEquals("PlayerOne", summoner.getName());
        assertEquals(25, summoner.getLevel());
        assertEquals(1234, summoner.getProfileIconId());
        assertEquals("GOLD", summoner.getTier());
        assertEquals("III", summoner.getRank());
        assertEquals(1500, summoner.getLp());
    }

    @Test
    void testMatchesManagement() {
        assertTrue(summoner.getMatches().isEmpty());
        
        List<MatchEntity> matches = new ArrayList<>();
        MatchEntity match1 = new MatchEntity();
        MatchEntity match2 = new MatchEntity();
        matches.add(match1);
        matches.add(match2);
        
        summoner.setMatches(matches);
        assertEquals(2, summoner.getMatches().size());
        assertTrue(summoner.getMatches().contains(match1));
        assertTrue(summoner.getMatches().contains(match2));
    }

    @Test
    void testChampionStatsManagement() {
        assertTrue(summoner.getChampionStats().isEmpty());
        
        List<ChampionStat> stats = new ArrayList<>();
        ChampionStat stat1 = new ChampionStat();
        ChampionStat stat2 = new ChampionStat();
        stats.add(stat1);
        stats.add(stat2);
        
        summoner.setChampionStats(stats);
        assertEquals(2, summoner.getChampionStats().size());
        assertTrue(summoner.getChampionStats().contains(stat1));
        assertTrue(summoner.getChampionStats().contains(stat2));
    }

    @Test
    void testRankTierCombinations() {
        // Test unranked player
        summoner.setTier(null);
        summoner.setRank(null);
        assertNull(summoner.getTier());
        assertNull(summoner.getRank());
        
        // Test ranked player
        summoner.setTier("SILVER");
        summoner.setRank("II");
        assertEquals("SILVER", summoner.getTier());
        assertEquals("II", summoner.getRank());
        
        // Test high tier without rank (Master, Grandmaster, Challenger)
        summoner.setTier("MASTER");
        summoner.setRank(null);
        assertEquals("MASTER", summoner.getTier());
        assertNull(summoner.getRank());
    }

    @Test
    void testLevelProgression() {
        summoner.setLevel(1);
        assertEquals(1, summoner.getLevel());
        
        summoner.setLevel(30);
        assertEquals(30, summoner.getLevel());
        
        summoner.setLevel(500);
        assertEquals(500, summoner.getLevel());
    }

    @Test
    void testProfileIconHandling() {
        assertNull(summoner.getProfileIconId());
        
        summoner.setProfileIconId(1);
        assertEquals(1, summoner.getProfileIconId());
        
        summoner.setProfileIconId(9999);
        assertEquals(9999, summoner.getProfileIconId());
        
        summoner.setProfileIconId(null);
        assertNull(summoner.getProfileIconId());
    }

    @Test
    void testLpHandling() {
        assertNull(summoner.getLp());
        
        summoner.setLp(0);
        assertEquals(0, summoner.getLp());
        
        summoner.setLp(100);
        assertEquals(100, summoner.getLp());
        
        summoner.setLp(-1); // Edge case
        assertEquals(-1, summoner.getLp());
        
        summoner.setLp(null);
        assertNull(summoner.getLp());
    }

    @Test
    void testCompleteSummonerSetup() {
        summoner.setId(42L);
        summoner.setRiotId("RIOT999");
        summoner.setName("CompletePlayer");
        summoner.setLevel(100);
        summoner.setProfileIconId(5555);
        summoner.setTier("DIAMOND");
        summoner.setRank("I");
        summoner.setLp(2000);
        
        List<MatchEntity> matches = List.of(new MatchEntity(), new MatchEntity());
        List<ChampionStat> stats = List.of(new ChampionStat(), new ChampionStat());
        summoner.setMatches(matches);
        summoner.setChampionStats(stats);

        assertEquals(42L, summoner.getId());
        assertEquals("RIOT999", summoner.getRiotId());
        assertEquals("CompletePlayer", summoner.getName());
        assertEquals(100, summoner.getLevel());
        assertEquals(5555, summoner.getProfileIconId());
        assertEquals("DIAMOND", summoner.getTier());
        assertEquals("I", summoner.getRank());
        assertEquals(2000, summoner.getLp());
        assertEquals(2, summoner.getMatches().size());
        assertEquals(2, summoner.getChampionStats().size());
    }
}