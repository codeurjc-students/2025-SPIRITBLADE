package com.tfg.tfg.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.MatchRepository;
import com.tfg.tfg.service.interfaces.IMatchService;
import com.tfg.tfg.service.interfaces.IRankHistoryService;

@Service
public class MatchService implements IMatchService {

    private final MatchRepository matchRepository;
    private final IRankHistoryService rankHistoryService;

    public MatchService(MatchRepository matchRepository, IRankHistoryService rankHistoryService) {
        this.matchRepository = matchRepository;
        this.rankHistoryService = rankHistoryService;
    }

    public List<MatchEntity> findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(Summoner summoner,
            Integer queueId) {
        return matchRepository.findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(summoner, queueId);
    }

    public List<MatchEntity> findRankedMatchesBySummonerOrderByTimestampDesc(Summoner summoner) {
        return matchRepository.findRankedMatchesBySummonerOrderByTimestampDesc(summoner);
    }

    public List<MatchEntity> findRecentMatches(Summoner summoner, LocalDateTime since) {
        List<MatchEntity> allMatches = matchRepository.findBySummonerOrderByTimestampDesc(summoner);

        return allMatches.stream()
                .filter(m -> m.getTimestamp() != null && m.getTimestamp().isAfter(since))
                .filter(m -> rankHistoryService.getLpForMatch(m.getId()).isPresent())
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .toList();
    }

    public List<MatchEntity> findRecentMatchesForRoleAnalysis(Summoner summoner, int limit) {
        return matchRepository.findBySummonerOrderByTimestampDesc(summoner)
                .stream()
                .limit(limit)
                .filter(m -> m.getLane() != null && !m.getLane().isEmpty())
                .toList();
    }

    public Map<String, MatchEntity> findExistingMatchesByMatchIdsAndSummoner(List<String> matchIds, Summoner summoner) {
        return matchRepository.findByMatchIdInAndSummoner(matchIds, summoner)
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
