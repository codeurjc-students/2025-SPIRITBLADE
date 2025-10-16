package com.tfg.tfg.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.service.DataDragonService;
import com.tfg.tfg.mapper.SummonerMapper;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummonerDTO {
    private Long id;
    private String riotId;
    private String puuid;
    private String name;
    private Integer level;
    private Integer profileIconId;
    private String profileIconUrl; // URL to profile icon image from Data Dragon
    private String tier;
    private String rank;
    private Integer lp;
    private Integer wins;
    private Integer losses;
    private LocalDateTime lastSearchedAt;
    
    /**
     * Creates a SummonerDTO from a Summoner entity
     * @param summoner The summoner entity
     * @param dataDragonService Service to generate profile icon URLs
     * @return SummonerDTO with all fields populated
     */
    public static SummonerDTO fromEntity(Summoner summoner, DataDragonService dataDragonService) {
        return SummonerMapper.toDTO(summoner, dataDragonService);
    }
}
