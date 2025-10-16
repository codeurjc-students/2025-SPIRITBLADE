package com.tfg.tfg.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;

@Repository
public interface MatchEntityRepository extends JpaRepository<MatchEntity, Long> {
    
    /**
     * Simple query: finds all matches for a specific summoner using the relationship.
     * JPA automatically generates the JOIN based on the @ManyToOne relationship.
     * 
     * @param summoner The summoner entity
     * @return List of matches played by this summoner
     */
    List<MatchEntity> findBySummoner(Summoner summoner);
    
    /**
     * Query with explicit JOIN and multiple conditions.
     * This demonstrates a complex query combining relationships and filters.
     * 
     * @param summonerId The summoner's ID
     * @param minKills Minimum number of kills
     * @return List of matches where summoner had at least minKills
     */
    @Query("SELECT m FROM MatchEntity m " +
           "JOIN m.summoner s " +
           "WHERE s.id = :summonerId AND m.kills >= :minKills " +
           "ORDER BY m.timestamp DESC")
    List<MatchEntity> findBySummonerIdWithMinKills(
        @Param("summonerId") Long summonerId, 
        @Param("minKills") int minKills
    );
    
    /**
     * Complex query with aggregation: gets win rate statistics.
     * Demonstrates JOIN with GROUP BY and aggregate functions.
     * 
     * @param summonerId The summoner's ID
     * @return Array with [totalMatches, wins, losses]
     */
    @Query("SELECT COUNT(m), " +
           "       SUM(CASE WHEN m.win = true THEN 1 ELSE 0 END), " +
           "       SUM(CASE WHEN m.win = false THEN 1 ELSE 0 END) " +
           "FROM MatchEntity m " +
           "WHERE m.summoner.id = :summonerId")
    Object[] getWinRateStats(@Param("summonerId") Long summonerId);
    
    /**
     * Query using derived method name with relationship navigation.
     * Spring Data JPA automatically creates the JOIN.
     * 
     * @param summonerName The summoner's name
     * @return List of matches for summoner with given name
     */
    List<MatchEntity> findBySummonerName(String summonerName);
    
    /**
     * Complex multi-table JOIN query.
     * Finds matches where summoner has notes attached.
     * Demonstrates joining through multiple relationships.
     * 
     * @param summonerId The summoner's ID
     * @return List of matches with notes
     */
    @Query("SELECT DISTINCT m FROM MatchEntity m " +
           "JOIN m.summoner s " +
           "LEFT JOIN NOTES n ON n.match.id = m.id " +
           "WHERE s.id = :summonerId AND n.id IS NOT NULL")
    List<MatchEntity> findMatchesWithNotesBySummonerId(@Param("summonerId") Long summonerId);
    
    /**
     * Query with subquery: finds summoners who played more than X matches.
     * Demonstrates complex query with nested SELECT.
     * 
     * @param minMatches Minimum number of matches
     * @return List of summoners with at least minMatches games
     */
    @Query("SELECT DISTINCT m.summoner FROM MatchEntity m " +
           "GROUP BY m.summoner " +
           "HAVING COUNT(m) >= :minMatches")
    List<Summoner> findActiveSummoners(@Param("minMatches") long minMatches);
    
    /**
     * Finds a cached match by its Riot match ID.
     * Used to check if match data is already in database before calling API.
     * 
     * @param matchId The Riot match ID (e.g., "EUW1_1234567890")
     * @return Optional containing the match if found in cache
     */
    Optional<MatchEntity> findByMatchId(String matchId);
    
    /**
     * Finds all matches for a summoner ordered by most recent first.
     * Used to get cached match history from database.
     * 
     * @param summoner The summoner entity
     * @return List of matches ordered by timestamp descending
     */
    List<MatchEntity> findBySummonerOrderByTimestampDesc(Summoner summoner);
    
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
