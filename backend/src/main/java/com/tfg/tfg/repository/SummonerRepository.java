package com.tfg.tfg.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.tfg.model.entity.Summoner;

@Repository
public interface SummonerRepository extends JpaRepository<Summoner, Long> {
    /**
     *  Find summoner by exact name
     */
    Optional<Summoner> findByName(String name);

    /**
     *  Find summoner by exact puuid
     */
    Optional<Summoner> findByPuuid(String puuid);

    /**
     *  Find top 10 summoners ordered by last searched date descending
     */
    List<Summoner> findTop10ByOrderByLastSearchedAtDesc();
}
