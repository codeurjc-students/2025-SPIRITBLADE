package com.tfg.tfg.repository;

import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    /**
     * Find recent matches for a summoner with pagination support
     */
    List<MatchEntity> findBySummonerOrderByTimestampDesc(Summoner summoner);

    /**
     * Find a specific match by matchId
     */
    Optional<MatchEntity> findByMatchId(String matchId);

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

    /**
     * Finds recent matches for a summoner with pagination.
     * Returns matches ordered by most recent first.
     * 
     * @param summoner The summoner entity
     * @param pageable Pagination parameters
     * @return List of matches limited by pagination
     */
    @Query("SELECT m FROM MatchEntity m WHERE m.summoner = :summoner ORDER BY m.timestamp DESC")
    List<MatchEntity> findRecentMatchesBySummoner(@Param("summoner") Summoner summoner,
            org.springframework.data.domain.Pageable pageable);
}
