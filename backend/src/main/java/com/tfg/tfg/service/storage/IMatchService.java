package com.tfg.tfg.service.storage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;

public interface IMatchService {

    List<MatchEntity> findRankedMatchesBySummonerAndQueueIdOrderByTimestampDesc(Summoner summoner, Integer queueId);

    List<MatchEntity> findRankedMatchesBySummonerOrderByTimestampDesc(Summoner summoner);

    List<MatchEntity> findRecentMatches(Summoner summoner, LocalDateTime since);

    List<MatchEntity> findRecentMatchesForRoleAnalysis(Summoner summoner, int limit);

    Map<String, MatchEntity> findExistingMatchesByMatchIds(List<String> matchIds);

    MatchEntity save(MatchEntity match);

    List<MatchEntity> saveAll(List<MatchEntity> matches);
}
