package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.MatchDetailDTO;
import com.tfg.tfg.model.dto.TeamDTO;
import com.tfg.tfg.model.dto.ParticipantDTO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchDetailDTOSimpleUnitTest {

    @Test
    void testDefaultConstructor() {
        MatchDetailDTO dto = new MatchDetailDTO();
        assertNotNull(dto);
        assertNull(dto.getMatchId());
    }

    @Test
    void testConstructorWithMatchId() {
        MatchDetailDTO dto = new MatchDetailDTO("MATCH123");
        assertEquals("MATCH123", dto.getMatchId());
    }

    @Test
    void testSettersAndGetters() {
        MatchDetailDTO dto = new MatchDetailDTO();
        
        dto.setMatchId("MATCH456");
        dto.setGameCreation(1699999999L);
        dto.setGameDuration(1800L);
        dto.setGameMode("CLASSIC");
        dto.setGameType("MATCHED_GAME");
        dto.setGameVersion("13.21");
        dto.setQueueId(420);
        
        assertEquals("MATCH456", dto.getMatchId());
        assertEquals(1699999999L, dto.getGameCreation());
        assertEquals(1800L, dto.getGameDuration());
        assertEquals("CLASSIC", dto.getGameMode());
        assertEquals("MATCHED_GAME", dto.getGameType());
        assertEquals("13.21", dto.getGameVersion());
        assertEquals(420, dto.getQueueId());
    }

    @Test
    void testTeamsAndParticipants() {
        MatchDetailDTO dto = new MatchDetailDTO();
        
        List<TeamDTO> teams = Arrays.asList(new TeamDTO(), new TeamDTO());
        List<ParticipantDTO> participants = Arrays.asList(
            new ParticipantDTO(),
            new ParticipantDTO()
        );
        
        dto.setTeams(teams);
        dto.setParticipants(participants);
        
        assertEquals(2, dto.getTeams().size());
        assertEquals(2, dto.getParticipants().size());
    }

    @Test
    @SuppressWarnings("java:S5976") // Tests intentionally kept simple and readable
    void testGetFormattedDuration_WithValidDuration() {
        MatchDetailDTO dto = new MatchDetailDTO();
        dto.setGameDuration(1865L); // 31 minutes and 5 seconds
        
        String formatted = dto.getFormattedDuration();
        assertEquals("31m 5s", formatted);
    }

    @Test
    void testGetFormattedDuration_WithNullDuration() {
        MatchDetailDTO dto = new MatchDetailDTO();
        dto.setGameDuration(null);
        
        String formatted = dto.getFormattedDuration();
        assertEquals("0m 0s", formatted);
    }

    @Test
    void testGetFormattedDuration_WithZeroDuration() {
        MatchDetailDTO dto = new MatchDetailDTO();
        dto.setGameDuration(0L);
        
        String formatted = dto.getFormattedDuration();
        assertEquals("0m 0s", formatted);
    }

    @Test
    void testGetFormattedDuration_ExactMinutes() {
        MatchDetailDTO dto = new MatchDetailDTO();
        dto.setGameDuration(1800L); // Exactly 30 minutes
        
        String formatted = dto.getFormattedDuration();
        assertEquals("30m 0s", formatted);
    }

    @Test
    void testAllFieldsSet() {
        MatchDetailDTO dto = new MatchDetailDTO("MATCH789");
        dto.setGameCreation(1700000000L);
        dto.setGameDuration(2400L);
        dto.setGameMode("ARAM");
        dto.setGameType("CUSTOM_GAME");
        dto.setGameVersion("14.1");
        dto.setQueueId(450);
        dto.setTeams(Arrays.asList(new TeamDTO()));
        dto.setParticipants(Arrays.asList(new ParticipantDTO()));
        
        assertNotNull(dto.getMatchId());
        assertNotNull(dto.getGameCreation());
        assertNotNull(dto.getGameDuration());
        assertNotNull(dto.getGameMode());
        assertNotNull(dto.getGameType());
        assertNotNull(dto.getGameVersion());
        assertNotNull(dto.getQueueId());
        assertNotNull(dto.getTeams());
        assertNotNull(dto.getParticipants());
    }
}
