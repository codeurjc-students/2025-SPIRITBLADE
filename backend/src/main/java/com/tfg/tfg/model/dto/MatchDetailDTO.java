package com.tfg.tfg.model.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatchDetailDTO {
    private String matchId;
    private Long gameCreation;
    private Long gameDuration;
    private String gameMode;
    private String gameType;
    private String gameVersion;
    private Integer queueId;
    
    private List<TeamDTO> teams;
    private List<ParticipantDTO> participants;

    public String getFormattedDuration() {
        if (gameDuration == null) return "0m 0s";
        long minutes = gameDuration / 60;
        long seconds = gameDuration % 60;
        return minutes + "m " + seconds + "s";
    }
}
