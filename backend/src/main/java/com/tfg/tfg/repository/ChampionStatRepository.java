package com.tfg.tfg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.tfg.model.entity.ChampionStat;

@Repository
public interface ChampionStatRepository extends JpaRepository<ChampionStat, Long> {

}
