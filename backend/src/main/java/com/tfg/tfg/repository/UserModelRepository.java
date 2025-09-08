package com.tfg.tfg.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.tfg.model.entity.UserModel;

@Repository
public interface UserModelRepository extends JpaRepository<UserModel, Long> {
    
    Optional<UserModel> findByName(String name);
    Optional<UserModel> findByContact(String email);
}
   
