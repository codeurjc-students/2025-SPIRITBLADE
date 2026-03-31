package com.tfg.tfg.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.tfg.tfg.model.dto.RankHistoryDTO;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.RankHistory;
import com.tfg.tfg.model.entity.Summoner;

public interface IRankHistoryService {

    RankHistory recordRankSnapshot(Summoner summoner, MatchEntity match,
            String tier, String rank, Integer leaguePoints);

    Optional<RankHistory> getRankForMatch(Long matchId);

    Optional<Integer> getLpForMatch(Long matchId);

    List<RankHistoryDTO> getRankProgression(Long summonerId, String queueType);

    Optional<RankHistoryDTO> getPeakRank(Summoner summoner, String queueType);
}
