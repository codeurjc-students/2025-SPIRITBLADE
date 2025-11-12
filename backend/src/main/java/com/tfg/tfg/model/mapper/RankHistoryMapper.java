package com.tfg.tfg.model.mapper;

import com.tfg.tfg.model.dto.RankHistoryDTO;
import com.tfg.tfg.model.entity.RankHistory;

/**
 * Mapper for converting between RankHistory entity and RankHistoryDTO.
 */
public class RankHistoryMapper {

    private RankHistoryMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Converts a RankHistory entity to a RankHistoryDTO.
     * 
     * @param entity The RankHistory entity
     * @return The corresponding RankHistoryDTO
     */
    public static RankHistoryDTO toDTO(RankHistory entity) {
        if (entity == null) {
            return null;
        }

        return new RankHistoryDTO(
            entity.getId(),
            entity.getSummoner() != null ? entity.getSummoner().getName() : null,
            entity.getTimestamp(),
            entity.getTier(),
            entity.getRank(),
            entity.getLeaguePoints(),
            entity.getWins(),
            entity.getLosses(),
            entity.getQueueType(),
            entity.getLpChange()
        );
    }

    /**
     * Converts a RankHistory entity to a simple RankHistoryDTO (backward compatible).
     * 
     * @param entity The RankHistory entity
     * @return The corresponding RankHistoryDTO with basic fields
     */
    public static RankHistoryDTO toSimpleDTO(RankHistory entity) {
        if (entity == null) {
            return null;
        }

        return new RankHistoryDTO(
            entity.getTimestamp() != null ? entity.getTimestamp().toString() : null,
            entity.getTier(),
            entity.getRank(),
            entity.getLeaguePoints(),
            entity.getWins(),
            entity.getLosses()
        );
    }
}
