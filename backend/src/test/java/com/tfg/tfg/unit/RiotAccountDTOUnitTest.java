package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.riot.RiotAccountDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiotAccountDTOUnitTest {

    @Test
    void testGettersAndSetters() {
        RiotAccountDTO dto = new RiotAccountDTO();
        
        dto.setPuuid("test-puuid");
        dto.setGameName("Player");
        dto.setTagLine("EUW");
        
        assertEquals("test-puuid", dto.getPuuid());
        assertEquals("Player", dto.getGameName());
        assertEquals("EUW", dto.getTagLine());
    }
}
