package com.tfg.tfg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.service.SummonerDto;

@RestController
@RequestMapping("/api/v1/summoners")
public class SummonerController {

    @Autowired
    private RiotService riotService;

    @GetMapping("/name/{name}")
    public SummonerDTO getSummoner(@PathVariable String name) {
        SummonerDto s = riotService.getSummonerByName(name);
        SummonerDTO dto = new SummonerDTO();
        dto.riotId = s.getId();
        dto.name = s.getName();
        dto.level = s.getLevel();
        dto.tier = "Unranked";
        dto.rank = "-";
        dto.lp = 0;
        return dto;
    }
}
