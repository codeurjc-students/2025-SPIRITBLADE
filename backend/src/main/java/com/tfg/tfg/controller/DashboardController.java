package com.tfg.tfg.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.mapper.SummonerMapper;
import com.tfg.tfg.model.dto.RankHistoryDTO;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.service.SummonerService;
import com.tfg.tfg.service.MatchService;
import com.tfg.tfg.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private static final String UNRANKED = "Unranked";
    private static final String GUEST = "Guest";
    private static final String UNKNOWN = "Unknown";
    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Helper class to encapsulate match processing context
     */
    private static class MatchProcessingContext {
        List<com.tfg.tfg.model.dto.MatchHistoryDTO> sortedMatches;
        Map<String, MatchEntity> existingMatches;
        Summoner summoner;
        String currentTier;
        String divisionTracker;
        int lpTracker;
        Map<String, Integer> lpByMatchId;
        List<MatchEntity> newMatches;
        
        MatchProcessingContext(List<com.tfg.tfg.model.dto.MatchHistoryDTO> sortedMatches,
                               Map<String, MatchEntity> existingMatches,
                               Summoner summoner,
                               String currentTier,
                               String divisionTracker,
                               int lpTracker) {
            this.sortedMatches = sortedMatches;
            this.existingMatches = existingMatches;
            this.summoner = summoner;
            this.currentTier = currentTier;
            this.divisionTracker = divisionTracker;
            this.lpTracker = lpTracker;
            this.lpByMatchId = new java.util.HashMap<>();
            this.newMatches = new java.util.ArrayList<>();
        }
    }
    private static final String DEFAULT_TIER = "UNRANKED";
    private static final String DEFAULT_RANK = "I";
    private static final int MIN_LP = 0;
    private static final int MAX_LP = 100;
    
    private final SummonerService summonerService;
    private final RiotService riotService;
    private final MatchService matchService;
    private final UserService userService;
    private final com.tfg.tfg.service.DataDragonService dataDragonService;

    public DashboardController(SummonerService summonerService, 
                              RiotService riotService,
                              MatchService matchService,
                              UserService userService,
                              com.tfg.tfg.service.DataDragonService dataDragonService) {
        this.summonerService = summonerService;
        this.riotService = riotService;
        this.matchService = matchService;
        this.userService = userService;
        this.dataDragonService = dataDragonService;
    }

    @GetMapping("/me/stats")
    public ResponseEntity<Map<String, Object>> myStats() {
        Map<String, Object> result = new HashMap<>();
        
        // Resolve authenticated username and linked summoner
        String username = resolveUsername();
        String linkedSummonerName = resolveLinkedSummonerName(username);
        
        // Only use linked summoner data, do NOT fallback to DataInitializer
        Summoner summoner = null;
        if (linkedSummonerName != null) {
            summoner = summonerService.findByNameIgnoreCase(linkedSummonerName).orElse(null);
        }

        // Populate stats based on summoner data
        populateSummonerStats(result, summoner);
        
        // Include user info in response
        result.put("username", username);
        result.put("linkedSummoner", linkedSummonerName);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Resolves the authenticated username or returns GUEST
     */
    private String resolveUsername() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ex) {
            // ignore and return default
        }
        return GUEST;
    }
    
    /**
     * Finds the summoner name linked to the authenticated user from UserModel
     */
    private String resolveLinkedSummonerName(String username) {
        if (GUEST.equals(username)) {
            return null;
        }
        
        try {
            // Get the user and return their linked summoner name
            return userService.findByName(username)
                .map(UserModel::getLinkedSummonerName)
                .orElse(null);
        } catch (Exception ex) {
            // ignore and return null
        }
        return null;
    }
    
    /**
     * Populates the result map with summoner stats
     */
    private void populateSummonerStats(Map<String, Object> result, Summoner summoner) {
        if (summoner != null) {
            result.put("currentRank", formatRank(summoner));
            result.put("lp7days", calculateLPGainedLast7Days(summoner));
            result.put("mainRole", calculateMainRole(summoner));
            result.put("favoriteChampion", getFavoriteChampion(summoner));
        } else {
            result.put("currentRank", UNRANKED);
            result.put("lp7days", 0);
            result.put("mainRole", UNKNOWN);
            result.put("favoriteChampion", null);
        }
    }
    
    /**
     * Formats the rank string from tier and rank
     */
    private String formatRank(Summoner summoner) {
        String tier = summoner.getTier() == null ? UNRANKED : summoner.getTier();
        String rank = summoner.getRank() == null ? "" : summoner.getRank();
        return tier.equals(UNRANKED) ? tier : tier + " " + rank;
    }
    
    /**
     * Gets the favorite champion from Riot API champion mastery
     */
    private String getFavoriteChampion(Summoner summoner) {
        if (summoner.getPuuid() == null || summoner.getPuuid().isEmpty()) {
            return null;
        }
        
        try {
            List<RiotChampionMasteryDTO> masteries = riotService.getTopChampionMasteries(summoner.getPuuid(), 1);
            if (!masteries.isEmpty()) {
                return masteries.get(0).getChampionName();
            }
        } catch (Exception e) {
            // If API fails, return null
        }
        return null;
    }
    
    /**
     * Calculates LP gained in the last 7 days based on match history
     */
    private int calculateLPGainedLast7Days(Summoner summoner) {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            
            // Get matches from last 7 days sorted by timestamp
            List<MatchEntity> recentMatches = matchService.findRecentMatches(summoner, sevenDaysAgo);
            
            if (recentMatches.isEmpty()) {
                return 0;
            }
            
            // Calculate LP difference between first match and current LP
            Integer firstMatchLP = recentMatches.get(0).getLpAtMatch();
            Integer currentLP = summoner.getLp();
            
            if (firstMatchLP == null || currentLP == null) {
                return 0;
            }
            
            // Calculate the net LP gain/loss
            return currentLP - firstMatchLP;
        } catch (Exception e) {
            // If calculation fails, return 0
            return 0;
        }
    }
    
    /**
     * Calculates the main role/lane based on match history
     */
    private String calculateMainRole(Summoner summoner) {
        try {
            // Get recent matches to analyze roles
            List<MatchEntity> recentMatches = matchService.findRecentMatchesForRoleAnalysis(summoner, 20);
            
            if (recentMatches.isEmpty()) {
                return UNKNOWN;
            }
            
            // Count occurrences of each lane
            Map<String, Long> laneCounts = recentMatches.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                        MatchEntity::getLane,
                        java.util.stream.Collectors.counting()
                    ));
            
            // Find the most played lane
            String mainLane = laneCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(UNKNOWN);
            
            // Format lane name nicely
            return formatLaneName(mainLane);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
    
    /**
     * Formats Riot API lane names to user-friendly names
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
            default -> lane; // Return as-is if unknown
        };
    }

    @GetMapping("/me/favorites")
    @Transactional(readOnly = true)
    public ResponseEntity<List<SummonerDTO>> myFavorites() {
        // Return favorite summoners for the authenticated user
        String username = resolveUsername();
        
        if (GUEST.equals(username)) {
            return ResponseEntity.ok(List.of());
        }
        
        // Get user and their favorite summoners
        UserModel user = userService.findByName(username).orElse(null);
        
        if (user == null) {
            return ResponseEntity.ok(List.of());
        }

        // Get user's favorite summoners
        List<SummonerDTO> favorites = user.getFavoriteSummoners()
                .stream()
                .map(s -> SummonerMapper.toDTO(s, riotService.getDataDragonService()))
                .toList();

        return ResponseEntity.ok(favorites);
    }

    /**
     * Add a summoner to user's favorites
     */
    @PostMapping("/me/favorites/{summonerName}")
    @Transactional
    public ResponseEntity<Map<String, Object>> addFavorite(@PathVariable String summonerName) {
        String username = resolveUsername();
        
        if (GUEST.equals(username)) {
            return ResponseEntity.status(401).body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, "User not authenticated"));
        }
        
        UserModel user = userService.findByName(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, "User not found"));
        }
        
        // First try to find summoner in database
        Summoner summoner = summonerService.findByNameIgnoreCase(summonerName).orElse(null);
        
        // If not in database, try to fetch from Riot API
        if (summoner == null) {
            logger.info("Summoner {} not in database, fetching from Riot API", summonerName);
            try {
                SummonerDTO summonerDTO = riotService.getSummonerByName(summonerName);
                if (summonerDTO == null) {
                    return ResponseEntity.status(404).body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, "Summoner not found"));
                }
                
                // After API call, summoner should be saved to DB, retrieve it
                summoner = summonerService.findByNameIgnoreCase(summonerName).orElse(null);
                if (summoner == null) {
                    return ResponseEntity.status(404).body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, "Summoner not found in database after API fetch"));
                }
            } catch (Exception e) {
                logger.error("Error fetching summoner from Riot API: {}", e.getMessage());
                return ResponseEntity.status(404).body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, "Summoner not found: " + e.getMessage()));
            }
        }
        
        // Check if it's the user's own linked summoner
        if (summonerName.equalsIgnoreCase(user.getLinkedSummonerName())) {
            return ResponseEntity.badRequest().body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, "Cannot add your own linked account as favorite"));
        }
        
        // Add to favorites
        user.addFavoriteSummoner(summoner);
        userService.save(user);
        
        return ResponseEntity.ok(Map.of(SUCCESS_KEY, true, MESSAGE_KEY, "Summoner added to favorites"));
    }

    /**
     * Remove a summoner from user's favorites
     */
    @DeleteMapping("/me/favorites/{summonerName}")
    @Transactional
    public ResponseEntity<Map<String, Object>> removeFavorite(@PathVariable String summonerName) {
        String username = resolveUsername();
        
        if (GUEST.equals(username)) {
            return ResponseEntity.status(401).body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, "User not authenticated"));
        }
        
        UserModel user = userService.findByName(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, "User not found"));
        }
        
        // Find summoner by name
        Summoner summoner = summonerService.findByNameIgnoreCase(summonerName).orElse(null);
        if (summoner == null) {
            return ResponseEntity.status(404).body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, "Summoner not found"));
        }
        
        // Remove from favorites
        user.removeFavoriteSummoner(summoner);
        userService.save(user);
        
        return ResponseEntity.ok(Map.of(SUCCESS_KEY, true, MESSAGE_KEY, "Summoner removed from favorites"));
    }

    /**
     * Get rank history for LP progression chart
     * Returns historical data for the authenticated user's linked summoner
     * Calculates LP backwards from current LP based on match results
     */
    @GetMapping("/me/rank-history")
    public ResponseEntity<List<RankHistoryDTO>> myRankHistory() {
        // Get the authenticated user's linked summoner
        String username = resolveUsername();
        String linkedSummonerName = resolveLinkedSummonerName(username);
        
        // ONLY show rank history if user has a linked summoner
        // Do NOT fallback to DataInitializer summoners
        if (linkedSummonerName == null) {
            return ResponseEntity.ok(List.of());
        }
        
        Summoner summoner = summonerService.findByNameIgnoreCase(linkedSummonerName).orElse(null);
        
        if (summoner == null) {
            return ResponseEntity.ok(List.of());
        }
        
        // Get ranked matches ordered by timestamp ASC (oldest first)
        List<MatchEntity> rankedMatches = matchService.findRankedMatchesBySummoner(summoner, "RANKED");
        
        // Calculate LP progression - matches are already ordered oldest first
        List<RankHistoryDTO> dtos = calculateLPProgression(summoner, rankedMatches);
        
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Calculate LP progression using real match data
     * @param summoner The summoner with current LP
     * @param rankedMatches Matches ordered from oldest to newest
     * @return List of RankHistoryDTO with LP progression
     */
    private List<RankHistoryDTO> calculateLPProgression(Summoner summoner, List<MatchEntity> rankedMatches) {
        if (rankedMatches.isEmpty()) {
            return createCurrentStateDTO(summoner);
        }
        
        // Check if matches have real LP data stored
        boolean hasRealLPData = rankedMatches.stream()
            .anyMatch(m -> m.getLpAtMatch() != null && m.getTierAtMatch() != null);
        
        return hasRealLPData 
            ? buildProgressionFromRealData(summoner, rankedMatches)
            : buildProgressionFromCalculation(summoner, rankedMatches);
    }
    
    /**
     * Create single DTO with current summoner state
     */
    private List<RankHistoryDTO> createCurrentStateDTO(Summoner summoner) {
        RankHistoryDTO current = new RankHistoryDTO();
        current.setDate(java.time.LocalDateTime.now().format(DATE_FORMATTER));
        current.setTier(summoner.getTier() != null ? summoner.getTier() : DEFAULT_TIER);
        current.setRank(summoner.getRank() != null ? summoner.getRank() : DEFAULT_RANK);
        current.setLeaguePoints(summoner.getLp() != null ? summoner.getLp() : MIN_LP);
        current.setWins(summoner.getWins() != null ? summoner.getWins() : 0);
        current.setLosses(summoner.getLosses() != null ? summoner.getLosses() : 0);
        return java.util.Collections.singletonList(current);
    }
    
    /**
     * Build progression using real LP data stored in matches
     */
    private List<RankHistoryDTO> buildProgressionFromRealData(Summoner summoner, List<MatchEntity> rankedMatches) {
        List<RankHistoryDTO> result = new java.util.ArrayList<>();
        int cumulativeWins = 0;
        int cumulativeLosses = 0;
        
        String currentTier = summoner.getTier() != null ? summoner.getTier() : DEFAULT_TIER;
        String currentRank = summoner.getRank() != null ? summoner.getRank() : DEFAULT_RANK;
        int currentLP = summoner.getLp() != null ? summoner.getLp() : MIN_LP;
        
        for (MatchEntity match : rankedMatches) {
            if (match.isWin()) {
                cumulativeWins++;
            } else {
                cumulativeLosses++;
            }
            
            RankHistoryDTO dto = new RankHistoryDTO();
            dto.setDate(match.getTimestamp().format(DATE_FORMATTER));
            dto.setTier(match.getTierAtMatch() != null ? match.getTierAtMatch() : currentTier);
            dto.setRank(match.getRankAtMatch() != null ? match.getRankAtMatch() : currentRank);
            dto.setLeaguePoints(match.getLpAtMatch() != null ? match.getLpAtMatch() : currentLP);
            dto.setWins(cumulativeWins);
            dto.setLosses(cumulativeLosses);
            
            result.add(dto);
        }
        
        return result;
    }
    
    /**
     * Build progression by calculating LP backwards from current state
     * Uses tier-based LP gain/loss estimation for more accurate results
     */
    private List<RankHistoryDTO> buildProgressionFromCalculation(Summoner summoner, List<MatchEntity> rankedMatches) {
        List<RankHistoryDTO> result = new java.util.ArrayList<>();
        
        int calculatedLP = summoner.getLp() != null ? summoner.getLp() : MIN_LP;
        String currentTier = summoner.getTier() != null ? summoner.getTier() : DEFAULT_TIER;
        String currentRank = summoner.getRank() != null ? summoner.getRank() : DEFAULT_RANK;
        
        // Calculate wins/losses going forward, LP going backward
        int forwardWins = 0;
        int forwardLosses = 0;
        
        for (int i = 0; i < rankedMatches.size(); i++) {
            MatchEntity match = rankedMatches.get(i);
            
            // Update cumulative forward count
            if (match.isWin()) {
                forwardWins++;
            } else {
                forwardLosses++;
            }
            
            // Calculate LP for this point (working backwards from current LP)
            int lpAtThisPoint = calculatedLP;
            
            // For LP calculation: work backwards from the end to this point
            for (int j = rankedMatches.size() - 1; j > i; j--) {
                MatchEntity laterMatch = rankedMatches.get(j);
                int change = calculateLPChange(currentTier, laterMatch.isWin());
                lpAtThisPoint -= change;
            }
            
            // Create DTO with state AFTER this match
            RankHistoryDTO dto = new RankHistoryDTO();
            dto.setDate(match.getTimestamp().format(DATE_FORMATTER));
            dto.setTier(currentTier);
            dto.setRank(currentRank);
            dto.setLeaguePoints(Math.clamp(lpAtThisPoint, MIN_LP, MAX_LP));
            dto.setWins(forwardWins);
            dto.setLosses(forwardLosses);
            
            result.add(dto);
        }
        
        return result;
    }
    
    /**
     * Calculates estimated LP gain or loss based on tier
     * These values are based on Riot's known LP distribution patterns
     * 
     * @param tier Current tier (IRON, BRONZE, SILVER, etc.)
     * @param isWin True if victory, false if defeat
     * @return Estimated LP change (positive for win, negative for loss)
     */
    private int calculateLPChange(String tier, boolean isWin) {
        if (tier == null) {
            tier = DEFAULT_TIER;
        }
        
        // LP gains are higher in lower ranks to help players climb faster
        // LP losses are lower in lower ranks to prevent frustration
        int lpGain;
        int lpLoss;
        
        switch (tier.toUpperCase()) {
            case "IRON":
                lpGain = 25;  // High gains for new players
                lpLoss = 10;  // Low losses to encourage learning
                break;
            case "BRONZE":
                lpGain = 23;
                lpLoss = 12;
                break;
            case "SILVER":
                lpGain = 21;
                lpLoss = 14;
                break;
            case "EMERALD":
                lpGain = 18;
                lpLoss = 17;
                break;
            case "PLATINUM":
                lpGain = 19;
                lpLoss = 16;
                break;
            case "DIAMOND":
                lpGain = 17;
                lpLoss = 18;  // Harder to maintain high ranks
                break;
            case "MASTER", "GRANDMASTER", "CHALLENGER":
                lpGain = 15;  // Lower gains at top ranks
                lpLoss = 20;  // Higher losses to maintain competitive integrity
                break;
            case DEFAULT_TIER, "GOLD":
            default:
                lpGain = 20;  // Standard gains
                lpLoss = 15;  // Standard losses
                break;
        }
        
        return isWin ? lpGain : -lpLoss;
    }
    
    /**
     * Get ranked match history for the linked summoner
     * Returns only RANKED_SOLO_5x5 and RANKED_FLEX_SR matches
     */
    @GetMapping("/me/ranked-matches")
    public ResponseEntity<List<com.tfg.tfg.model.dto.MatchHistoryDTO>> getRankedMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) Integer queueId) {
        
        String username = resolveUsername();
        String linkedSummonerName = resolveLinkedSummonerName(username);
        
        if (linkedSummonerName == null) {
            return ResponseEntity.ok(List.of());
        }
        
        Summoner summoner = summonerService.findByNameIgnoreCase(linkedSummonerName).orElse(null);
        if (summoner == null || summoner.getPuuid() == null) {
            return ResponseEntity.ok(List.of());
        }
        
        try {
            // STRATEGY: Check database first to avoid API calls
            List<MatchEntity> cachedRankedMatches;
            if (queueId != null) {
                // Filter by specific queue (420=Solo/Duo or 440=Flex)
                cachedRankedMatches = matchService.findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(summoner, queueId);
            } else {
                // Get all ranked matches (both queues)
                cachedRankedMatches = matchService.findRankedMatchesBySummonerOrderByTimestampDesc(summoner);
            }
            
            String queueTypeLog = determineQueueTypeLog(queueId);
            logger.info("💾 Found {} cached ranked matches ({}) in database for user {}", 
                cachedRankedMatches.size(), queueTypeLog, username);
            
            // Check if we need to update from API
            boolean needsUpdate = cachedRankedMatches.isEmpty() 
                || checkIfCacheNeedsUpdate(cachedRankedMatches, summoner.getPuuid());
            
            if (cachedRankedMatches.isEmpty()) {
                logger.info("📥 No cached matches, fetching from API");
            }
            
            // If cache is valid, return from database
            if (!needsUpdate && cachedRankedMatches.size() >= size) {
                List<com.tfg.tfg.model.dto.MatchHistoryDTO> result = cachedRankedMatches.stream()
                    .skip((long) page * size)
                    .limit(size)
                    .map(this::convertMatchEntityToDTO)
                    .toList();
                
                // LP is already saved in database, no need to calculate
                
                logger.info("📊 Returning {} ranked matches from cache", result.size());
                return ResponseEntity.ok(result);
            }
            
            // Otherwise, fetch from API and update database
            logger.info("📥 Fetching fresh data from Riot API...");
            int start = page * size;
            
            // Fetch more matches to ensure we get enough ranked ones (API returns all types)
            List<com.tfg.tfg.model.dto.MatchHistoryDTO> allMatches = 
                riotService.getMatchHistory(summoner.getPuuid(), start, size);

            logger.info("� Fetched {} total matches from API for user {}", allMatches.size(), username);

            // Filter by queueId (now available in MatchHistoryDTO)
            List<com.tfg.tfg.model.dto.MatchHistoryDTO> rankedMatches = filterRankedMatches(allMatches, queueId, size);
            
            logger.info("📊 Filtered to {} ranked matches from {} total (queueId filter: {})", 
                rankedMatches.size(), allMatches.size(), queueId != null ? queueId : "420 or 440");
            
            // Save all matches to database for future caching (batch operation)
            // LP is calculated and saved inside saveMatchesToDatabase
            saveMatchesToDatabase(summoner, rankedMatches);
            
            logger.info("📊 Returning {} ranked matches (fetched from API)", rankedMatches.size());
            
            // LP already calculated and saved in database, return matches as-is
            return ResponseEntity.ok(rankedMatches.stream().limit(size).toList());
            
        } catch (Exception e) {
            logger.error("Error fetching ranked matches for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
    /**
     * Convert MatchEntity to MatchHistoryDTO
     */
    private com.tfg.tfg.model.dto.MatchHistoryDTO convertMatchEntityToDTO(MatchEntity match) {
        com.tfg.tfg.model.dto.MatchHistoryDTO dto = new com.tfg.tfg.model.dto.MatchHistoryDTO();
        dto.setMatchId(match.getMatchId());
        dto.setChampionName(match.getChampionName());
        dto.setChampionIconUrl(dataDragonService.getChampionIconUrl(match.getChampionId() != null ? match.getChampionId().longValue() : null));
        dto.setWin(match.isWin());
        dto.setKills(match.getKills());
        dto.setDeaths(match.getDeaths());
        dto.setAssists(match.getAssists());
        dto.setGameDuration(match.getGameDuration());
        // Convert LocalDateTime (UTC) to epoch seconds
        dto.setGameTimestamp(match.getTimestamp() != null ? match.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC) : null);
        dto.setQueueId(match.getQueueId());
        dto.setLpAtMatch(match.getLpAtMatch());  // Include saved LP
        return dto;
    }
    
    /**
     * Save multiple matches to database in batch (optimized)
     * Calculates and saves LP at match time for accurate historical tracking
     * Also updates the input DTOs with calculated LP values
     */
    private void saveMatchesToDatabase(Summoner summoner, List<com.tfg.tfg.model.dto.MatchHistoryDTO> matches) {
        if (matches.isEmpty()) {
            return;
        }
        
        try {
            // Batch check which matches already exist
            List<String> matchIds = matches.stream()
                .map(com.tfg.tfg.model.dto.MatchHistoryDTO::getMatchId)
                .toList();
            
            Map<String, MatchEntity> existingMatches = matchService.findExistingMatchesByMatchIds(matchIds);
            
            logger.debug("💾 Batch saving: {} matches, {} already exist", matches.size(), existingMatches.size());
            
            // Sort matches by timestamp (oldest first) for LP calculation
            List<com.tfg.tfg.model.dto.MatchHistoryDTO> sortedMatches = new java.util.ArrayList<>(matches);
            sortedMatches.sort((a, b) -> Long.compare(
                a.getGameTimestamp() != null ? a.getGameTimestamp() : 0,
                b.getGameTimestamp() != null ? b.getGameTimestamp() : 0
            ));
            
            // Calculate LP progression starting from current LP and working backwards
            String currentTier = summoner.getTier() != null ? summoner.getTier() : DEFAULT_TIER;
            String currentDivision = summoner.getRank() != null ? summoner.getRank() : DEFAULT_RANK;
            int currentLP = summoner.getLp() != null ? summoner.getLp() : MIN_LP;
            
            logger.info("🎯 Starting LP calculation - Current LP: {}, Tier: {}, Division: {}, Matches: {}", 
                currentLP, currentTier, currentDivision, sortedMatches.size());
            
            // Track LP and division as we go BACKWARDS in time
            int lpTracker = currentLP;
            String divisionTracker = currentDivision;
            
            // Create processing context
            MatchProcessingContext context = new MatchProcessingContext(
                sortedMatches, existingMatches, summoner, currentTier, divisionTracker, lpTracker);
            
            // Process matches BACKWARDS (newest to oldest) since we're working from current LP
            lpTracker = processMatchesBackwards(context);
            
            logger.info("📈 LP calculation complete: Traced back to approximately {} LP (from current {})", 
                lpTracker, currentLP);
            
            // Batch save
            if (!context.newMatches.isEmpty()) {
                matchService.saveAll(context.newMatches);
                logger.info("💾 Batch saved {} new/updated matches with LP tracking", context.newMatches.size());
            }
            
            // Update all input DTOs with calculated LP
            for (com.tfg.tfg.model.dto.MatchHistoryDTO matchDTO : matches) {
                Integer calculatedLP = context.lpByMatchId.get(matchDTO.getMatchId());
                if (calculatedLP != null) {
                    matchDTO.setLpAtMatch(calculatedLP);
                }
            }
            
        } catch (Exception e) {
            logger.warn("⚠️ Could not batch save matches to database: {}", e.getMessage());
        }
    }
    
    /**
     * Check if a summoner can be demoted from their current division
     * Master/Grandmaster/Challenger don't have divisions, so they can't demote via division system
     * Iron IV is the lowest division, can't demote
     */
    private boolean canDemote(String tier, String division) {
        // Master+ don't have divisions
        if (tier.equals("MASTER") || tier.equals("GRANDMASTER") || tier.equals("CHALLENGER")) {
            return false;
        }
        // Iron IV is the lowest possible rank
        return !(tier.equals("IRON") && division.equals("IV"));
    }
    
    /**
     * Demote to the previous division within the same tier
     * I -> II -> III -> IV
     */
    private String demoteDivision(String currentDivision) {
        return switch (currentDivision) {
            case "I" -> "II";
            case "II" -> "III";
            case "III", "IV" -> "IV"; // Can't demote past IV
            default -> currentDivision;
        };
    }
    
    /**
     * Refresh match history for the linked summoner
     * Fetches recent ranked matches from Riot API and saves them to database
     */
    @PostMapping("/me/refresh-matches")
    public ResponseEntity<Map<String, Object>> refreshMatches() {
        String username = resolveUsername();
        String linkedSummonerName = resolveLinkedSummonerName(username);
        
        if (linkedSummonerName == null) {
            return ResponseEntity.badRequest().body(Map.of(
                SUCCESS_KEY, false, 
                MESSAGE_KEY, "No linked summoner account found"
            ));
        }
        
        Summoner summoner = summonerService.findByNameIgnoreCase(linkedSummonerName).orElse(null);
        if (summoner == null || summoner.getPuuid() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                SUCCESS_KEY, false, 
                MESSAGE_KEY, "Summoner not found or missing PUUID"
            ));
        }
        
        try {
            // Fetch recent matches from Riot API - this will cache them automatically
            List<com.tfg.tfg.model.dto.MatchHistoryDTO> matches = riotService.getMatchHistory(summoner.getPuuid(), 0, 30);
            
            // Count how many were actually saved/cached
            int matchCount = matches != null ? matches.size() : 0;
            
            return ResponseEntity.ok(Map.of(
                SUCCESS_KEY, true,
                MESSAGE_KEY, "Match history refreshed successfully",
                "matchesProcessed", matchCount
            ));
        } catch (Exception e) {
            logger.error("Error refreshing matches for summoner {}: {}", linkedSummonerName, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                SUCCESS_KEY, false,
                MESSAGE_KEY, "Failed to refresh match history: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Helper method to determine queue type for logging
     */
    private String determineQueueTypeLog(Integer queueId) {
        if (queueId == null) {
            return "All Ranked";
        }
        return queueId == 420 ? "Solo/Duo" : "Flex";
    }
    
    /**
     * Helper method to extract the last 6 characters of match ID for logging
     */
    private String getMatchIdSuffix(String matchId) {
        if (matchId == null) {
            return "null";
        }
        return matchId.substring(Math.max(0, matchId.length() - 6));
    }
    
    /**
     * Helper method to build match entity from DTO
     */
    private MatchEntity buildMatchEntity(MatchEntity existing, com.tfg.tfg.model.dto.MatchHistoryDTO matchDTO, 
                                          Summoner summoner, int lpAtMatchStart) {
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
            match.setTimestamp(java.time.LocalDateTime.ofEpochSecond(
                matchDTO.getGameTimestamp(), 0, java.time.ZoneOffset.UTC));
        }
        
        match.setLpAtMatch(lpAtMatchStart);
        return match;
    }
    
    /**
     * Helper method to calculate LP change going backwards in time
     */
    private int calculateBackwardsLpChange(int lpTracker, boolean won, String currentTier, String divisionTracker) {
        // INVERTED because we're going backwards: if they won, we subtract LP
        int lpChange = won ? -20 : +15;
        int newLpTracker = lpTracker + lpChange;
        String newDivision = divisionTracker;
        
        // Handle division demotion if LP goes below 0
        while (newLpTracker < 0 && canDemote(currentTier, newDivision)) {
            newDivision = demoteDivision(newDivision);
            newLpTracker += 100; // Add 100 LP from previous division
            logger.debug("⬇️ Demotion simulated while going backwards: now at {} {}, LP={}", 
                currentTier, newDivision, newLpTracker);
        }
        
        // If we can't demote and LP < 0, clamp to 0 (Iron IV case)
        if (newLpTracker < 0) {
            newLpTracker = 0;
        }
        
        return newLpTracker;
    }
    
    /**
     * Filter matches by ranked queue ID
     */
    private List<com.tfg.tfg.model.dto.MatchHistoryDTO> filterRankedMatches(
            List<com.tfg.tfg.model.dto.MatchHistoryDTO> matches, Integer queueId, int limit) {
        return matches.stream()
            .filter(match -> {
                Integer matchQueueId = match.getQueueId();
                if (matchQueueId == null) return false;
                
                // Filter by requested queue or both ranked queues
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
     * Helper method to check if cached matches need update from API
     */
    private boolean checkIfCacheNeedsUpdate(List<MatchEntity> cachedMatches, String puuid) {
        if (cachedMatches.isEmpty()) {
            return true;
        }
        
        // Get the most recent match from database
        MatchEntity mostRecentCached = cachedMatches.get(0);
        String mostRecentMatchId = mostRecentCached.getMatchId();
        
        // Check API for the latest match
        try {
            List<com.tfg.tfg.model.dto.MatchHistoryDTO> latestFromApi = 
                riotService.getMatchHistory(puuid, 0, 1);
            
            if (!latestFromApi.isEmpty()) {
                String latestApiMatchId = latestFromApi.get(0).getMatchId();
                
                if (!latestApiMatchId.equals(mostRecentMatchId)) {
                    logger.info("New matches detected (latest API: {} vs cached: {}), updating...", 
                        latestApiMatchId, mostRecentMatchId);
                    return true;
                } else {
                    logger.info("Cache is up to date, using database data");
                }
            }
        } catch (Exception e) {
            logger.warn("Could not check latest match from API, using cache: {}", e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Process matches backwards in time to calculate LP progression
     * Returns updated lpTracker after processing all matches
     */
    private int processMatchesBackwards(MatchProcessingContext ctx) {
        int updatedLpTracker = ctx.lpTracker;
        
        for (int i = ctx.sortedMatches.size() - 1; i >= 0; i--) {
            com.tfg.tfg.model.dto.MatchHistoryDTO matchDTO = ctx.sortedMatches.get(i);
            MatchEntity existing = ctx.existingMatches.get(matchDTO.getMatchId());
            
            // If match exists with valid LP data, use it
            if (existing != null && existing.getLpAtMatch() != null && existing.getLpAtMatch() > 0) {
                ctx.lpByMatchId.put(matchDTO.getMatchId(), existing.getLpAtMatch());
                if (logger.isDebugEnabled()) {
                    logger.debug("Match {} (existing): LP={}", 
                        getMatchIdSuffix(matchDTO.getMatchId()), existing.getLpAtMatch());
                }
                continue;
            }
            
            // LP at the START of this match
            int lpAtMatchStart = updatedLpTracker;
            boolean won = matchDTO.getWin() != null && matchDTO.getWin();
            int lpChange = won ? -20 : +15;
            updatedLpTracker = calculateBackwardsLpChange(updatedLpTracker, won, ctx.currentTier, ctx.divisionTracker);
            
            // Create/update match entity
            MatchEntity match = buildMatchEntity(existing, matchDTO, ctx.summoner, lpAtMatchStart);
            
            if (logger.isDebugEnabled()) {
                logger.debug("Match {} (new): Win={}, LPAtStart={}, Change={}, LPBefore={}", 
                    getMatchIdSuffix(matchDTO.getMatchId()), won, lpAtMatchStart, lpChange, updatedLpTracker);
            }
            
            ctx.lpByMatchId.put(matchDTO.getMatchId(), lpAtMatchStart);
            ctx.newMatches.add(match);
        }
        return updatedLpTracker;
    }
}
