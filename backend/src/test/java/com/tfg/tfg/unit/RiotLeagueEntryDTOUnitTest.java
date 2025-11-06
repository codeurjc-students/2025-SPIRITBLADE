package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.riot.RiotLeagueEntryDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiotLeagueEntryDTOUnitTest {

    @Test
    void testGettersAndSetters() {
        RiotLeagueEntryDTO dto = new RiotLeagueEntryDTO();
        
        dto.setQueueType("RANKED_SOLO_5x5");
        dto.setTier("GOLD");
        dto.setRank("II");
        dto.setLeaguePoints(75);
        dto.setWins(100);
        dto.setLosses(95);
        
        assertEquals("RANKED_SOLO_5x5", dto.getQueueType());
        assertEquals("GOLD", dto.getTier());
        assertEquals("II", dto.getRank());
        assertEquals(75, dto.getLeaguePoints());
        assertEquals(100, dto.getWins());
        assertEquals(95, dto.getLosses());
    }
}
