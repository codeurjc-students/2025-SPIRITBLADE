package com.tfg.tfg.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;

/**
 * Service layer for Dashboard operations.
 * Handles all business logic for dashboard statistics, LP calculations, and match processing.
 */
@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    private static final String UNRANKED = "Unranked";
    private static final String UNKNOWN = "Unknown";
    private static final String DEFAULT_TIER = "UNRANKED";
    private static final String DEFAULT_RANK = "I";
    private static final int MIN_LP = 0;
    
    private final MatchService matchService;
    private final RiotService riotService;
    private final DataDragonService dataDragonService;
    private final RankHistoryService rankHistoryService;

    public DashboardService(MatchService matchService, 
                          RiotService riotService,
                          DataDragonService dataDragonService,
                          RankHistoryService rankHistoryService) {
        this.matchService = matchService;
        this.riotService = riotService;
        this.dataDragonService = dataDragonService;
        this.rankHistoryService = rankHistoryService;
    }

    /**
     * Get complete personal statistics for a summoner
     */
    public Map<String, Object> getPersonalStats(Summoner summoner) {
        Map<String, Object> stats = new HashMap<>();
        
        if (summoner != null) {
            stats.put("currentRank", formatRank(summoner));
            stats.put("lp7days", calculateLPGainedLast7Days(summoner));
            stats.put("mainRole", calculateMainRole(summoner));
            stats.put("favoriteChampion", getFavoriteChampion(summoner));
        } else {
            stats.put("currentRank", UNRANKED);
            stats.put("lp7days", 0);
            stats.put("mainRole", UNKNOWN);
            stats.put("favoriteChampion", null);
        }
        
        return stats;
    }

    /**
     * Format rank string from tier and rank
     */
    public String formatRank(Summoner summoner) {
        String tier = summoner.getTier() == null ? UNRANKED : summoner.getTier();
        String rank = summoner.getRank() == null ? "" : summoner.getRank();
        return tier.equals(UNRANKED) ? tier : tier + " " + rank;
    }

    /**
     * Calculate LP gained in the last 7 days
     */
    public int calculateLPGainedLast7Days(Summoner summoner) {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<MatchEntity> recentMatches = matchService.findRecentMatches(summoner, sevenDaysAgo);
            
            if (recentMatches.isEmpty()) {
                return 0;
            }
            
            // Get LP from RankHistory for the first (oldest) match
            Optional<Integer> firstMatchLP = rankHistoryService.getLpForMatch(recentMatches.get(0).getId());
            Integer currentLP = summoner.getLp();
            
            if (firstMatchLP.isEmpty() || currentLP == null) {
                return 0;
            }
            
            return currentLP - firstMatchLP.get();
        } catch (Exception e) {
            logger.warn("Error calculating LP for summoner {}: {}", summoner.getName(), e.getMessage());
            return 0;
        }
    }

    /**
     * Calculate main role/lane based on match history
     */
    public String calculateMainRole(Summoner summoner) {
        try {
            List<MatchEntity> recentMatches = matchService.findRecentMatchesForRoleAnalysis(summoner, 20);
            
            if (recentMatches.isEmpty()) {
                return UNKNOWN;
            }
            
            Map<String, Long> laneCounts = recentMatches.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                        MatchEntity::getLane,
                        java.util.stream.Collectors.counting()
                    ));
            
            String mainLane = laneCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(UNKNOWN);
            
            return formatLaneName(mainLane);
        } catch (Exception e) {
            logger.warn("Error calculating main role for summoner {}: {}", summoner.getName(), e.getMessage());
            return UNKNOWN;
        }
    }

    /**
     * Format lane names to user-friendly names
     */
    private String formatLaneName(String lane) {
        if (lane == null || lane.isEmpty()) {
            return UNKNOWN;
        }
        
        return switch (lane.toUpperCase()) {
            case "TOP" -> "Top Lane";
            case "JUNGLE" -> "Jungle";
            case "MIDDLE", "MID" -> "Mid Lane";
            case "BOTTOM", "BOT" -> "Bot Lane";
            case "UTILITY", "SUPPORT" -> "Support";
            default -> lane;
        };
    }

    /**
     * Get favorite champion from Riot API
     */
    public String getFavoriteChampion(Summoner summoner) {
        if (summoner.getPuuid() == null || summoner.getPuuid().isEmpty()) {
            return null;
        }
        
        try {
            List<RiotChampionMasteryDTO> masteries = riotService.getTopChampionMasteries(summoner.getPuuid(), 1);
            if (!masteries.isEmpty()) {
                return masteries.get(0).getChampionName();
            }
        } catch (Exception e) {
            logger.debug("Failed to fetch champion masteries for {}: {}", summoner.getName(), e.getMessage());
        }
        return null;
    }

    /**
     * Get ranked matches with LP calculation
     * Returns cached matches or fetches from API if needed
     */
    public List<MatchHistoryDTO> getRankedMatchesWithLP(Summoner summoner, Integer queueId, int page, int size) {
        // Check database first
        List<MatchEntity> cachedMatches;
        if (queueId != null) {
            cachedMatches = matchService.findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(summoner, queueId);
        } else {
            cachedMatches = matchService.findRankedMatchesBySummonerOrderByTimestampDesc(summoner);
        }
        
        logger.debug("Found {} cached ranked matches for summoner {}", cachedMatches.size(), summoner.getName());
        
        // Check if we need to update from API
        boolean needsUpdate = cachedMatches.isEmpty() || 
                            checkIfCacheNeedsUpdate(cachedMatches, summoner.getPuuid());
        
        // If cache is valid, return from database
        if (!needsUpdate && cachedMatches.size() >= size) {
            return cachedMatches.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::convertMatchEntityToDTO)
                .toList();
        }
        
        // Otherwise, fetch from API
        logger.info("Fetching fresh match data from Riot API for summoner {}", summoner.getName());
        int start = page * size;
        List<MatchHistoryDTO> allMatches = riotService.getMatchHistory(summoner.getPuuid(), start, size);
        
        // Filter ranked matches
        List<MatchHistoryDTO> rankedMatches = filterRankedMatches(allMatches, queueId, size);
        
        // Save matches with LP calculation
        saveMatchesToDatabaseWithLP(summoner, rankedMatches);
        
        return rankedMatches.stream().limit(size).toList();
    }

    /**
     * Convert MatchEntity to DTO
     */
    private MatchHistoryDTO convertMatchEntityToDTO(MatchEntity match) {
        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setMatchId(match.getMatchId());
        dto.setChampionName(match.getChampionName());
        dto.setChampionIconUrl(dataDragonService.getChampionIconUrl(
            match.getChampionId() != null ? match.getChampionId().longValue() : null));
        dto.setWin(match.isWin());
        dto.setKills(match.getKills());
        dto.setDeaths(match.getDeaths());
        dto.setAssists(match.getAssists());
        dto.setGameDuration(match.getGameDuration());
        dto.setGameTimestamp(match.getTimestamp() != null ? 
            match.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC) : null);
        dto.setQueueId(match.getQueueId());
        
        // Load LP from RankHistory if available
        rankHistoryService.getLpForMatch(match.getId())
                .ifPresent(dto::setLpAtMatch);
        
        return dto;
    }

    /**
     * Filter matches by ranked queue ID
     */
    private List<MatchHistoryDTO> filterRankedMatches(List<MatchHistoryDTO> matches, Integer queueId, int limit) {
        return matches.stream()
            .filter(match -> {
                Integer matchQueueId = match.getQueueId();
                if (matchQueueId == null) return false;
                
                if (queueId != null) {
                    return matchQueueId.equals(queueId);
                } else {
                    return matchQueueId == 420 || matchQueueId == 440;
                }
            })
            .limit(limit)
            .toList();
    }

    /**
     * Check if cached matches need update from API
     */
    private boolean checkIfCacheNeedsUpdate(List<MatchEntity> cachedMatches, String puuid) {
        if (cachedMatches.isEmpty()) {
            return true;
        }
        
        MatchEntity mostRecentCached = cachedMatches.get(0);
        String mostRecentMatchId = mostRecentCached.getMatchId();
        
        try {
            List<MatchHistoryDTO> latestFromApi = riotService.getMatchHistory(puuid, 0, 1);
            
            if (!latestFromApi.isEmpty()) {
                String latestApiMatchId = latestFromApi.get(0).getMatchId();
                
                if (!latestApiMatchId.equals(mostRecentMatchId)) {
                    logger.info("New matches detected, updating cache");
                    return true;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not check latest match from API: {}", e.getMessage());
        }
        
        return false;
    }

    /**
     * Save matches to database with LP calculation
     */
    private void saveMatchesToDatabaseWithLP(Summoner summoner, List<MatchHistoryDTO> matches) {
        if (matches.isEmpty()) {
            return;
        }
        
        try {
            List<String> matchIds = matches.stream()
                .map(MatchHistoryDTO::getMatchId)
                .toList();
            
            Map<String, MatchEntity> existingMatches = matchService.findExistingMatchesByMatchIds(matchIds);
            
            // Count matches needing LP calculation
            long matchesNeedingLP = matches.stream()
                .filter(m -> {
                    MatchEntity existing = existingMatches.get(m.getMatchId());
                    if (existing == null) return true;
                    // Check if RankHistory exists for this match
                    return rankHistoryService.getLpForMatch(existing.getId()).isEmpty();
                })
                .count();
            
            // If all have LP, just populate DTOs from RankHistory
            if (matchesNeedingLP == 0) {
                logger.info("All matches already have LP, loading from RankHistory");
                for (MatchHistoryDTO matchDTO : matches) {
                    MatchEntity existing = existingMatches.get(matchDTO.getMatchId());
                    if (existing != null) {
                        rankHistoryService.getLpForMatch(existing.getId())
                                .ifPresent(matchDTO::setLpAtMatch);
                    }
                }
                return;
            }
            
            // Sort matches for LP calculation
            List<MatchHistoryDTO> sortedMatches = new ArrayList<>(matches);
            sortedMatches.sort((a, b) -> Long.compare(
                a.getGameTimestamp() != null ? a.getGameTimestamp() : 0,
                b.getGameTimestamp() != null ? b.getGameTimestamp() : 0
            ));
            
            // Validate summoner has rank data
            String currentTier = summoner.getTier() != null ? summoner.getTier() : DEFAULT_TIER;
            String currentDivision = summoner.getRank() != null ? summoner.getRank() : DEFAULT_RANK;
            int currentLP = summoner.getLp() != null ? summoner.getLp() : MIN_LP;
            
            boolean isRankedSummoner = !DEFAULT_TIER.equals(currentTier) && currentLP >= 0;
            
            if (!isRankedSummoner) {
                logger.warn("Cannot calculate LP for unranked summoner {}", summoner.getName());
                // Save matches without LP tracking
                for (MatchHistoryDTO matchDTO : sortedMatches) {
                    MatchEntity existing = existingMatches.get(matchDTO.getMatchId());
                    MatchEntity match = buildMatchEntity(existing, matchDTO, summoner);
                    matchService.save(match);
                }
                return;
            }
            
            logger.info("Starting LP calculation for {} matches", matchesNeedingLP);
            
            // Process matches and calculate LP
            Map<String, Integer> lpByMatchId = new HashMap<>();
            List<MatchEntity> newMatches = new ArrayList<>();
            
            int lpTracker = currentLP;
            
            // Process backwards in time
            for (int i = sortedMatches.size() - 1; i >= 0; i--) {
                MatchHistoryDTO matchDTO = sortedMatches.get(i);
                MatchEntity existing = existingMatches.get(matchDTO.getMatchId());
                
                // Skip if already has LP in RankHistory
                if (existing != null) {
                    Optional<Integer> existingLp = rankHistoryService.getLpForMatch(existing.getId());
                    if (existingLp.isPresent()) {
                        lpTracker = existingLp.get();
                        lpByMatchId.put(matchDTO.getMatchId(), existingLp.get());
                        continue;
                    }
                }
                
                // Calculate LP for this match
                int lpAtMatchStart = lpTracker;
                boolean won = matchDTO.getWin() != null && matchDTO.getWin();
                
                // Calculate backwards LP change
                lpTracker = calculateBackwardsLpChange(lpTracker, won, currentTier, currentDivision);
                
                // Create/update match entity
                MatchEntity match = buildMatchEntity(existing, matchDTO, summoner);
                
                lpByMatchId.put(matchDTO.getMatchId(), lpAtMatchStart);
                newMatches.add(match);
            }
            
            // Batch save matches and create RankHistory entries
            if (!newMatches.isEmpty()) {
                List<MatchEntity> savedMatches = matchService.saveAll(newMatches);
                
                // Create RankHistory for each match
                for (MatchEntity match : savedMatches) {
                    Integer lp = lpByMatchId.get(match.getMatchId());
                    if (lp != null) {
                        rankHistoryService.recordRankSnapshot(summoner, match, currentTier, currentDivision, lp);
                    }
                }
                
                logger.info("Saved {} matches with RankHistory tracking", savedMatches.size());
            }
            
            // Update DTOs with calculated LP
            for (MatchHistoryDTO matchDTO : matches) {
                Integer calculatedLP = lpByMatchId.get(matchDTO.getMatchId());
                if (calculatedLP != null) {
                    matchDTO.setLpAtMatch(calculatedLP);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error saving matches to database: {}", e.getMessage(), e);
        }
    }

    /**
     * Build match entity from DTO
     */
    private MatchEntity buildMatchEntity(MatchEntity existing, MatchHistoryDTO matchDTO, 
                                        Summoner summoner) {
        MatchEntity match = existing != null ? existing : new MatchEntity();
        match.setMatchId(matchDTO.getMatchId());
        match.setSummoner(summoner);
        match.setChampionName(matchDTO.getChampionName());
        match.setWin(matchDTO.getWin());
        match.setKills(matchDTO.getKills());
        match.setDeaths(matchDTO.getDeaths());
        match.setAssists(matchDTO.getAssists());
        match.setGameDuration(matchDTO.getGameDuration());
        match.setQueueId(matchDTO.getQueueId());
        
        if (matchDTO.getGameTimestamp() != null) {
            match.setTimestamp(LocalDateTime.ofEpochSecond(
                matchDTO.getGameTimestamp(), 0, java.time.ZoneOffset.UTC));
        }
        
        return match;
    }

    /**
     * Calculate LP change going backwards in time
     */
    private int calculateBackwardsLpChange(int lpTracker, boolean won, String currentTier, String divisionTracker) {
        // Inverted: if won, subtract LP (going back in time)
        int lpChange = won ? -20 : +15;
        int newLpTracker = lpTracker + lpChange;
        
        // Handle demotion if LP < 0
        while (newLpTracker < 0 && canDemote(currentTier, divisionTracker)) {
            divisionTracker = demoteDivision(divisionTracker);
            newLpTracker += 100;
        }
        
        // Clamp to 0 if can't demote
        if (newLpTracker < 0) {
            newLpTracker = 0;
        }
        
        return newLpTracker;
    }

    /**
     * Check if can demote from current division
     */
    private boolean canDemote(String tier, String division) {
        if (tier.equals("MASTER") || tier.equals("GRANDMASTER") || tier.equals("CHALLENGER")) {
            return false;
        }
        return !(tier.equals("IRON") && division.equals("IV"));
    }

    /**
     * Demote to previous division
     */
    private String demoteDivision(String currentDivision) {
        return switch (currentDivision) {
            case "I" -> "II";
            case "II" -> "III";
            case "III", "IV" -> "IV";
            default -> currentDivision;
        };
    }
}
