package com.tfg.tfg.model.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamDTO {
    private Integer teamId;
    private Boolean win;
    private List<ParticipantDTO> participants;
    
    private Integer baronKills;
    private Integer dragonKills;
    private Integer towerKills;
    private Integer inhibitorKills;
    private Integer riftHeraldKills;
    
    private List<String> bans;

}
