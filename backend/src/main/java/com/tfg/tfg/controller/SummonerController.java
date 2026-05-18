package com.tfg.tfg.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.MatchDetailDTO;
import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.mapper.SummonerMapper;
import com.tfg.tfg.service.interfaces.IDataDragonService;
import com.tfg.tfg.service.interfaces.IRiotService;
import com.tfg.tfg.service.interfaces.ISummonerService;

@RestController
@RequestMapping("/api/v1/summoners")
public class SummonerController {

    private final ISummonerService summonerService;
    private final IRiotService riotService;

    public SummonerController(ISummonerService summonerService, IRiotService riotService) {
        this.summonerService = summonerService;
        this.riotService = riotService;
    }

    @GetMapping("/recent")
    public ResponseEntity<List<SummonerDTO>> getRecentSearches() {
        List<Summoner> recentSummoners = summonerService.findRecentSearches();
        IDataDragonService dataDragonService = riotService.getDataDragonService();
        List<SummonerDTO> dtos = recentSummoners.stream()
                .filter(s -> s.getLastSearchedAt() != null)
                .map(s -> SummonerMapper.toDTO(s, dataDragonService))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<SummonerDTO> getSummoner(@PathVariable String name) {
        SummonerDTO dto = riotService.getSummonerByName(name);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/name/{name}/masteries")
    public ResponseEntity<List<RiotChampionMasteryDTO>> getTopChampions(@PathVariable String name) {
        SummonerDTO summoner = riotService.getSummonerByName(name);

        List<RiotChampionMasteryDTO> masteries = riotService.getTopChampionMasteries(summoner.getPuuid(), 3);
        return ResponseEntity.ok(masteries);
    }

    @GetMapping("/name/{name}/matches")
    public ResponseEntity<List<MatchHistoryDTO>> getRecentMatches(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        SummonerDTO summoner = riotService.getSummonerByName(name);

        int start = page * size;

        List<MatchHistoryDTO> matches = riotService.getMatchHistory(summoner.getPuuid(), start, size);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/matches/{matchId}")
    public ResponseEntity<MatchDetailDTO> getMatchDetails(@PathVariable String matchId) {
        MatchDetailDTO matchDetails = riotService.getMatchDetails(matchId);
        if (matchDetails == null) {
            throw new com.tfg.tfg.exception.MatchNotFoundException("Match details not found for ID: " + matchId);
        }
        return ResponseEntity.ok(matchDetails);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SummonerDTO> getById(@PathVariable Long id) {
        return summonerService.findById(id)
                .map(s -> ResponseEntity.ok(SummonerMapper.toDTO(s, riotService.getDataDragonService())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
