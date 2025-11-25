package com.tfg.tfg.repository;

import com.tfg.tfg.model.entity.Champion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChampionRepository extends JpaRepository<Champion, Long> {
    Optional<Champion> findByKey(String key);
}
