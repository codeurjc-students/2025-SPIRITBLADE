package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.riot.RiotMatchDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RiotMatchDTONestedClassesUnitTest {

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
