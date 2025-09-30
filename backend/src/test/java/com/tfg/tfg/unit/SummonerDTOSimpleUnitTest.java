package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.dto.SummonerDTO;

class SummonerDTOSimpleUnitTest {

    @Test
    void testSummonerDTOCreation() {
        SummonerDTO dto = new SummonerDTO();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getRiotId());
        assertNull(dto.getName());
        assertNull(dto.getLevel());
        assertNull(dto.getProfileIconId());
        assertNull(dto.getTier());
        assertNull(dto.getRank());
        assertNull(dto.getLp());
    }

    @Test
    void testSummonerDTOSettersAndGetters() {
        SummonerDTO dto = new SummonerDTO();
        dto.setId(1L);
        dto.setRiotId("riot#123");
        dto.setName("TestSummoner");
        dto.setLevel(30);
        dto.setProfileIconId(123);
        dto.setTier("GOLD");
        dto.setRank("II");
        dto.setLp(75);
        
        assertEquals(1L, dto.getId());
        assertEquals("riot#123", dto.getRiotId());
        assertEquals("TestSummoner", dto.getName());
        assertEquals(30, dto.getLevel());
        assertEquals(123, dto.getProfileIconId());
        assertEquals("GOLD", dto.getTier());
        assertEquals("II", dto.getRank());
        assertEquals(75, dto.getLp());
    }

    @Test
    void testSummonerDTOWithNullValues() {
        SummonerDTO dto = new SummonerDTO();
        dto.setRiotId(null);
        dto.setName(null);
        dto.setLevel(null);
        dto.setTier(null);
        dto.setRank(null);
        dto.setLp(null);
        
        assertNull(dto.getRiotId());
        assertNull(dto.getName());
        assertNull(dto.getLevel());
        assertNull(dto.getTier());
        assertNull(dto.getRank());
        assertNull(dto.getLp());
    }

    @Test
    void testSummonerDTOHighLevel() {
        SummonerDTO dto = new SummonerDTO();
        dto.setLevel(500);
        
        assertEquals(500, dto.getLevel());
        assertTrue(dto.getLevel() > 400);
    }

    @Test
    void testSummonerDTOLowLevel() {
        SummonerDTO dto = new SummonerDTO();
        dto.setLevel(1);
        
        assertEquals(1, dto.getLevel());
        assertTrue(dto.getLevel() < 30);
    }

    @Test
    void testSummonerDTOUnranked() {
        SummonerDTO dto = new SummonerDTO();
        dto.setTier("UNRANKED");
        dto.setRank(null);
        dto.setLp(0);
        
        assertEquals("UNRANKED", dto.getTier());
        assertNull(dto.getRank());
        assertEquals(0, dto.getLp());
    }

    @Test
    void testSummonerDTOHighTier() {
        SummonerDTO dto = new SummonerDTO();
        dto.setTier("CHALLENGER");
        dto.setRank("I");
        dto.setLp(1200);
        
        assertEquals("CHALLENGER", dto.getTier());
        assertEquals("I", dto.getRank());
        assertEquals(1200, dto.getLp());
    }

    @Test
    void testSummonerDTONegativeLP() {
        SummonerDTO dto = new SummonerDTO();
        dto.setLp(-10);
        
        assertEquals(-10, dto.getLp());
        assertTrue(dto.getLp() < 0);
    }

    @Test
    void testSummonerDTOProfileIcon() {
        SummonerDTO dto = new SummonerDTO();
        dto.setProfileIconId(4001);
        
        assertEquals(4001, dto.getProfileIconId());
        assertTrue(dto.getProfileIconId() > 4000);
    }

    @Test
    void testSummonerDTOCompleteProfile() {
        SummonerDTO dto = new SummonerDTO();
        dto.setId(999L);
        dto.setRiotId("player#999");
        dto.setName("ProPlayer");
        dto.setLevel(250);
        dto.setProfileIconId(29);
        dto.setTier("DIAMOND");
        dto.setRank("I");
        dto.setLp(95);
        
        assertEquals(999L, dto.getId());
        assertEquals("player#999", dto.getRiotId());
        assertEquals("ProPlayer", dto.getName());
        assertEquals(250, dto.getLevel());
        assertEquals(29, dto.getProfileIconId());
        assertEquals("DIAMOND", dto.getTier());
        assertEquals("I", dto.getRank());
        assertEquals(95, dto.getLp());
    }

    @Test
    void testSummonerDTOEmptyStrings() {
        SummonerDTO dto = new SummonerDTO();
        dto.setRiotId("");
        dto.setName("");
        dto.setTier("");
        dto.setRank("");
        
        assertEquals("", dto.getRiotId());
        assertEquals("", dto.getName());
        assertEquals("", dto.getTier());
        assertEquals("", dto.getRank());
    }
}