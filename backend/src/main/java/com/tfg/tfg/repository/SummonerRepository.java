package com.tfg.tfg.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.tfg.model.entity.Summoner;

@Repository
public interface SummonerRepository extends JpaRepository<Summoner, Long> {
    Optional<Summoner> findByName(String name);
}
