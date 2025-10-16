package com.tfg.tfg.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
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
import com.tfg.tfg.repository.MatchRepository;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.service.RiotService;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private static final String UNRANKED = "Unranked";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String DEFAULT_TIER = "UNRANKED";
    private static final String DEFAULT_RANK = "I";
    private static final int MIN_LP = 0;
    private static final int MAX_LP = 100;
    
    private final SummonerRepository summonerRepository;
    private final RiotService riotService;
    private final MatchRepository matchRepository;

    public DashboardController(SummonerRepository summonerRepository, 
                              RiotService riotService,
                              MatchRepository matchRepository) {
        this.summonerRepository = summonerRepository;
        this.riotService = riotService;
        this.matchRepository = matchRepository;
    }

    @GetMapping("/me/stats")
    public ResponseEntity<Map<String, Object>> myStats() {
        Map<String, Object> result = new HashMap<>();
        // Use optimized query instead of fetching all summoners
        Summoner summoner = summonerRepository.findFirstByOrderByIdAsc().orElse(null);

        // Resolve authenticated username and linked summoner
        String username = resolveUsername();
        String linkedSummonerName = resolveLinkedSummonerName(username);

        // Populate stats based on summoner data
        populateSummonerStats(result, summoner);
        
        // Include user info in response
        result.put("username", username);
        result.put("linkedSummoner", linkedSummonerName);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Resolves the authenticated username or returns "Guest"
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
        return "Guest";
    }
    
    /**
     * Finds the summoner linked to the authenticated user using optimized DB query
     */
    private String resolveLinkedSummonerName(String username) {
        if ("Guest".equals(username)) {
            return null;
        }
        
        try {
            // Use optimized query instead of in-memory filtering
            return summonerRepository.findLinkedSummonerByUsername(username)
                .map(Summoner::getName)
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
            result.put("lp7days", 42);
            result.put("mainRole", "Mid Lane");
            result.put("favoriteChampion", getFavoriteChampion(summoner));
        } else {
            result.put("currentRank", UNRANKED);
            result.put("lp7days", 0);
            result.put("mainRole", "Unknown");
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

    @GetMapping("/me/favorites")
    public ResponseEntity<List<SummonerDTO>> myFavorites() {
        // Development stub: return up to two favorite summoners for the "own" summoner.
        // Reuse resolveUsername() and resolveLinkedSummonerName() instead of duplicating auth logic
        String username = resolveUsername();
        String linkedSummonerName = resolveLinkedSummonerName(username);
        
        // Find the own summoner using optimized query
        Summoner own = null;
        if (linkedSummonerName != null) {
            own = summonerRepository.findLinkedSummonerByUsername(linkedSummonerName).orElse(null);
        }
        
        // Fallback: if no own resolved, use the first summoner
        if (own == null) {
            own = summonerRepository.findFirstByOrderByIdAsc().orElse(null);
        }
        
        if (own == null) {
            return ResponseEntity.ok(List.of());
        }

        // Get top 2 summoners excluding the own summoner using optimized query
        Summoner finalOwn = own;
        List<SummonerDTO> list = summonerRepository
                .findTopByIdNotOrderByLastSearchedAtDesc(finalOwn.getId(), PageRequest.of(0, 2))
                .stream()
                .map(s -> SummonerMapper.toDTO(s, riotService.getDataDragonService()))
                .toList();

        return ResponseEntity.ok(list);
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
        
        Summoner summoner = null;
        if (linkedSummonerName != null) {
            summoner = summonerRepository.findLinkedSummonerByUsername(linkedSummonerName).orElse(null);
        }
        
        // Fallback to first summoner if no linked account
        if (summoner == null) {
            summoner = summonerRepository.findFirstByOrderByIdAsc().orElse(null);
        }
        
        if (summoner == null) {
            return ResponseEntity.ok(List.of());
        }
        
        // Get ranked matches ordered by timestamp DESC (most recent first)
        List<MatchEntity> rankedMatches = matchRepository.findRankedMatchesBySummoner(summoner, "RANKED");
        
        // Convert to mutable list and reverse to get oldest first for calculation
        List<MatchEntity> mutableMatches = new java.util.ArrayList<>(rankedMatches);
        java.util.Collections.reverse(mutableMatches);
        
        // Calculate LP progression from current LP backwards
        List<RankHistoryDTO> dtos = calculateLPProgression(summoner, mutableMatches);
        
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
        int cumulativeWins = summoner.getWins() != null ? summoner.getWins() : 0;
        int cumulativeLosses = summoner.getLosses() != null ? summoner.getLosses() : 0;
        
        // Go backwards through match history
        for (int i = rankedMatches.size() - 1; i >= 0; i--) {
            MatchEntity match = rankedMatches.get(i);
            
            // Calculate LP and wins/losses BEFORE this match
            int lpChange = calculateLPChange(currentTier, match.isWin());
            calculatedLP -= lpChange; // Subtract because we're going backwards
            
            int winsBeforeMatch = cumulativeWins;
            int lossesBeforeMatch = cumulativeLosses;
            
            if (match.isWin()) {
                winsBeforeMatch--;
            } else {
                lossesBeforeMatch--;
            }
            
            // Create DTO with state AFTER this match (chronologically)
            RankHistoryDTO dto = new RankHistoryDTO();
            dto.setDate(match.getTimestamp().format(DATE_FORMATTER));
            dto.setTier(currentTier);
            dto.setRank(currentRank);
            int lpAfterMatch = calculatedLP + lpChange;
            dto.setLeaguePoints(Math.clamp(lpAfterMatch, MIN_LP, MAX_LP)); // LP after match
            
            // Wins/losses should be cumulative UP TO this point
            if (match.isWin()) {
                dto.setWins(winsBeforeMatch + 1);
                dto.setLosses(lossesBeforeMatch);
            } else {
                dto.setWins(winsBeforeMatch);
                dto.setLosses(lossesBeforeMatch + 1);
            }
            
            result.add(0, dto);
            
            // Update for next iteration (going backwards)
            cumulativeWins = winsBeforeMatch;
            cumulativeLosses = lossesBeforeMatch;
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
}
