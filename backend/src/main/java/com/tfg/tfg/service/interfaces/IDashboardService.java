package com.tfg.tfg.service.interfaces;

import java.util.List;
import java.util.Map;

import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.entity.Summoner;

public interface IDashboardService {

    Map<String, Object> getPersonalStats(Summoner summoner);

    String formatRank(Summoner summoner);

    int calculateLPGainedLast7Days(Summoner summoner);

    String calculateMainRole(Summoner summoner);

    String getFavoriteChampion(Summoner summoner);

    Double calculateAverageVisionScore(Summoner summoner);

    String calculateAverageKDA(Summoner summoner);

    List<MatchHistoryDTO> getRankedMatchesWithLP(Summoner summoner, Integer queueId, int page, int size);
}
