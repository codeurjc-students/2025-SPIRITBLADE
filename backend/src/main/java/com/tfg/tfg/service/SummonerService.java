package com.tfg.tfg.service;

import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Summoner operations.
 * Acts as intermediary between controllers and repository.
 */
@Service
@Transactional
public class SummonerService {

    private final SummonerRepository summonerRepository;

    public SummonerService(SummonerRepository summonerRepository) {
        this.summonerRepository = summonerRepository;
    }

    /**
     * Find all summoners with pagination
     */
    public Page<Summoner> findAll(Pageable pageable) {
        return summonerRepository.findAll(pageable);
    }

    /**
     * Find summoner by ID
     */
    public Optional<Summoner> findById(Long id) {
        return summonerRepository.findById(id);
    }

    /**
     * Find summoner by name (case insensitive)
     */
    public Optional<Summoner> findByNameIgnoreCase(String name) {
        return summonerRepository.findByNameIgnoreCase(name);
    }

    /**
     * Find recent searches (last 10 summoners searched)
     */
    public List<Summoner> findRecentSearches() {
        return summonerRepository.findTop10ByOrderByLastSearchedAtDesc();
    }

    /**
     * Save or update summoner
     */
    public Summoner save(Summoner summoner) {
        return summonerRepository.save(summoner);
    }
}
