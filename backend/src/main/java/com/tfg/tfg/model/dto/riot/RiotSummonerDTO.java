package com.tfg.tfg.model.dto.riot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiotSummonerDTO {
    private String id;
    private String accountId;
    private String puuid;
    private String name;
    private Integer profileIconId;
    private Long revisionDate;
    private Integer summonerLevel;
}