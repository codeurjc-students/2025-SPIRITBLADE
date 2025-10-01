package com.tfg.tfg.controller;

import java.util.List;

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

    private final SummonerRepository summonerRepository;

    public SummonerController(SummonerRepository summonerRepository) {
        this.summonerRepository = summonerRepository;
    }

    @GetMapping
    public ResponseEntity<List<SummonerDTO>> getAllSummoners() {
        List<Summoner> summoners = summonerRepository.findAll();
        List<SummonerDTO> dtos = summoners.stream().map(this::mapSummoner).toList();
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
            List<MatchDTO> list = s.getMatches().stream().map(this::mapMatch).toList();
            return ResponseEntity.ok(list);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/champion-stats")
    public ResponseEntity<List<ChampionStatDTO>> getChampionStats(@PathVariable Long id) {
        return summonerRepository.findById(id).map(s -> {
            List<ChampionStatDTO> list = s.getChampionStats().stream().map(this::mapChampionStat).toList();
            return ResponseEntity.ok(list);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    private SummonerDTO mapSummoner(Summoner s) {
        SummonerDTO dto = new SummonerDTO();
        dto.setId(s.getId());
        dto.setRiotId(s.getRiotId());
        dto.setName(s.getName());
        dto.setLevel(s.getLevel());
        dto.setProfileIconId(s.getProfileIconId());
        dto.setTier(s.getTier());
        dto.setRank(s.getRank());
        dto.setLp(s.getLp());
        return dto;
    }

    private MatchDTO mapMatch(MatchEntity m) {
        MatchDTO dto = new MatchDTO();
        dto.setId(m.getId());
        dto.setMatchId(m.getMatchId());
        dto.setTimestamp(m.getTimestamp());
        dto.setWin(m.isWin());
        dto.setKills(m.getKills());
        dto.setDeaths(m.getDeaths());
        dto.setAssists(m.getAssists());
        return dto;
    }

    private ChampionStatDTO mapChampionStat(ChampionStat c) {
        ChampionStatDTO dto = new ChampionStatDTO();
        dto.setId(c.getId());
        dto.setChampionId(c.getChampionId());
        dto.setGamesPlayed(c.getGamesPlayed());
        dto.setWins(c.getWins());
        dto.setKills(c.getKills());
        dto.setDeaths(c.getDeaths());
        dto.setAssists(c.getAssists());
        return dto;
    }
}
