package com.tfg.tfg.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.tfg.model.entity.UserModel;

@Repository
public interface UserModelRepository extends JpaRepository<UserModel, Long> {
    
    /**
     * Find user by name. NOTE: throws NonUniqueResultException if duplicates exist.
     * Prefer existsByName() for existence checks, findFirstByName() when resilience is needed.
     */
    Optional<UserModel> findByName(String name);

    /**
     * Safe existence check that does not throw when duplicate rows are present.
     */
    boolean existsByName(String name);

    /**
     * Returns ALL users with the given name – used to detect and clean up duplicates.
     */
    java.util.List<UserModel> findAllByName(String name);
    
    /**
     * Finds the first user ordered by ID (ascending).
     * This is more efficient than findAll().stream().findFirst()
     */
    Optional<UserModel> findFirstByOrderByIdAsc();
    
    // Pagination and filtering methods
    
    /**
     * Find users by role with pagination.
     */
    Page<UserModel> findByRolsContaining(String role, Pageable pageable);
    
    /**
     * Find users by active status with pagination.
     */
    Page<UserModel> findByActive(boolean active, Pageable pageable);
    
    /**
     * Find users by role and active status with pagination.
     */
    Page<UserModel> findByRolsContainingAndActive(String role, boolean active, Pageable pageable);
    
    /**
     * Search users by name or email (case insensitive) with pagination.
     */
    Page<UserModel> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String name, String email, Pageable pageable);
}
   
