package com.tfg.tfg.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.tfg.model.entity.UserModel;

@Repository
public interface UserModelRepository extends JpaRepository<UserModel, Long> {
    
    Optional<UserModel> findByName(String name);
    Optional<UserModel> findByEmail(String email);
    
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
   
