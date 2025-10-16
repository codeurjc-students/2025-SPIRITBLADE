package com.tfg.tfg.repository;

import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {
    
    /**
     * Find all matches for a summoner ordered by timestamp ascending (oldest first)
     */
    List<MatchEntity> findBySummonerOrderByTimestampAsc(Summoner summoner);
    
    /**
     * Find ranked matches for a summoner ordered by timestamp ascending
     * Used for LP progression tracking
     */
    @Query("SELECT m FROM MatchEntity m WHERE m.summoner = :summoner " +
        "AND m.gameMode LIKE %:mode% " +
        "AND m.tierAtMatch IS NOT NULL " +
        "ORDER BY m.timestamp ASC")
    List<MatchEntity> findRankedMatchesBySummoner(@Param("summoner") Summoner summoner, @Param("mode") String mode);
    
    /**
     * Find recent matches for a summoner with pagination support
     */
    List<MatchEntity> findBySummonerOrderByTimestampDesc(Summoner summoner);
}
