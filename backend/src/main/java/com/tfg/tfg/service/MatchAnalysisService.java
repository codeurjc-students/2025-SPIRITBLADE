package com.tfg.tfg.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.MatchEntityRepository;
import com.tfg.tfg.repository.SummonerRepository;

/**
 * Service demonstrating different ways to query related entities.
 * Shows best practices for handling JPA relationships and complex queries.
 */
@Service
public class MatchAnalysisService {
    
    private final MatchEntityRepository matchRepository;
    private final SummonerRepository summonerRepository;
    
    public MatchAnalysisService(MatchEntityRepository matchRepository, 
                               SummonerRepository summonerRepository) {
        this.matchRepository = matchRepository;
        this.summonerRepository = summonerRepository;
    }
    
    /**
     * EXAMPLE 1: Simple relationship navigation (automatic JOIN by JPA)
     * Gets all matches for a summoner using the @ManyToOne relationship.
     */
    public List<MatchEntity> getMatchesForSummoner(String summonerName) {
        // First query: Get summoner
        Optional<Summoner> summoner = summonerRepository.findByName(summonerName);
        
        if (summoner.isEmpty()) {
            return List.of();
        }
        
        // Second query: Get matches using relationship
        // JPA automatically creates: SELECT * FROM MATCHES WHERE summoner_id = ?
        return matchRepository.findBySummoner(summoner.get());
    }
    
    /**
     * EXAMPLE 2: Using derived query method (automatic JOIN)
     * Spring Data JPA automatically creates the JOIN query.
     */
    public List<MatchEntity> getMatchesBySummonerNameSimple(String summonerName) {
        // Single query generated automatically:
        // SELECT m.* FROM MATCHES m JOIN SUMMONERS s ON m.summoner_id = s.id 
        // WHERE s.name = ?
        return matchRepository.findBySummonerName(summonerName);
    }
    
    /**
     * EXAMPLE 3: Complex query with filters (explicit JOIN)
     * Gets high-performance matches for a summoner.
     */
    public List<MatchEntity> getHighKillMatches(Long summonerId, int minKills) {
        // Custom query with explicit JOIN and multiple conditions
        return matchRepository.findBySummonerIdWithMinKills(summonerId, minKills);
    }
    
    /**
     * EXAMPLE 4: Aggregation query (JOIN with GROUP BY)
     * Calculates win rate statistics.
     */
    public WinRateStats getWinRateForSummoner(Long summonerId) {
        Object[] stats = matchRepository.getWinRateStats(summonerId);
        
        if (stats == null || stats[0] == null) {
            return new WinRateStats(0, 0, 0);
        }
        
        long total = ((Number) stats[0]).longValue();
        long wins = ((Number) stats[1]).longValue();
        long losses = ((Number) stats[2]).longValue();
        
        return new WinRateStats(total, wins, losses);
    }
    
    /**
     * EXAMPLE 5: Multi-table JOIN
     * Gets matches that have notes attached.
     */
    public List<MatchEntity> getMatchesWithNotes(Long summonerId) {
        // Complex query joining MATCHES -> SUMMONER -> NOTES
        return matchRepository.findMatchesWithNotesBySummonerId(summonerId);
    }
    
    /**
     * EXAMPLE 6: Subquery with aggregation
     * Finds active summoners (those with many matches).
     */
    public List<Summoner> getActiveSummoners(long minMatches) {
        // Complex query with GROUP BY and HAVING
        return matchRepository.findActiveSummoners(minMatches);
    }
    
    /**
     * EXAMPLE 7: Lazy loading (accessing relationship in memory)
     * WARNING: This can cause N+1 query problem if not careful!
     */
    public String getSummonerNameFromMatch(Long matchId) {
        Optional<MatchEntity> match = matchRepository.findById(matchId);
        
        if (match.isEmpty()) {
            return null;
        }
        
        // This triggers a second query to load the Summoner
        // because @ManyToOne defaults to EAGER fetch
        Summoner summoner = match.get().getSummoner();
        return summoner != null ? summoner.getName() : null;
    }
    
    /**
     * Inner class to represent win rate statistics
     */
    public static class WinRateStats {
        private final long totalMatches;
        private final long wins;
        private final long losses;
        
        public WinRateStats(long totalMatches, long wins, long losses) {
            this.totalMatches = totalMatches;
            this.wins = wins;
            this.losses = losses;
        }
        
        public long getTotalMatches() { return totalMatches; }
        public long getWins() { return wins; }
        public long getLosses() { return losses; }
        
        public double getWinRate() {
            return totalMatches > 0 ? (wins * 100.0) / totalMatches : 0.0;
        }
    }
}
