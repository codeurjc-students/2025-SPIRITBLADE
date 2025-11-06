package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.riot.RiotSummonerDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiotSummonerDTOUnitTest {

    @Test
    void testGettersAndSetters() {
        RiotSummonerDTO dto = new RiotSummonerDTO();
        
        dto.setId("summoner-id");
        dto.setPuuid("test-puuid");
        dto.setName("PlayerName");
        dto.setProfileIconId(1234);
        dto.setSummonerLevel(150);
        
        assertEquals("summoner-id", dto.getId());
        assertEquals("test-puuid", dto.getPuuid());
        assertEquals("PlayerName", dto.getName());
        assertEquals(1234, dto.getProfileIconId());
        assertEquals(150, dto.getSummonerLevel());
    }
}
