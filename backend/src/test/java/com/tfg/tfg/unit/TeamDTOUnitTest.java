package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.TeamDTO;
import com.tfg.tfg.model.dto.ParticipantDTO;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class TeamDTOUnitTest {

    @Test
    void testDefaultConstructor() {
        TeamDTO dto = new TeamDTO();
        assertNotNull(dto);
    }

    @Test
    void testParameterizedConstructor() {
        TeamDTO dto = new TeamDTO(100, true);
        assertEquals(100, dto.getTeamId());
        assertTrue(dto.getWin());
    }

    @Test
    void testGettersAndSetters() {
        TeamDTO dto = new TeamDTO();
        ParticipantDTO p1 = new ParticipantDTO();
        ParticipantDTO p2 = new ParticipantDTO();
        
        dto.setTeamId(200);
        dto.setWin(false);
        dto.setParticipants(Arrays.asList(p1, p2));
        dto.setBaronKills(2);
        dto.setDragonKills(3);
        dto.setTowerKills(7);
        dto.setInhibitorKills(1);
        dto.setRiftHeraldKills(1);
        dto.setBans(Arrays.asList("Yasuo", "Zed", "Thresh", "Blitzcrank", "Yuumi"));
        
        assertEquals(200, dto.getTeamId());
        assertFalse(dto.getWin());
        assertEquals(2, dto.getParticipants().size());
        assertEquals(2, dto.getBaronKills());
        assertEquals(3, dto.getDragonKills());
        assertEquals(7, dto.getTowerKills());
        assertEquals(1, dto.getInhibitorKills());
        assertEquals(1, dto.getRiftHeraldKills());
        assertEquals(5, dto.getBans().size());
        assertTrue(dto.getBans().contains("Yasuo"));
    }
}
