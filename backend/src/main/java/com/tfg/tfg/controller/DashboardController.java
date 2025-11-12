package com.tfg.tfg.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.model.mapper.SummonerMapper;
import com.tfg.tfg.model.dto.RankHistoryDTO;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.AiAnalysisResponseDto;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private static final String GUEST = "Guest";
    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    
    private final SummonerService summonerService;
    private final RiotService riotService;
    private final MatchService matchService;
    private final UserService userService;
    private final com.tfg.tfg.service.AiAnalysisService aiAnalysisService;
    private final com.tfg.tfg.service.RankHistoryService rankHistoryService;
    private final com.tfg.tfg.service.DashboardService dashboardService;

    public DashboardController(SummonerService summonerService, 
                              RiotService riotService,
                              MatchService matchService,
                              UserService userService,
                              com.tfg.tfg.service.AiAnalysisService aiAnalysisService,
                              com.tfg.tfg.service.RankHistoryService rankHistoryService,
                              com.tfg.tfg.service.DashboardService dashboardService) {
        this.summonerService = summonerService;
        this.riotService = riotService;
        this.matchService = matchService;
        this.userService = userService;
        this.aiAnalysisService = aiAnalysisService;
        this.rankHistoryService = rankHistoryService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/me/stats")
    public ResponseEntity<Map<String, Object>> myStats() {
        String username = resolveUsername();
        String linkedSummonerName = resolveLinkedSummonerName(username);
        
        Summoner summoner = null;
        if (linkedSummonerName != null) {
            summoner = summonerService.findByNameIgnoreCase(linkedSummonerName).orElse(null);
        }

        // Delegate to service for business logic
        Map<String, Object> result = dashboardService.getPersonalStats(summoner);
        
        // Add user info
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
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                // Principal can be a String "anonymousUser" or a UserDetails instance
                if (principal instanceof String s && "anonymousUser".equals(s)) {
                    return GUEST;
                }
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
     * Get ranked match history for the linked summoner
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
            // Delegate to service for business logic
            List<com.tfg.tfg.model.dto.MatchHistoryDTO> rankedMatches = 
                dashboardService.getRankedMatchesWithLP(summoner, queueId, page, size);
            logger.info("Returning {} ranked matches for user {}", rankedMatches.size(), username);
            return ResponseEntity.ok(rankedMatches);
        } catch (Exception e) {
            logger.error("Error fetching ranked matches for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body(List.of());
        }
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
     * AI-powered performance analysis endpoint.
     * Analyzes player's recent RANKED match history using Google Gemini AI.
     * 
     * RATE LIMITING: Users can only request 1 analysis every 5 minutes to prevent API abuse
     * 
     * @param matchCount Number of recent matches to analyze (default: 10, max: 10)
     * @return AI-generated performance analysis
     */
    @PostMapping("/me/ai-analysis")
    public ResponseEntity<?> generateAiAnalysis(@RequestParam(defaultValue = "10") int matchCount) {
        try {
            // Validate match count
            if (matchCount < 10) {
                return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, "At least 10 matches are required for analysis"
                ));
            }
            if (matchCount > 10) {
                matchCount = 10; // Cap at 10 matches
            }
            
            // Get authenticated user
            String username = resolveUsername();
            if (GUEST.equals(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, "You must be logged in to use AI analysis"
                ));
            }
            
            // Get user and check cooldown
            UserModel user = userService.findByName(username).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, "User not found"
                ));
            }
            
            // RATE LIMITING: Check cooldown (5 minutes)
            final int COOLDOWN_MINUTES = 5;
            if (user.getLastAiAnalysisRequest() != null) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime cooldownEnds = user.getLastAiAnalysisRequest().plusMinutes(COOLDOWN_MINUTES);
                
                if (now.isBefore(cooldownEnds)) {
                    long minutesRemaining = java.time.Duration.between(now, cooldownEnds).toMinutes();
                    long secondsRemaining = java.time.Duration.between(now, cooldownEnds).toSeconds() % 60;
                    
                    return ResponseEntity.status(429).body(Map.of(
                        SUCCESS_KEY, false,
                        MESSAGE_KEY, String.format("You must wait %d minutes and %d seconds before requesting another analysis", 
                            minutesRemaining, secondsRemaining),
                        "cooldownEnds", cooldownEnds.toString(),
                        "remainingSeconds", java.time.Duration.between(now, cooldownEnds).toSeconds()
                    ));
                }
            }
            
            // Get linked summoner
            if (user.getLinkedSummonerName() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, "You must link your League of Legends account first"
                ));
            }
            
            // Get summoner
            Summoner summoner = summonerService.findByNameIgnoreCase(user.getLinkedSummonerName())
                .orElse(null);
            
            if (summoner == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, "The linked summoner account was not found"
                ));
            }
            
            // Get recent RANKED matches only (queueId 420 = Solo/Duo, 440 = Flex)
            List<MatchEntity> allMatches = matchService.findRecentMatches(
                summoner, 
                LocalDateTime.now().minusMonths(2)
            );
            
            // Filter only RANKED matches
            List<MatchEntity> rankedMatches = allMatches.stream()
                .filter(match -> {
                    Integer queueId = match.getQueueId();
                    return queueId != null && (queueId == 420 || queueId == 440);
                })
                .limit(matchCount)
                .toList();
            
            if (rankedMatches.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    SUCCESS_KEY, true,
                    "analysis", "No recent ranked matches were found for analysis. Play some ranked matches and try again."
                ));
            }
            
            if (rankedMatches.size() < 5) {
                return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, String.format("Only %d ranked matches were found. At least 5 are required for analysis.", rankedMatches.size())
                ));
            }
            
            logger.info("Generating AI analysis for user {} ({} ranked matches out of {} total)", 
                username, rankedMatches.size(), allMatches.size());
            
            // Update cooldown timestamp BEFORE calling AI service (prevent parallel requests)
            user.setLastAiAnalysisRequest(LocalDateTime.now());
            userService.save(user);
            
            // Generate AI analysis
            AiAnalysisResponseDto analysis = aiAnalysisService.analyzePerformance(summoner, rankedMatches);
            
            return ResponseEntity.ok(analysis);
            
        } catch (IllegalStateException e) {
            logger.error("AI service not configured: {}", e.getMessage());
            return ResponseEntity.status(503).body(Map.of(
                SUCCESS_KEY, false,
                MESSAGE_KEY, "The AI analysis service is currently unavailable"
            ));
        } catch (Exception e) {
            logger.error("Error generating AI analysis: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                SUCCESS_KEY, false,
                MESSAGE_KEY, "Error generating AI analysis: " + e.getMessage()
            ));
        }
    }

    /**
     * Get rank progression data (chronologically ordered for charts).
     * 
     * @param queueType Optional queue type (default: RANKED_SOLO_5x5)
     * @return List of rank history entries in chronological order
     */
    @GetMapping("/me/rank-progression")
    public ResponseEntity<?> getMyRankProgression(
            @RequestParam(defaultValue = "RANKED_SOLO_5x5") String queueType) {
        
        try {
            String username = resolveUsername();
            
            if (GUEST.equals(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, "Authentication required"
                ));
            }
            
            UserModel user = userService.findByName(username).orElse(null);
            
            if (user == null || user.getLinkedSummonerName() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, "No linked account"
                ));
            }
            
            Summoner summoner = summonerService.findByNameIgnoreCase(user.getLinkedSummonerName())
                .orElse(null);
            
            if (summoner == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, "Summoner not found"
                ));
            }
            
            // Get rank progression (chronologically ordered)
            List<RankHistoryDTO> progression = rankHistoryService.getRankProgression(summoner.getId(), queueType);
            
            // Get peak rank
            java.util.Optional<RankHistoryDTO> peakRank = rankHistoryService.getPeakRank(summoner, queueType);
            
            logger.info("Retrieved rank progression with {} entries for user {}", progression.size(), username);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put("progression", progression);
            response.put("totalEntries", progression.size());
            response.put("queueType", queueType);
            peakRank.ifPresent(peak -> response.put("peakRank", peak));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching rank progression: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                SUCCESS_KEY, false,
                MESSAGE_KEY, "Error fetching rank progression: " + e.getMessage()
            ));
        }
    }
}
