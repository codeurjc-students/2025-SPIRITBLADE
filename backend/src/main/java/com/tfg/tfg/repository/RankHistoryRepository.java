package com.tfg.tfg.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tfg.tfg.model.entity.RankHistory;
import com.tfg.tfg.model.entity.Summoner;

/**
 * Repository for RankHistory entity operations.
 */
@Repository
public interface RankHistoryRepository extends JpaRepository<RankHistory, Long> {

    /**
     * Find the most recent rank history entry for a summoner in a specific queue.
     * 
     * @param summoner The summoner
     * @param queueType The queue type
     * @return Optional containing the most recent entry if it exists
     */
    Optional<RankHistory> findFirstBySummonerAndQueueTypeOrderByTimestampDesc(Summoner summoner, String queueType);

    /**
     * Get rank history with calculated LP changes for a summoner.
     * This query returns all entries and allows calculating LP progression.
     * 
     * @param summonerId The summoner ID
     * @param queueType The queue type
     * @return List of rank history entries
     */
    @Query("SELECT rh FROM RankHistory rh WHERE rh.summoner.id = :summonerId " +
           "AND rh.queueType = :queueType ORDER BY rh.timestamp ASC")
    List<RankHistory> findRankProgressionBySummonerAndQueue(
            @Param("summonerId") Long summonerId, 
            @Param("queueType") String queueType);


    /**
     * Get the highest LP ever reached by a summoner in a queue.
     * 
     * @param summoner The summoner
     * @param queueType The queue type
     * @return Optional containing the peak rank entry
     */
    @Query("SELECT rh FROM RankHistory rh WHERE rh.summoner = :summoner " +
           "AND rh.queueType = :queueType " +
           "ORDER BY rh.leaguePoints DESC")
    Optional<RankHistory> findPeakRank(
            @Param("summoner") Summoner summoner,
            @Param("queueType") String queueType);

    /**
     * Find rank history entry associated with a specific match.
     * 
     * @param matchId The match ID
     * @return Optional containing the rank history if it exists
     */
    @Query("SELECT rh FROM RankHistory rh WHERE rh.triggeringMatch.id = :matchId")
    Optional<RankHistory> findByMatchId(@Param("matchId") Long matchId);
}
