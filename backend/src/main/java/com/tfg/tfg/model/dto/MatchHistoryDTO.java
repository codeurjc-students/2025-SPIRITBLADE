package com.tfg.tfg.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatchHistoryDTO {
    private String matchId;
    private String championName;
    private String championIconUrl;
    private Boolean win;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Long gameDuration;
    private Long gameTimestamp;
    private Integer lpAtMatch;
    private Integer queueId;
    private Integer visionScore;

    public String getKda() {
        return kills + "/" + deaths + "/" + assists;
    }

    public String getFormattedDuration() {
        if (gameDuration == null)
            return "0m";
        long minutes = gameDuration / 60;
        return minutes + "m";
    }
}
