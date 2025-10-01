package com.tfg.tfg.service;

import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar operaciones relacionadas con Summoners.
 */
@Service
public class SummonerService {

    private final SummonerRepository summonerRepository;

    public SummonerService(SummonerRepository summonerRepository) {
        this.summonerRepository = summonerRepository;
    }

    /**
     * Busca un summoner por nombre.
     */
    public Optional<Summoner> findByName(String name) {
        return summonerRepository.findByName(name);
    }

    /**
     * Busca un summoner por ID.
     */
    public Optional<Summoner> findById(Long id) {
        return summonerRepository.findById(id);
    }

    /**
     * Obtiene todos los summoners.
     */
    public List<Summoner> findAll() {
        return summonerRepository.findAll();
    }

    /**
     * Guarda un summoner.
     */
    public Summoner save(Summoner summoner) {
        return summonerRepository.save(summoner);
    }

    /**
     * Elimina un summoner por ID.
     */
    public void deleteById(Long id) {
        summonerRepository.deleteById(id);
    }
}