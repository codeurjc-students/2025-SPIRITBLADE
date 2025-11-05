package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.TeamDTO;
import com.tfg.tfg.model.dto.ParticipantDTO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamDTOSimpleUnitTest {

    @Test
    void testDefaultConstructor() {
        TeamDTO dto = new TeamDTO();
        assertNotNull(dto);
        assertNull(dto.getTeamId());
        assertNull(dto.getWin());
    }

    @Test
    void testParameterizedConstructor() {
        TeamDTO dto = new TeamDTO(100, true);
        
        assertEquals(100, dto.getTeamId());
        assertTrue(dto.getWin());
    }

    @Test
    void testAllSettersAndGetters() {
        TeamDTO dto = new TeamDTO();
        
        dto.setTeamId(200);
        dto.setWin(false);
        dto.setBaronKills(2);
        dto.setDragonKills(3);
        dto.setTowerKills(8);
        dto.setInhibitorKills(2);
        dto.setRiftHeraldKills(1);
        
        List<String> bans = Arrays.asList("Yasuo", "Zed", "Vayne", "Master Yi", "Teemo");
        dto.setBans(bans);
        
        List<ParticipantDTO> participants = Arrays.asList(
            new ParticipantDTO(),
            new ParticipantDTO(),
            new ParticipantDTO(),
            new ParticipantDTO(),
            new ParticipantDTO()
        );
        dto.setParticipants(participants);
        
        assertEquals(200, dto.getTeamId());
        assertFalse(dto.getWin());
        assertEquals(2, dto.getBaronKills());
        assertEquals(3, dto.getDragonKills());
        assertEquals(8, dto.getTowerKills());
        assertEquals(2, dto.getInhibitorKills());
        assertEquals(1, dto.getRiftHeraldKills());
        assertEquals(5, dto.getBans().size());
        assertEquals(5, dto.getParticipants().size());
    }

    @Test
    void testWinningTeam() {
        TeamDTO dto = new TeamDTO(100, true);
        dto.setBaronKills(3);
        dto.setDragonKills(4);
        dto.setTowerKills(11);
        dto.setInhibitorKills(3);
        
        assertTrue(dto.getWin());
        assertEquals(3, dto.getBaronKills());
        assertEquals(4, dto.getDragonKills());
    }

    @Test
    void testLosingTeam() {
        TeamDTO dto = new TeamDTO(200, false);
        dto.setBaronKills(0);
        dto.setDragonKills(1);
        dto.setTowerKills(3);
        
        assertFalse(dto.getWin());
        assertEquals(0, dto.getBaronKills());
        assertEquals(1, dto.getDragonKills());
    }

    @Test
    void testObjectivesZeroValues() {
        TeamDTO dto = new TeamDTO();
        dto.setBaronKills(0);
        dto.setDragonKills(0);
        dto.setTowerKills(0);
        dto.setInhibitorKills(0);
        dto.setRiftHeraldKills(0);
        
        assertEquals(0, dto.getBaronKills());
        assertEquals(0, dto.getDragonKills());
        assertEquals(0, dto.getTowerKills());
        assertEquals(0, dto.getInhibitorKills());
        assertEquals(0, dto.getRiftHeraldKills());
    }

    @Test
    void testBansManagement() {
        TeamDTO dto = new TeamDTO();
        
        List<String> bans = Arrays.asList("Ahri", "Lux", "Jinx", "Thresh", "Lee Sin");
        dto.setBans(bans);
        
        assertNotNull(dto.getBans());
        assertEquals(5, dto.getBans().size());
        assertTrue(dto.getBans().contains("Ahri"));
        assertTrue(dto.getBans().contains("Lee Sin"));
    }

    @Test
    void testParticipantsManagement() {
        TeamDTO dto = new TeamDTO(100, true);
        
        ParticipantDTO p1 = new ParticipantDTO();
        p1.setChampionName("Ahri");
        
        ParticipantDTO p2 = new ParticipantDTO();
        p2.setChampionName("Lee Sin");
        
        dto.setParticipants(Arrays.asList(p1, p2));
        
        assertEquals(2, dto.getParticipants().size());
        assertEquals("Ahri", dto.getParticipants().get(0).getChampionName());
        assertEquals("Lee Sin", dto.getParticipants().get(1).getChampionName());
    }
}
