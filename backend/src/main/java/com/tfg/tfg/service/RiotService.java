package com.tfg.tfg.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RiotService {
    @Value("${riot.api.key}")
    private String apiKey;
    
    public SummonerDto getSummonerByName(String name) {
        // Minimal stub implementation until Riot API is integrated
        return new SummonerDto(name, name, 1);
    }
}