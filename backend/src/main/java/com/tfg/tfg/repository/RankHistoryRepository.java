package com.tfg.tfg.repository;

import java.time.LocalDateTime;
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
     * Find all rank history entries for a summoner, ordered by timestamp descending.
     * 
     * @param summoner The summoner
     * @return List of rank history entries
     */
    List<RankHistory> findBySummonerOrderByTimestampDesc(Summoner summoner);

    /**
     * Find rank history for a specific queue type.
     * 
     * @param summoner The summoner
     * @param queueType The queue type (e.g., "RANKED_SOLO_5x5")
     * @return List of rank history entries
     */
    List<RankHistory> findBySummonerAndQueueTypeOrderByTimestampDesc(Summoner summoner, String queueType);

    /**
     * Find the most recent rank history entry for a summoner in a specific queue.
     * 
     * @param summoner The summoner
     * @param queueType The queue type
     * @return Optional containing the most recent entry if it exists
     */
    Optional<RankHistory> findFirstBySummonerAndQueueTypeOrderByTimestampDesc(Summoner summoner, String queueType);

    /**
     * Find rank history entries within a date range.
     * 
     * @param summoner The summoner
     * @param startDate Start date
     * @param endDate End date
     * @return List of rank history entries
     */
    List<RankHistory> findBySummonerAndTimestampBetweenOrderByTimestampDesc(
            Summoner summoner, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count total rank history entries for a summoner.
     * 
     * @param summoner The summoner
     * @return Count of entries
     */
    long countBySummoner(Summoner summoner);

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
     * Find entries where LP increased (wins).
     * 
     * @param summoner The summoner
     * @param queueType The queue type
     * @return List of winning rank history entries
     */
    @Query("SELECT rh FROM RankHistory rh WHERE rh.summoner = :summoner " +
           "AND rh.queueType = :queueType AND rh.lpChange > 0 " +
           "ORDER BY rh.timestamp DESC")
    List<RankHistory> findWinningEntries(
            @Param("summoner") Summoner summoner,
            @Param("queueType") String queueType);

    /**
     * Find entries where LP decreased (losses).
     * 
     * @param summoner The summoner
     * @param queueType The queue type
     * @return List of losing rank history entries
     */
    @Query("SELECT rh FROM RankHistory rh WHERE rh.summoner = :summoner " +
           "AND rh.queueType = :queueType AND rh.lpChange < 0 " +
           "ORDER BY rh.timestamp DESC")
    List<RankHistory> findLosingEntries(
            @Param("summoner") Summoner summoner,
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
     * Delete old rank history entries before a certain date.
     * Useful for cleanup/maintenance.
     * 
     * @param cutoffDate The date before which entries should be deleted
     */
    void deleteByTimestampBefore(LocalDateTime cutoffDate);
}
