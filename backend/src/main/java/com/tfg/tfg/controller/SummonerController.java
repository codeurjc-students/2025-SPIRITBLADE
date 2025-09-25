package com.tfg.tfg.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.model.dto.ChampionStatDTO;
import com.tfg.tfg.model.dto.MatchDTO;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.entity.ChampionStat;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;

@RestController
@RequestMapping("/api/v1/summoners")
public class SummonerController {

    @Autowired
    private SummonerRepository summonerRepository;

    @GetMapping
    public ResponseEntity<List<SummonerDTO>> getAllSummoners() {
        List<Summoner> summoners = summonerRepository.findAll();
        List<SummonerDTO> dtos = summoners.stream().map(this::mapSummoner).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<SummonerDTO> getSummoner(@PathVariable String name) {
        return summonerRepository.findByName(name).map(s -> {
            SummonerDTO dto = mapSummoner(s);
            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SummonerDTO> getById(@PathVariable Long id) {
        return summonerRepository.findById(id).map(s -> ResponseEntity.ok(mapSummoner(s))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<List<MatchDTO>> getMatches(@PathVariable Long id) {
        return summonerRepository.findById(id).map(s -> {
            List<MatchDTO> list = s.getMatches().stream().map(this::mapMatch).collect(Collectors.toList());
            return ResponseEntity.ok(list);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/champion-stats")
    public ResponseEntity<List<ChampionStatDTO>> getChampionStats(@PathVariable Long id) {
        return summonerRepository.findById(id).map(s -> {
            List<ChampionStatDTO> list = s.getChampionStats().stream().map(this::mapChampionStat).collect(Collectors.toList());
            return ResponseEntity.ok(list);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    private SummonerDTO mapSummoner(Summoner s) {
        SummonerDTO dto = new SummonerDTO();
        dto.id = s.getId();
        dto.riotId = s.getRiotId();
        dto.name = s.getName();
        dto.level = s.getLevel();
        dto.profileIconId = s.getProfileIconId();
        dto.tier = s.getTier();
        dto.rank = s.getRank();
        dto.lp = s.getLp();
        return dto;
    }

    private MatchDTO mapMatch(MatchEntity m) {
        MatchDTO dto = new MatchDTO();
        dto.id = m.getId();
        dto.matchId = m.getMatchId();
        dto.timestamp = m.getTimestamp();
        dto.win = m.isWin();
        dto.kills = m.getKills();
        dto.deaths = m.getDeaths();
        dto.assists = m.getAssists();
        return dto;
    }

    private ChampionStatDTO mapChampionStat(ChampionStat c) {
        ChampionStatDTO dto = new ChampionStatDTO();
        dto.id = c.getId();
        dto.championId = c.getChampionId();
        dto.gamesPlayed = c.getGamesPlayed();
        dto.wins = c.getWins();
        dto.kills = c.getKills();
        dto.deaths = c.getDeaths();
        dto.assists = c.getAssists();
        return dto;
    }
}
