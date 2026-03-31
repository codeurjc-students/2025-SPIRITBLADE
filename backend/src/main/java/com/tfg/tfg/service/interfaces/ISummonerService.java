package com.tfg.tfg.service.interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tfg.tfg.model.entity.Summoner;

public interface ISummonerService {

    Page<Summoner> findAll(Pageable pageable);

    Optional<Summoner> findById(Long id);

    Optional<Summoner> findByName(String name);

    List<Summoner> findRecentSearches();
}
