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

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
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
        return matchRepository.findBySummonerOrderByTimestampDesc(summoner)
                .stream()
                .filter(m -> m.getTimestamp() != null && m.getTimestamp().isAfter(since))
                .filter(m -> m.getLpAtMatch() != null)
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
}
