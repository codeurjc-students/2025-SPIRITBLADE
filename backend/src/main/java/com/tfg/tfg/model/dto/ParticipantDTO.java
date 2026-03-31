package com.tfg.tfg.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ParticipantDTO {
    private String summonerName;
    private String riotIdGameName;
    private String riotIdTagline;
    private String championName;
    private String championIconUrl;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Integer level;
    private Integer totalMinionsKilled;
    private Integer goldEarned;
    private Integer totalDamageDealtToChampions;
    private Integer visionScore;
    private Boolean win;
    private Integer teamId;
    private String teamPosition;
    
    private Integer item0;
    private Integer item1;
    private Integer item2;
    private Integer item3;
    private Integer item4;
    private Integer item5;
    private Integer item6;

    public String getKda() {
        return kills + "/" + deaths + "/" + assists;
    }

    public Double getKdaRatio() {
        if (deaths == null || deaths == 0) {
            return (kills != null && assists != null) ? (double)(kills + assists) : 0.0;
        }
        return (kills != null && assists != null) ? (double)(kills + assists) / deaths : 0.0;
    }
}
