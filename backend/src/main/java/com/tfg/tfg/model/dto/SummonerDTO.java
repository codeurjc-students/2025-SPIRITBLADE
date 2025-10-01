package com.tfg.tfg.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummonerDTO {
    private Long id;
    private String riotId;
    private String name;
    private Integer level;
    private Integer profileIconId;
    private String tier;
    private String rank;
    private Integer lp;
}
