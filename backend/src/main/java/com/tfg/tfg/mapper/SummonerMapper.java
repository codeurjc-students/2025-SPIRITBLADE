package com.tfg.tfg.mapper;

import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.service.DataDragonService;

/**
 * Mapper helper to convert between Summoner entity and SummonerDTO
 * Centralizes mapping logic to keep services/controllers thin.
 */
public final class SummonerMapper {

    private SummonerMapper() {
        // static helper
    }

    public static SummonerDTO toDTO(Summoner summoner, DataDragonService dataDragonService) {
        if (summoner == null) return null;
        SummonerDTO dto = new SummonerDTO();
        dto.setId(summoner.getId());
        dto.setRiotId(summoner.getRiotId());
        dto.setPuuid(summoner.getPuuid());
        dto.setName(summoner.getName());
        dto.setLevel(summoner.getLevel());
        dto.setProfileIconId(summoner.getProfileIconId());
        dto.setProfileIconUrl(dataDragonService != null ? dataDragonService.getProfileIconUrl(summoner.getProfileIconId()) : null);
        dto.setTier(summoner.getTier());
        dto.setRank(summoner.getRank());
        dto.setLp(summoner.getLp());
        dto.setWins(summoner.getWins());
        dto.setLosses(summoner.getLosses());
        dto.setLastSearchedAt(summoner.getLastSearchedAt());
        return dto;
    }

    public static Summoner toEntity(SummonerDTO dto) {
        if (dto == null) return null;
        Summoner summoner = new Summoner();
        summoner.setRiotId(dto.getRiotId());
        summoner.setPuuid(dto.getPuuid());
        summoner.setName(dto.getName());
        summoner.setLevel(dto.getLevel());
        summoner.setProfileIconId(dto.getProfileIconId());
        summoner.setTier(dto.getTier());
        summoner.setRank(dto.getRank());
        summoner.setLp(dto.getLp());
        summoner.setWins(dto.getWins());
        summoner.setLosses(dto.getLosses());
        // lastSearchedAt intentionally not set here (managed by service)
        return summoner;
    }
}
