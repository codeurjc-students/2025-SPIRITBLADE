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
import com.tfg.tfg.model.mapper.MatchMapper;
import com.tfg.tfg.service.interfaces.IDataDragonService;
import com.tfg.tfg.service.interfaces.IDashboardService;
import com.tfg.tfg.service.interfaces.IMatchService;
import com.tfg.tfg.service.interfaces.IRankHistoryService;
import com.tfg.tfg.service.interfaces.IRiotService;

/**
 * Service layer for Dashboard operations.
 * Handles all business logic for dashboard statistics, LP calculations, and
 * match processing.
 */
@Service
public class DashboardService implements IDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    private static final String UNRANKED = "Unranked";
    private static final String UNKNOWN = "Unknown";
    private static final String DEFAULT_TIER = "UNRANKED";
    private static final String DEFAULT_RANK = "I";
    private static final int MIN_LP = 0;
    private static final String DEFAULT_KDA = "0/0/0";

    private final IMatchService matchService;
    private final IRiotService riotService;
    private final IDataDragonService dataDragonService;
    private final IRankHistoryService rankHistoryService;

    public DashboardService(IMatchService matchService,
            IRiotService riotService,
            IDataDragonService dataDragonService,
            IRankHistoryService rankHistoryService) {
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
            stats.put("averageKda", calculateAverageKDA(summoner));
            stats.put("avgVisionScore", calculateAverageVisionScore(summoner));
        } else {
            stats.put("currentRank", UNRANKED);
            stats.put("lp7days", 0);
            stats.put("mainRole", UNKNOWN);
            stats.put("favoriteChampion", null);
            stats.put("averageKda", 0.0);
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
                            java.util.stream.Collectors.counting()));

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
     * Calculate average Vision Score from recent ranked matches (last 7 days)
     */
    public Double calculateAverageVisionScore(Summoner summoner) {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<MatchEntity> recentMatches = matchService.findRecentMatches(summoner, sevenDaysAgo);

            if (recentMatches.isEmpty()) {
                return 0.0;
            }

            List<MatchEntity> rankedMatches = recentMatches.stream()
                    .filter(match -> {
                        Integer queueId = match.getQueueId();
                        return queueId != null && (queueId == 420 || queueId == 440);
                    })
                    .toList();

            if (rankedMatches.isEmpty()) {
                return 0.0;
            }

            double totalVision = 0;
            int count = 0;

            for (MatchEntity match : rankedMatches) {
                if (match.getVisionScore() != null) {
                    totalVision += match.getVisionScore();
                    count++;
                }
            }

            if (count == 0)
                return 0.0;

            double avg = totalVision / count;
            return Math.round(avg * 10.0) / 10.0;
        } catch (Exception e) {
            logger.warn("Error calculating average Vision Score for summoner {}: {}", summoner.getName(),
                    e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calculate average KDA from recent ranked matches (last 7 days)
     */
    public String calculateAverageKDA(Summoner summoner) {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<MatchEntity> recentMatches = matchService.findRecentMatches(summoner, sevenDaysAgo);

            if (recentMatches.isEmpty()) {
                return DEFAULT_KDA;
            }

            List<MatchEntity> rankedMatches = recentMatches.stream()
                    .filter(match -> {
                        Integer queueId = match.getQueueId();
                        return queueId != null && (queueId == 420 || queueId == 440);
                    })
                    .toList();

            if (rankedMatches.isEmpty()) {
                return DEFAULT_KDA;
            }

            int totalKills = 0;
            int totalDeaths = 0;
            int totalAssists = 0;

            for (MatchEntity match : rankedMatches) {
                totalKills += match.getKills();
                totalDeaths += match.getDeaths();
                totalAssists += match.getAssists();
            }

            int matchesCount = rankedMatches.size();
            int avgKills = totalKills / matchesCount;
            int avgDeaths = totalDeaths / matchesCount;
            int avgAssists = totalAssists / matchesCount;
            return avgKills + "/" + avgDeaths + "/" + avgAssists;
        } catch (Exception e) {
            logger.warn("Error calculating average KDA for summoner {}: {}", summoner.getName(), e.getMessage());
            return DEFAULT_KDA;
        }
    }

    /**
     * Get ranked matches with LP calculation
     * Returns cached matches or fetches from API if needed
     */
    public List<MatchHistoryDTO> getRankedMatchesWithLP(Summoner summoner, Integer queueId, int page, int size) {
        List<MatchEntity> cachedMatches = loadCachedMatches(summoner, queueId);
        logger.debug("Found {} cached ranked matches for summoner {}", cachedMatches.size(), summoner.getName());

        boolean needsUpdate = cachedMatches.isEmpty() || checkIfCacheNeedsUpdate(cachedMatches, summoner.getPuuid());
        if (!needsUpdate && cachedMatches.size() >= size) {
            return buildPageFromCache(cachedMatches, page, size);
        }

        return fetchAndSaveFreshMatches(summoner, queueId, page, size);
    }

    private List<MatchEntity> loadCachedMatches(Summoner summoner, Integer queueId) {
        if (queueId != null) {
            return matchService.findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(summoner, queueId);
        }
        return matchService.findRankedMatchesBySummonerOrderByTimestampDesc(summoner);
    }

    private List<MatchHistoryDTO> buildPageFromCache(List<MatchEntity> cachedMatches, int page, int size) {
        return cachedMatches.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::convertMatchEntityToDTO)
                .toList();
    }

    private List<MatchHistoryDTO> fetchAndSaveFreshMatches(Summoner summoner, Integer queueId, int page, int size) {
        logger.info("Fetching fresh match data from Riot API for summoner {}", summoner.getName());
        List<MatchHistoryDTO> allMatches = riotService.getMatchHistory(summoner.getPuuid(), page * size, size);
        List<MatchHistoryDTO> rankedMatches = filterRankedMatches(allMatches, queueId, size);
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
        dto.setGameTimestamp(
                match.getTimestamp() != null ? match.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC) : null);
        dto.setQueueId(match.getQueueId());

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
                    if (matchQueueId == null)
                        return false;

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

            Map<String, MatchEntity> existingMatches = matchService.findExistingMatchesByMatchIdsAndSummoner(matchIds, summoner);

            long matchesNeedingLP = countMatchesNeedingLP(matches, existingMatches);

            if (matchesNeedingLP == 0) {
                logger.info("All matches already have LP, loading from RankHistory");
                populateLpFromHistory(matches, existingMatches);
                return;
            }

            List<MatchHistoryDTO> sortedMatches = new ArrayList<>(matches);
            sortedMatches.sort((a, b) -> Long.compare(
                    a.getGameTimestamp() != null ? a.getGameTimestamp() : 0,
                    b.getGameTimestamp() != null ? b.getGameTimestamp() : 0));

            if (!isRankedSummoner(summoner)) {
                logger.warn("Cannot calculate LP for unranked summoner {}", summoner.getName());
                saveMatchesWithoutLp(sortedMatches, existingMatches, summoner);
                return;
            }

            logger.info("Starting LP calculation for {} matches", matchesNeedingLP);

            processAndSaveMatchesWithLp(sortedMatches, matches, existingMatches, summoner);

        } catch (Exception e) {
            logger.error("Error saving matches to database: {}", e.getMessage(), e);
        }
    }

    private long countMatchesNeedingLP(List<MatchHistoryDTO> matches, Map<String, MatchEntity> existingMatches) {
        return matches.stream()
                .filter(m -> {
                    MatchEntity existing = existingMatches.get(m.getMatchId());
                    if (existing == null)
                        return true;
                    return rankHistoryService.getLpForMatch(existing.getId()).isEmpty();
                })
                .count();
    }

    private void populateLpFromHistory(List<MatchHistoryDTO> matches, Map<String, MatchEntity> existingMatches) {
        for (MatchHistoryDTO matchDTO : matches) {
            MatchEntity existing = existingMatches.get(matchDTO.getMatchId());
            if (existing != null) {
                rankHistoryService.getLpForMatch(existing.getId())
                        .ifPresent(matchDTO::setLpAtMatch);
            }
        }
    }

    private boolean isRankedSummoner(Summoner summoner) {
        String currentTier = summoner.getTier() != null ? summoner.getTier() : DEFAULT_TIER;
        int currentLP = summoner.getLp() != null ? summoner.getLp() : MIN_LP;
        return !DEFAULT_TIER.equals(currentTier) && currentLP >= 0;
    }

    private void saveMatchesWithoutLp(List<MatchHistoryDTO> sortedMatches, Map<String, MatchEntity> existingMatches,
            Summoner summoner) {
        for (MatchHistoryDTO matchDTO : sortedMatches) {
            MatchEntity existing = existingMatches.get(matchDTO.getMatchId());
            MatchEntity match = MatchMapper.toEntity(existing, matchDTO, summoner);
            matchService.save(match);
        }
    }

    private void processAndSaveMatchesWithLp(List<MatchHistoryDTO> sortedMatches, List<MatchHistoryDTO> originalMatches,
            Map<String, MatchEntity> existingMatches, Summoner summoner) {
        String currentTier = summoner.getTier() != null ? summoner.getTier() : DEFAULT_TIER;
        String currentDivision = summoner.getRank() != null ? summoner.getRank() : DEFAULT_RANK;
        int currentLP = summoner.getLp() != null ? summoner.getLp() : MIN_LP;

        LpCalculationResult result = buildLpMap(sortedMatches, existingMatches, summoner, currentTier, currentDivision,
                currentLP);

        persistMatchesAndRankHistory(result.newMatches(), result.lpByMatchId(), summoner, currentTier, currentDivision);
        applyCalculatedLp(originalMatches, result.lpByMatchId());
    }

    private record LpCalculationResult(Map<String, Integer> lpByMatchId, List<MatchEntity> newMatches) {
    }

    private LpCalculationResult buildLpMap(List<MatchHistoryDTO> sortedMatches,
            Map<String, MatchEntity> existingMatches, Summoner summoner,
            String currentTier, String currentDivision, int currentLP) {
        Map<String, Integer> lpByMatchId = new HashMap<>();
        List<MatchEntity> newMatches = new ArrayList<>();
        int lpTracker = currentLP;

        for (int i = sortedMatches.size() - 1; i >= 0; i--) {
            MatchHistoryDTO matchDTO = sortedMatches.get(i);
            MatchEntity existing = existingMatches.get(matchDTO.getMatchId());
            Optional<Integer> existingLp = existing != null ? rankHistoryService.getLpForMatch(existing.getId())
                    : Optional.empty();

            if (existingLp.isPresent()) {
                lpTracker = existingLp.get();
                lpByMatchId.put(matchDTO.getMatchId(), existingLp.get());
                continue;
            }

            int lpAtMatchStart = lpTracker;
            boolean won = matchDTO.getWin() != null && matchDTO.getWin();
            lpTracker = calculateBackwardsLpChange(lpTracker, won, currentTier, currentDivision);
            lpByMatchId.put(matchDTO.getMatchId(), lpAtMatchStart);
            newMatches.add(MatchMapper.toEntity(existing, matchDTO, summoner));
        }
        return new LpCalculationResult(lpByMatchId, newMatches);
    }

    private void persistMatchesAndRankHistory(List<MatchEntity> newMatches, Map<String, Integer> lpByMatchId,
            Summoner summoner, String currentTier, String currentDivision) {
        if (newMatches.isEmpty()) {
            return;
        }
        List<MatchEntity> savedMatches = matchService.saveAll(newMatches);
        for (MatchEntity match : savedMatches) {
            Integer lp = lpByMatchId.get(match.getMatchId());
            if (lp != null) {
                rankHistoryService.recordRankSnapshot(summoner, match, currentTier, currentDivision, lp);
            }
        }
        logger.info("Saved {} matches with RankHistory tracking", savedMatches.size());
    }

    private void applyCalculatedLp(List<MatchHistoryDTO> originalMatches, Map<String, Integer> lpByMatchId) {
        for (MatchHistoryDTO matchDTO : originalMatches) {
            Integer calculatedLP = lpByMatchId.get(matchDTO.getMatchId());
            if (calculatedLP != null) {
                matchDTO.setLpAtMatch(calculatedLP);
            }
        }
    }

    /**
     * Calculate LP change going backwards in time
     */
    private int calculateBackwardsLpChange(int lpTracker, boolean won, String currentTier, String divisionTracker) {
        int lpChange = won ? -20 : +15;
        int newLpTracker = lpTracker + lpChange;

        while (newLpTracker < 0 && canDemote(currentTier, divisionTracker)) {
            divisionTracker = demoteDivision(divisionTracker);
            newLpTracker += 100;
        }

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
