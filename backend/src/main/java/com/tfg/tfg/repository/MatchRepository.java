package com.tfg.tfg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.tfg.model.entity.MatchEntity;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

}
