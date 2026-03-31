package com.tfg.tfg.service.interfaces;

import java.util.List;

import com.tfg.tfg.model.dto.MatchDetailDTO;
import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;

public interface IRiotService {

    IDataDragonService getDataDragonService();

    SummonerDTO getSummonerByName(String riotId);

    List<RiotChampionMasteryDTO> getTopChampionMasteries(String puuid, int count);

    List<MatchHistoryDTO> getMatchHistory(String puuid, int start, int count);

    MatchDetailDTO getMatchDetails(String matchId);
}
