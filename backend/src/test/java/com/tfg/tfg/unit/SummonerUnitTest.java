package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    }

    @Test
    void testParameterizedConstructor() {
        Summoner paramSummoner = new Summoner("RIOT123", "TestPlayer#EUW", 30);
        
        assertEquals("RIOT123", paramSummoner.getRiotId());
        assertEquals("TestPlayer#EUW", paramSummoner.getName());
        assertEquals(30, paramSummoner.getLevel());
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

    // Tests for matches and championStats removed - data now fetched from Riot API in real-time

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

        assertEquals(42L, summoner.getId());
        assertEquals("RIOT999", summoner.getRiotId());
        assertEquals("CompletePlayer", summoner.getName());
        assertEquals(100, summoner.getLevel());
        assertEquals(5555, summoner.getProfileIconId());
        assertEquals("DIAMOND", summoner.getTier());
        assertEquals("I", summoner.getRank());
        assertEquals(2000, summoner.getLp());
    }
}