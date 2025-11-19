package com.tfg.tfg.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.tfg.model.dto.RankHistoryDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.RankHistory;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.mapper.RankHistoryMapper;
import com.tfg.tfg.repository.RankHistoryRepository;

/**
 * Service for managing rank history operations.
 * Tracks summoner rank progression over time.
 */
@Service
public class RankHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(RankHistoryService.class);
    private static final String RANKED_SOLO_QUEUE = "RANKED_SOLO_5x5";

    private final RankHistoryRepository rankHistoryRepository;

    public RankHistoryService(RankHistoryRepository rankHistoryRepository) {
        this.rankHistoryRepository = rankHistoryRepository;
    }

    /**
     * Records a rank snapshot after a match.
     * Automatically calculates LP change from previous entry.
     * 
     * @param summoner The summoner
     * @param match The match that triggered this snapshot
     * @param tier The tier at the time of the match
     * @param rank The rank at the time of the match
     * @param leaguePoints The LP at the time of the match
     * @return The created RankHistory entry
     */
    @Transactional
    public RankHistory recordRankSnapshot(Summoner summoner, MatchEntity match, 
                                          String tier, String rank, Integer leaguePoints) {
        if (summoner == null || match == null) {
            logger.warn("Cannot record rank snapshot: summoner or match is null");
            return null;
        }

        // Skip if no rank data provided
        if (tier == null) {
            logger.debug("Skipping rank snapshot: no rank data provided for match {}", match.getMatchId());
            return null;
        }

        String queueType = determineQueueType(match.getQueueId());
        
        RankHistory rankHistory = new RankHistory();
        rankHistory.setSummoner(summoner);
        rankHistory.setTriggeringMatch(match);
        rankHistory.setTimestamp(match.getTimestamp() != null ? match.getTimestamp() : LocalDateTime.now());
        rankHistory.setTier(tier);
        rankHistory.setRank(rank);
        rankHistory.setLeaguePoints(leaguePoints);
        rankHistory.setQueueType(queueType);

        // Calculate LP change from previous entry
        Optional<RankHistory> previousEntry = rankHistoryRepository
                .findFirstBySummonerAndQueueTypeOrderByTimestampDesc(summoner, queueType);

        if (previousEntry.isPresent()) {
            Integer previousLp = previousEntry.get().getLeaguePoints();
            
            if (previousLp != null && leaguePoints != null) {
                int lpChange = leaguePoints - previousLp;
                rankHistory.setLpChange(lpChange);
                logger.debug("Rank snapshot: {} LP change for {} ({} -> {})", 
                        lpChange > 0 ? "+" + lpChange : lpChange, 
                        summoner.getName(), previousLp, leaguePoints);
            }
        } else {
            logger.debug("First rank snapshot for {} in {}", summoner.getName(), queueType);
        }

        RankHistory saved = rankHistoryRepository.save(rankHistory);
        logger.info("Recorded rank snapshot for {}: {} {} ({} LP)", 
                summoner.getName(), saved.getTier(), saved.getRank(), saved.getLeaguePoints());
        
        return saved;
    }

    /**
     * Gets the rank data (tier, rank, LP) for a specific match.
     * 
     * @param matchId The match ID
     * @return Optional containing the RankHistory if it exists
     */
    public Optional<RankHistory> getRankForMatch(Long matchId) {
        return rankHistoryRepository.findByMatchId(matchId);
    }

    /**
     * Gets the LP at the time of a specific match.
     * 
     * @param matchId The match ID
     * @return Optional containing the LP if found
     */
    public Optional<Integer> getLpForMatch(Long matchId) {
        return rankHistoryRepository.findByMatchId(matchId)
                .map(RankHistory::getLeaguePoints);
    }

    /**
     * Gets rank progression data (ordered chronologically for charts).
     * 
     * @param summonerId The summoner ID
     * @param queueType The queue type
     * @return List of rank history entries in chronological order
     */
    public List<RankHistoryDTO> getRankProgression(Long summonerId, String queueType) {
        List<RankHistory> history = rankHistoryRepository
                .findRankProgressionBySummonerAndQueue(summonerId, queueType);
        
        return history.stream()
                .map(RankHistoryMapper::toDTO)
                .toList();
    }

    /**
     * Gets the peak rank (highest LP) ever reached by a summoner.
     * 
     * @param summoner The summoner
     * @param queueType The queue type
     * @return Optional containing the peak rank history DTO
     */
    public Optional<RankHistoryDTO> getPeakRank(Summoner summoner, String queueType) {
        return rankHistoryRepository
                .findPeakRank(summoner, queueType)
                .map(RankHistoryMapper::toDTO);
    }

    /**
     * Determines queue type from queue ID.
     * 
     * @param queueId The queue ID
     * @return Queue type string
     */
    private String determineQueueType(Integer queueId) {
        if (queueId == null) {
            return RANKED_SOLO_QUEUE; // Default
        }
        
        return switch (queueId) {
            case 420 -> RANKED_SOLO_QUEUE;
            case 440 -> "RANKED_FLEX_SR";
            default -> RANKED_SOLO_QUEUE;
        };
    }
}
