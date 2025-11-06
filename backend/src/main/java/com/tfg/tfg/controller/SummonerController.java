package com.tfg.tfg.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.mapper.SummonerMapper;
import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.service.DataDragonService;

@RestController
@RequestMapping("/api/v1/summoners")
public class SummonerController {

    private final SummonerRepository summonerRepository;
    private final RiotService riotService;

    public SummonerController(SummonerRepository summonerRepository, RiotService riotService) {
        this.summonerRepository = summonerRepository;
        this.riotService = riotService;
    }

    @GetMapping
    public ResponseEntity<Page<SummonerDTO>> getAllSummoners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageRequest pageable = PageRequest.of(page, size, Sort.by("lastSearchedAt").descending());
        Page<Summoner> summonersPage = summonerRepository.findAll(pageable);
        
        DataDragonService dataDragonService = riotService.getDataDragonService();
        Page<SummonerDTO> dtos = summonersPage.map(s -> SummonerMapper.toDTO(s, dataDragonService));
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<SummonerDTO>> getRecentSearches() {
        List<Summoner> recentSummoners = summonerRepository.findTop10ByOrderByLastSearchedAtDesc();
        DataDragonService dataDragonService = riotService.getDataDragonService();
        List<SummonerDTO> dtos = recentSummoners.stream()
            .filter(s -> s.getLastSearchedAt() != null)
            .map(s -> SummonerMapper.toDTO(s, dataDragonService))
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<SummonerDTO> getSummoner(@PathVariable String name) {
        // Use Riot API service to get live data
        SummonerDTO dto = riotService.getSummonerByName(name);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/name/{name}/masteries")
    public ResponseEntity<List<RiotChampionMasteryDTO>> getTopChampions(@PathVariable String name) {
        // First get summoner to obtain PUUID
        SummonerDTO summoner = riotService.getSummonerByName(name);
        if (summoner == null || summoner.getPuuid() == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Get champion masteries using PUUID (with champion names enriched)
        List<RiotChampionMasteryDTO> masteries = riotService.getTopChampionMasteries(summoner.getPuuid(), 3);
        return ResponseEntity.ok(masteries);
    }
    
    @GetMapping("/name/{name}/matches")
    public ResponseEntity<List<MatchHistoryDTO>> getRecentMatches(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        // First get summoner to obtain PUUID
        SummonerDTO summoner = riotService.getSummonerByName(name);
        if (summoner == null || summoner.getPuuid() == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Calculate start index for pagination
        int start = page * size;
        
        // Get match history using PUUID with pagination
        List<MatchHistoryDTO> matches = riotService.getMatchHistory(summoner.getPuuid(), start, size);
        return ResponseEntity.ok(matches);
    }
    
    @GetMapping("/matches/{matchId}")
    public ResponseEntity<com.tfg.tfg.model.dto.MatchDetailDTO> getMatchDetails(@PathVariable String matchId) {
        com.tfg.tfg.model.dto.MatchDetailDTO matchDetails = riotService.getMatchDetails(matchId);
        if (matchDetails != null) {
            return ResponseEntity.ok(matchDetails);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SummonerDTO> getById(@PathVariable Long id) {
        return summonerRepository.findById(id)
            .map(s -> ResponseEntity.ok(SummonerMapper.toDTO(s, riotService.getDataDragonService())))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
