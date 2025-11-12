package com.tfg.tfg.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.MatchRepository;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final RankHistoryService rankHistoryService;

    public MatchService(MatchRepository matchRepository, RankHistoryService rankHistoryService) {
        this.matchRepository = matchRepository;
        this.rankHistoryService = rankHistoryService;
    }

    public List<MatchEntity> findBySummonerOrderByTimestampDesc(Summoner summoner) {
        return matchRepository.findBySummonerOrderByTimestampDesc(summoner);
    }

    public List<MatchEntity> findRankedMatchesBySummoner(Summoner summoner, String queueType) {
        return matchRepository.findRankedMatchesBySummoner(summoner, queueType);
    }

    public List<MatchEntity> findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(Summoner summoner, Integer queueId) {
        return matchRepository.findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(summoner, queueId);
    }

    public List<MatchEntity> findRankedMatchesBySummonerOrderByTimestampDesc(Summoner summoner) {
        return matchRepository.findRankedMatchesBySummonerOrderByTimestampDesc(summoner);
    }

    public List<MatchEntity> findRecentMatches(Summoner summoner, LocalDateTime since) {
        // Find matches with RankHistory entries (ranked matches with LP tracking)
        List<MatchEntity> allMatches = matchRepository.findBySummonerOrderByTimestampDesc(summoner);
        
        return allMatches.stream()
                .filter(m -> m.getTimestamp() != null && m.getTimestamp().isAfter(since))
                .filter(m -> rankHistoryService.getLpForMatch(m.getId()).isPresent())
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp())) // Oldest first
                .toList();
    }

    public List<MatchEntity> findRecentMatchesForRoleAnalysis(Summoner summoner, int limit) {
        return matchRepository.findBySummonerOrderByTimestampDesc(summoner)
                .stream()
                .limit(limit)
                .filter(m -> m.getLane() != null && !m.getLane().isEmpty())
                .toList();
    }

    public Map<String, MatchEntity> findExistingMatchesByMatchIds(List<String> matchIds) {
        return matchRepository.findByMatchIdIn(matchIds)
                .stream()
                .collect(Collectors.toMap(MatchEntity::getMatchId, m -> m));
    }

    public MatchEntity save(MatchEntity match) {
        return matchRepository.save(match);
    }

    public List<MatchEntity> saveAll(List<MatchEntity> matches) {
        return matchRepository.saveAll(matches);
    }

    /**
     * Saves a match and records its rank snapshot.
     * Use this when you have rank data available at match save time.
     * 
     * @param match The match entity
     * @param tier Tier at match time
     * @param rank Rank at match time  
     * @param leaguePoints LP at match time
     * @return Saved match entity
     */
    public MatchEntity saveWithRankSnapshot(MatchEntity match, String tier, String rank, Integer leaguePoints) {
        MatchEntity saved = matchRepository.save(match);
        
        if (tier != null && saved.getSummoner() != null) {
            rankHistoryService.recordRankSnapshot(saved.getSummoner(), saved, tier, rank, leaguePoints);
        }
        
        return saved;
    }

    /**
     * Saves matches and records their rank snapshots.
     * 
     * @param matches List of match entities
     * @param tier Tier at matches time
     * @param rank Rank at matches time
     * @param leaguePointsMap Map of matchId to LP at match time
     * @return Saved match entities
     */
    public List<MatchEntity> saveAllWithRankSnapshots(List<MatchEntity> matches, String tier, String rank, Map<String, Integer> leaguePointsMap) {
        List<MatchEntity> saved = matchRepository.saveAll(matches);
        
        saved.stream()
                .filter(m -> m.getSummoner() != null)
                .forEach(m -> {
                    Integer lp = leaguePointsMap.get(m.getMatchId());
                    if (lp != null) {
                        rankHistoryService.recordRankSnapshot(m.getSummoner(), m, tier, rank, lp);
                    }
                });
        
        return saved;
    }
}
