package com.tfg.tfg.model.mapper;

import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.service.DataDragonService;
import com.tfg.tfg.service.RankHistoryService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Mapper helper to convert between MatchEntity and MatchHistoryDTO
 * Centralizes mapping logic to keep services/controllers thin.
 */
public final class MatchMapper {

    private MatchMapper() {
        // static helper
    }

    /**
     * Convert MatchHistoryDTO to MatchEntity
     * @param dto The DTO to convert
     * @param summoner The summoner entity
     * @return New MatchEntity
     */
    public static MatchEntity toEntity(MatchHistoryDTO dto, Summoner summoner) {
        return toEntity(null, dto, summoner);
    }

    /**
     * Convert MatchHistoryDTO to MatchEntity, updating existing entity if provided
     * @param existing Existing entity to update, or null to create new
     * @param dto The DTO to convert
     * @param summoner The summoner entity
     * @return MatchEntity (updated or new)
     */
    public static MatchEntity toEntity(MatchEntity existing, MatchHistoryDTO dto, Summoner summoner) {
        if (dto == null) return null;

        MatchEntity entity = existing != null ? existing : new MatchEntity();

        entity.setMatchId(dto.getMatchId());
        entity.setSummoner(summoner);
        entity.setChampionName(dto.getChampionName());
        entity.setWin(dto.getWin() != null && dto.getWin());
        entity.setKills(dto.getKills() != null ? dto.getKills() : 0);
        entity.setDeaths(dto.getDeaths() != null ? dto.getDeaths() : 0);
        entity.setAssists(dto.getAssists() != null ? dto.getAssists() : 0);
        entity.setGameDuration(dto.getGameDuration());
        entity.setQueueId(dto.getQueueId());

        if (dto.getGameTimestamp() != null) {
            entity.setTimestamp(LocalDateTime.ofEpochSecond(
                dto.getGameTimestamp(), 0, java.time.ZoneOffset.UTC));
        }

        return entity;
    }

    /**
     * Convert MatchEntity to MatchHistoryDTO
     * @param entity The entity to convert
     * @param dataDragonService Service for champion icon URLs
     * @param rankHistoryService Service for LP data
     * @return MatchHistoryDTO
     */
    public static MatchHistoryDTO toDTO(MatchEntity entity, DataDragonService dataDragonService, 
                                       RankHistoryService rankHistoryService) {
        if (entity == null) return null;

        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setMatchId(entity.getMatchId());
        dto.setChampionName(entity.getChampionName());
        dto.setWin(entity.isWin());
        dto.setKills(entity.getKills());
        dto.setDeaths(entity.getDeaths());
        dto.setAssists(entity.getAssists());
        dto.setGameDuration(entity.getGameDuration());
        dto.setGameTimestamp(entity.getTimestamp() != null ? 
            entity.getTimestamp().toEpochSecond(ZoneOffset.UTC) : null);
        dto.setQueueId(entity.getQueueId());

        // Load LP from RankHistory if available
        if (rankHistoryService != null) {
            rankHistoryService.getLpForMatch(entity.getId()).ifPresent(dto::setLpAtMatch);
        }

        // Enrich with champion icon from DataDragon
        if (dataDragonService != null && entity.getChampionId() != null) {
            try {
                dto.setChampionIconUrl(dataDragonService.getChampionIconUrl(entity.getChampionId().longValue()));
            } catch (Exception e) {
                // Icon fetch failure is non-critical
            }
        }

        return dto;
    }
}