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
    
    /**
     * Find the most recent match for a summoner
     */
    MatchEntity findFirstBySummonerOrderByTimestampDesc(Summoner summoner);
    
    /**
     * Find a specific match by matchId
     */
    MatchEntity findByMatchId(String matchId);
    
    /**
     * Find multiple matches by their matchIds (for batch loading)
     */
    @Query("SELECT m FROM MatchEntity m WHERE m.matchId IN :matchIds")
    List<MatchEntity> findByMatchIdIn(@Param("matchIds") List<String> matchIds);
    
    /**
     * Find ranked matches by queueId (420 = Solo/Duo, 440 = Flex)
     * Uses JOIN FETCH to avoid N+1 query problem
     */
    @Query("SELECT DISTINCT m FROM MatchEntity m " +
        "LEFT JOIN FETCH m.summoner " +
        "WHERE m.summoner = :summoner " +
        "AND (m.queueId = 420 OR m.queueId = 440) " +
        "ORDER BY m.timestamp DESC")
    List<MatchEntity> findRankedMatchesBySummonerOrderByTimestampDesc(@Param("summoner") Summoner summoner);
    
    /**
     * Find ranked matches for a specific queue type
     * Uses JOIN FETCH to avoid N+1 query problem
     */
    @Query("SELECT DISTINCT m FROM MatchEntity m " +
        "LEFT JOIN FETCH m.summoner " +
        "WHERE m.summoner = :summoner " +
        "AND m.queueId = :queueId " +
        "ORDER BY m.timestamp DESC")
    List<MatchEntity> findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(
        @Param("summoner") Summoner summoner, 
        @Param("queueId") Integer queueId);
}
