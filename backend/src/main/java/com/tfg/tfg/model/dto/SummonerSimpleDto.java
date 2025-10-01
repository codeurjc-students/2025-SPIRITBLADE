package com.tfg.tfg.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummonerSimpleDto {
    private String id;
    private String name;
    private Integer level;
}
