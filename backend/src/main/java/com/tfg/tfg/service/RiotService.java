package com.tfg.tfg.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.tfg.tfg.model.dto.SummonerSimpleDto;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;

import java.util.Optional;

@Service
public class RiotService {
    @Value("${riot.api.key}")
    private String apiKey;
    
    @Autowired
    private SummonerRepository summonerRepository;

    public SummonerSimpleDto getSummonerByName(String name) {
        // Try to read from DB first (this allows DatabaseInitializer data to be used)
        Optional<Summoner> found = summonerRepository.findByName(name);
        if (found.isPresent()) {
            Summoner s = found.get();
            return new SummonerSimpleDto(s.getRiotId(), s.getName(), s.getLevel());
        }

        // Fallback stub implementation until Riot API is integrated
        return new SummonerSimpleDto(name, name, 1);
    }
}