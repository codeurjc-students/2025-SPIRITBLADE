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
import com.tfg.tfg.mapper.RankHistoryMapper;
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
     * @return The created RankHistory entry
     */
    @Transactional
    public RankHistory recordRankSnapshot(Summoner summoner, MatchEntity match) {
        if (summoner == null || match == null) {
            logger.warn("Cannot record rank snapshot: summoner or match is null");
            return null;
        }

        // Skip if match doesn't have rank data
        if (match.getTierAtMatch() == null) {
            logger.debug("Skipping rank snapshot: no rank data in match {}", match.getMatchId());
            return null;
        }

        String queueType = determineQueueType(match.getQueueId());
        
        RankHistory rankHistory = new RankHistory();
        rankHistory.setSummoner(summoner);
        rankHistory.setTriggeringMatch(match);
        rankHistory.setTimestamp(match.getTimestamp() != null ? match.getTimestamp() : LocalDateTime.now());
        rankHistory.setTier(match.getTierAtMatch());
        rankHistory.setRank(match.getRankAtMatch());
        rankHistory.setLeaguePoints(match.getLpAtMatch());
        rankHistory.setQueueType(queueType);

        // Calculate LP change from previous entry
        Optional<RankHistory> previousEntry = rankHistoryRepository
                .findFirstBySummonerAndQueueTypeOrderByTimestampDesc(summoner, queueType);

        if (previousEntry.isPresent()) {
            Integer previousLp = previousEntry.get().getLeaguePoints();
            Integer currentLp = match.getLpAtMatch();
            
            if (previousLp != null && currentLp != null) {
                int lpChange = currentLp - previousLp;
                rankHistory.setLpChange(lpChange);
                logger.debug("Rank snapshot: {} LP change for {} ({} -> {})", 
                        lpChange > 0 ? "+" + lpChange : lpChange, 
                        summoner.getName(), previousLp, currentLp);
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
     * Gets rank history for a summoner in Ranked Solo/Duo queue.
     * 
     * @param summoner The summoner
     * @return List of rank history DTOs
     */
    public List<RankHistoryDTO> getRankHistory(Summoner summoner) {
        return getRankHistory(summoner, RANKED_SOLO_QUEUE);
    }

    /**
     * Gets rank history for a summoner in a specific queue.
     * 
     * @param summoner The summoner
     * @param queueType The queue type
     * @return List of rank history DTOs
     */
    public List<RankHistoryDTO> getRankHistory(Summoner summoner, String queueType) {
        List<RankHistory> history = rankHistoryRepository
                .findBySummonerAndQueueTypeOrderByTimestampDesc(summoner, queueType);
        
        return history.stream()
                .map(RankHistoryMapper::toDTO)
                .toList();
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
     * Gets the most recent rank snapshot for a summoner.
     * 
     * @param summoner The summoner
     * @param queueType The queue type
     * @return Optional containing the most recent rank history DTO
     */
    public Optional<RankHistoryDTO> getCurrentRank(Summoner summoner, String queueType) {
        return rankHistoryRepository
                .findFirstBySummonerAndQueueTypeOrderByTimestampDesc(summoner, queueType)
                .map(RankHistoryMapper::toDTO);
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
     * Calculates average LP gain/loss over a time period.
     * 
     * @param summoner The summoner
     * @param queueType The queue type
     * @param startDate Start date
     * @param endDate End date
     * @return Average LP change per game
     */
    public Double calculateAverageLpChange(Summoner summoner, String queueType, 
                                          LocalDateTime startDate, LocalDateTime endDate) {
        List<RankHistory> history = rankHistoryRepository
                .findBySummonerAndTimestampBetweenOrderByTimestampDesc(summoner, startDate, endDate);
        
        if (history.isEmpty()) {
            return 0.0;
        }

        double totalLpChange = history.stream()
                .filter(rh -> rh.getLpChange() != null)
                .mapToInt(RankHistory::getLpChange)
                .sum();
        
        long countWithLpChange = history.stream()
                .filter(rh -> rh.getLpChange() != null)
                .count();

        return countWithLpChange > 0 ? totalLpChange / countWithLpChange : 0.0;
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

    /**
     * Cleans up old rank history entries (for maintenance).
     * 
     * @param beforeDate Delete entries before this date
     */
    @Transactional
    public void cleanupOldEntries(LocalDateTime beforeDate) {
        logger.info("Cleaning up rank history entries before {}", beforeDate);
        rankHistoryRepository.deleteByTimestampBefore(beforeDate);
    }

    /**
     * Gets count of rank history entries for a summoner.
     * 
     * @param summoner The summoner
     * @return Count of entries
     */
    public long getEntryCount(Summoner summoner) {
        return rankHistoryRepository.countBySummoner(summoner);
    }
}
