package com.tfg.tfg.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.model.dto.SummonerDTO;

@RestController
@RequestMapping("/api/summoners")
public class SummonerController {

    @GetMapping("/name/{name}")
    public SummonerDTO getSummoner(@PathVariable String name) {
        SummonerDTO dto = new SummonerDTO();
        dto.name = name;
        dto.level = 142;
        dto.tier = "Gold II";
        dto.lp = 1247;
        return dto;
    }
}
