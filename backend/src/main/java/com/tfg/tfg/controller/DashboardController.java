package com.tfg.tfg.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.model.mapper.SummonerMapper;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.AiAnalysisResponseDto;
import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.service.interfaces.IRiotService;
import com.tfg.tfg.service.interfaces.ISummonerService;
import com.tfg.tfg.service.interfaces.IAiAnalysisService;
import com.tfg.tfg.service.interfaces.IDashboardService;
import com.tfg.tfg.service.interfaces.IMatchService;
import com.tfg.tfg.service.interfaces.IUserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private static final String GUEST = "Guest";
    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String SUMMONER_NOT_FOUND_MSG = "Summoner not found";
    private static final String USER_NOT_FOUND_MSG = "User not found";

    private final ISummonerService summonerService;
    private final IRiotService riotService;
    private final IMatchService matchService;
    private final IUserService userService;
    private final IAiAnalysisService aiAnalysisService;
    private final IDashboardService dashboardService;

    public DashboardController(ISummonerService summonerService,
            IRiotService riotService,
            IMatchService matchService,
            IUserService userService,
            IAiAnalysisService aiAnalysisService,
            IDashboardService dashboardService) {
        this.summonerService = summonerService;
        this.riotService = riotService;
        this.matchService = matchService;
        this.userService = userService;
        this.aiAnalysisService = aiAnalysisService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/me/stats")
    public ResponseEntity<Map<String, Object>> myStats() {
        String username = resolveUsername();
        String linkedSummonerName = resolveLinkedSummonerName(username);

        Summoner summoner = null;
        if (linkedSummonerName != null) {
            summoner = summonerService.findByName(linkedSummonerName).orElse(null);
        }

        Map<String, Object> result = dashboardService.getPersonalStats(summoner);

        result.put("username", username);
        result.put("linkedSummoner", linkedSummonerName);

        if (!GUEST.equals(username)) {
            userService.findByName(username).ifPresent(user -> {
                result.put("email", user.getEmail());
            });
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Resolves the authenticated username or returns GUEST
     */
    private String resolveUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof String s && "anonymousUser".equals(s)) {
                return GUEST;
            }
            return auth.getName();
        }
        return GUEST;
    }

    /**
     * Finds the summoner name linked to the authenticated user from UserModel
     */
    private String resolveLinkedSummonerName(String username) {
        return userService.findByName(username)
                .map(UserModel::getLinkedSummonerName)
                .orElse(null);
    }

    @GetMapping("/me/favorites")
    @Transactional(readOnly = true)
    public ResponseEntity<List<SummonerDTO>> myFavorites() {
        String username = resolveUsername();

        if (GUEST.equals(username)) {
            return ResponseEntity.ok(List.of());
        }

        UserModel user = userService.findByName(username).orElse(null);

        if (user == null) {
            return ResponseEntity.ok(List.of());
        }

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
            return ResponseEntity.status(404).body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, USER_NOT_FOUND_MSG));
        }

        Summoner summoner = summonerService.findByName(summonerName).orElse(null);

        if (summoner == null) {
            logger.info("Summoner not in database, fetching from Riot API");
            try {
                SummonerDTO summonerDTO = riotService.getSummonerByName(summonerName);
                if (summonerDTO == null) {
                    return ResponseEntity.status(404)
                            .body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, SUMMONER_NOT_FOUND_MSG));
                }

                summoner = summonerService.findByName(summonerName).orElse(null);
                if (summoner == null) {
                    return ResponseEntity.status(404).body(
                            Map.of(SUCCESS_KEY, false, MESSAGE_KEY, "Summoner not found in database after API fetch"));
                }
            } catch (Exception e) {
                logger.error("Error fetching summoner from Riot API: {}", e.getMessage());
                return ResponseEntity.status(404)
                        .body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, SUMMONER_NOT_FOUND_MSG + ": " + e.getMessage()));
            }
        }

        if (summonerName.equalsIgnoreCase(user.getLinkedSummonerName())) {
            return ResponseEntity.badRequest()
                    .body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, "Cannot add your own linked account as favorite"));
        }

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
            return ResponseEntity.status(404).body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, USER_NOT_FOUND_MSG));
        }

        Summoner summoner = summonerService.findByName(summonerName).orElse(null);
        if (summoner == null) {
            return ResponseEntity.status(404).body(Map.of(SUCCESS_KEY, false, MESSAGE_KEY, SUMMONER_NOT_FOUND_MSG));
        }

        user.removeFavoriteSummoner(summoner);
        userService.save(user);

        return ResponseEntity.ok(Map.of(SUCCESS_KEY, true, MESSAGE_KEY, "Summoner removed from favorites"));
    }

    /**
     * Get ranked match history for the linked summoner
     */
    @GetMapping("/me/ranked-matches")
    public ResponseEntity<List<MatchHistoryDTO>> getRankedMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) Integer queueId) {

        String username = resolveUsername();
        String linkedSummonerName = resolveLinkedSummonerName(username);

        if (linkedSummonerName == null) {
            return ResponseEntity.ok(List.of());
        }

        Summoner summoner = summonerService.findByName(linkedSummonerName).orElse(null);
        if (summoner == null || summoner.getPuuid() == null) {
            return ResponseEntity.ok(List.of());
        }

        try {
            List<MatchHistoryDTO> rankedMatches = dashboardService
                    .getRankedMatchesWithLP(summoner, queueId, page, size);
            logger.info("Returning {} ranked matches", rankedMatches.size());
            return ResponseEntity.ok(rankedMatches);
        } catch (Exception e) {
            logger.error("Error fetching ranked matches: {}", e.getMessage());
            return ResponseEntity.status(500).body(List.of());
        }
    }

    /**
     * AI-powered performance analysis endpoint.
     * Analyzes player's recent RANKED match history using Google Gemini AI.
     * 
     * RATE LIMITING: Users can only request 1 analysis every 5 minutes to prevent
     * API abuse
     * 
     * @param matchCount Number of recent matches to analyze (default: 10, max: 10)
     * @return AI-generated performance analysis
     */
    @PostMapping("/me/ai-analysis")
    public ResponseEntity<Object> generateAiAnalysis(@RequestParam(defaultValue = "10") int matchCount) {
        try {
            if (matchCount < 10) {
                return ResponseEntity.badRequest().body(Map.of(
                        SUCCESS_KEY, false,
                        MESSAGE_KEY, "At least 10 matches are required for analysis"));
            }
            if (matchCount > 10) {
                matchCount = 10;
            }

            String username = resolveUsername();
            if (GUEST.equals(username)) {
                return ResponseEntity.status(401).body(Map.of(
                        SUCCESS_KEY, false,
                        MESSAGE_KEY, "You must be logged in to use AI analysis"));
            }

            UserModel user = userService.findByName(username).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        SUCCESS_KEY, false,
                        MESSAGE_KEY, USER_NOT_FOUND_MSG));
            }

            if (user.getLastAiAnalysisRequest() != null) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime cooldownEnds = user.getLastAiAnalysisRequest().plusMinutes(5);

                if (now.isBefore(cooldownEnds)) {
                    long minutesRemaining = java.time.Duration.between(now, cooldownEnds).toMinutes();
                    long secondsRemaining = java.time.Duration.between(now, cooldownEnds).toSeconds() % 60;

                    return ResponseEntity.status(429).body(Map.of(
                            SUCCESS_KEY, false,
                            MESSAGE_KEY,
                            String.format("You must wait %d minutes and %d seconds before requesting another analysis",
                                    minutesRemaining, secondsRemaining),
                            "cooldownEnds", cooldownEnds.toString(),
                            "remainingSeconds", java.time.Duration.between(now, cooldownEnds).toSeconds()));
                }
            }

            if (user.getLinkedSummonerName() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        SUCCESS_KEY, false,
                        MESSAGE_KEY, "You must link your League of Legends account first"));
            }

            Summoner summoner = summonerService.findByName(user.getLinkedSummonerName())
                    .orElse(null);

            if (summoner == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        SUCCESS_KEY, false,
                        MESSAGE_KEY, "The linked summoner account was not found"));
            }

            List<MatchEntity> allMatches = matchService.findRecentMatches(
                    summoner,
                    LocalDateTime.now().minusMonths(2));

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
                        "analysis",
                        "No recent ranked matches were found for analysis. Play some ranked matches and try again."));
            }

            if (rankedMatches.size() < 5) {
                return ResponseEntity.badRequest().body(Map.of(
                        SUCCESS_KEY, false,
                        MESSAGE_KEY,
                        String.format("Only %d ranked matches were found. At least 5 are required for analysis.",
                                rankedMatches.size())));
            }

            logger.info("Generating AI analysis ({} ranked matches out of {} total)",
                    rankedMatches.size(), allMatches.size());

            user.setLastAiAnalysisRequest(LocalDateTime.now());
            userService.save(user);

            AiAnalysisResponseDto analysis = aiAnalysisService.analyzePerformance(summoner, rankedMatches);

            return ResponseEntity.ok(analysis);

        } catch (IllegalStateException e) {
            logger.error("AI service not configured: {}", e.getMessage());
            return ResponseEntity.status(503).body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, "The AI analysis service is currently unavailable"));
        } catch (Exception e) {
            logger.error("Error generating AI analysis: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    SUCCESS_KEY, false,
                    MESSAGE_KEY, "Error generating AI analysis: " + e.getMessage()));
        }
    }
}
