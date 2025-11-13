package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.riot.RiotMatchDTO;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional unit tests for RiotMatchDTO.
 * Tests remaining getters/setters in ParticipantDTO, InfoDTO, MetadataDTO, and TeamDTO.
 */
class RiotMatchDTOTest {

    @Test
    void testParticipantDTORemainingFields() {
        RiotMatchDTO.ParticipantDTO participant = new RiotMatchDTO.ParticipantDTO();
        
        // Test fields not covered in previous tests
        participant.setRiotIdGameName("TestPlayer");
        participant.setRiotIdTagline("EUW");
        participant.setChampLevel(18);
        participant.setItem0(3152);
        participant.setItem1(3006);
        participant.setItem2(3031);
        participant.setItem3(3087);
        participant.setItem4(3036);
        participant.setItem5(3046);
        participant.setItem6(3363);
        
        // Assert
        assertEquals("TestPlayer", participant.getRiotIdGameName());
        assertEquals("EUW", participant.getRiotIdTagline());
        assertEquals(18, participant.getChampLevel());
        assertEquals(3152, participant.getItem0());
        assertEquals(3006, participant.getItem1());
        assertEquals(3031, participant.getItem2());
        assertEquals(3087, participant.getItem3());
        assertEquals(3036, participant.getItem4());
        assertEquals(3046, participant.getItem5());
        assertEquals(3363, participant.getItem6());
    }

    @Test
    void testParticipantDTOWinFieldBoolean() {
        RiotMatchDTO.ParticipantDTO participant = new RiotMatchDTO.ParticipantDTO();
        
        participant.setWin(true);
        assertEquals(Boolean.TRUE, participant.getWin());
        
        participant.setWin(false);
        assertEquals(Boolean.FALSE, participant.getWin());
        
        participant.setWin(null);
        assertNull(participant.getWin());
    }

    @Test
    void testInfoDTOGameModeAndQueue() {
        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        
        info.setGameMode("ARAM");
        info.setQueueId(450);
        
        assertEquals("ARAM", info.getGameMode());
        assertEquals(450, info.getQueueId());
    }

    @Test
    void testMetadataDTODataVersion() {
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        
        metadata.setDataVersion("3");
        assertEquals("3", metadata.getDataVersion());
    }

    @Test
    void testTeamDTOWinBoolean() {
        RiotMatchDTO.TeamDTO team = new RiotMatchDTO.TeamDTO();
        
        team.setWin(true);
        assertEquals(Boolean.TRUE, team.getWin());
        
        team.setWin(false);
        assertEquals(Boolean.FALSE, team.getWin());
    }

    @Test
    void testTeamDTOBans() {
        RiotMatchDTO.TeamDTO team = new RiotMatchDTO.TeamDTO();
        
        RiotMatchDTO.BanDTO ban1 = new RiotMatchDTO.BanDTO();
        ban1.setChampionId(157);
        RiotMatchDTO.BanDTO ban2 = new RiotMatchDTO.BanDTO();
        ban2.setChampionId(238);
        
        team.setBans(Arrays.asList(ban1, ban2));
        
        assertNotNull(team.getBans());
        assertEquals(2, team.getBans().size());
        assertEquals(157, team.getBans().get(0).getChampionId());
        assertEquals(238, team.getBans().get(1).getChampionId());
    }

    
    @Test
    void testObjectiveDTO() {
        RiotMatchDTO.ObjectiveDTO objective = new RiotMatchDTO.ObjectiveDTO();
        
        objective.setFirst(true);
        objective.setKills(2);
        
        assertTrue(objective.getFirst());
        assertEquals(2, objective.getKills());
    }

    @Test
    void testObjectivesDTO() {
        RiotMatchDTO.ObjectivesDTO objectives = new RiotMatchDTO.ObjectivesDTO();
        RiotMatchDTO.ObjectiveDTO baron = new RiotMatchDTO.ObjectiveDTO();
        RiotMatchDTO.ObjectiveDTO dragon = new RiotMatchDTO.ObjectiveDTO();
        RiotMatchDTO.ObjectiveDTO tower = new RiotMatchDTO.ObjectiveDTO();
        RiotMatchDTO.ObjectiveDTO inhibitor = new RiotMatchDTO.ObjectiveDTO();
        RiotMatchDTO.ObjectiveDTO riftHerald = new RiotMatchDTO.ObjectiveDTO();
        
        baron.setKills(2);
        dragon.setKills(3);
        tower.setKills(7);
        inhibitor.setKills(1);
        riftHerald.setKills(1);
        
        objectives.setBaron(baron);
        objectives.setDragon(dragon);
        objectives.setTower(tower);
        objectives.setInhibitor(inhibitor);
        objectives.setRiftHerald(riftHerald);
        
        assertEquals(2, objectives.getBaron().getKills());
        assertEquals(3, objectives.getDragon().getKills());
        assertEquals(7, objectives.getTower().getKills());
        assertEquals(1, objectives.getInhibitor().getKills());
        assertEquals(1, objectives.getRiftHerald().getKills());
    }

    @Test
    void testBanDTO() {
        RiotMatchDTO.BanDTO ban = new RiotMatchDTO.BanDTO();
        
        ban.setChampionId(157);
        ban.setPickTurn(1);
        
        assertEquals(157, ban.getChampionId());
        assertEquals(1, ban.getPickTurn());
    }
}
