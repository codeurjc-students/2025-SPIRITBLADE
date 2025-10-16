package com.tfg.tfg.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tfg.tfg.model.entity.Summoner;

@Repository
public interface SummonerRepository extends JpaRepository<Summoner, Long> {
    Optional<Summoner> findByName(String name);
    List<Summoner> findTop10ByOrderByLastSearchedAtDesc();
    
    /**
     * Finds the first summoner ordered by ID (ascending).
     * This is more efficient than findAll().stream().findFirst()
     */
    Optional<Summoner> findFirstByOrderByIdAsc();
    
    /**
     * Finds a summoner linked to a user by matching their names (case-insensitive).
     * This uses a JOIN between Summoner and UserModel tables.
     * 
     * @param username The username to search for
     * @return Optional containing the linked Summoner if found
     */
    @Query("SELECT s FROM Summoner s WHERE LOWER(s.name) = LOWER(:username)")
    Optional<Summoner> findLinkedSummonerByUsername(@Param("username") String username);
    
    /**
     * Finds top N summoners excluding a specific ID, ordered by last searched date.
     * This is optimized for getting favorite summoners list.
     * 
     * @param excludeId The ID to exclude (usually the current user's summoner)
     * @param limit Maximum number of results to return
     * @return List of summoners excluding the specified ID
     */
    @Query("SELECT s FROM Summoner s WHERE s.id <> :excludeId ORDER BY s.lastSearchedAt DESC")
    List<Summoner> findTopByIdNotOrderByLastSearchedAtDesc(@Param("excludeId") Long excludeId, 
                                                            org.springframework.data.domain.Pageable pageable);
}
