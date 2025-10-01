package com.tfg.tfg.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionStatDTO {
    private Long id;
    private Integer championId;
    private int gamesPlayed;
    private int wins;
    private int kills;
    private int deaths;
    private int assists;
}
