package com.tfg.tfg.service.storage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tfg.tfg.exception.UserNotFoundException;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;

public interface IUserService {

    Optional<UserModel> findByName(String username);

    UserModel getUserById(Long id);

    UserModel getUserByName(String username);

    Optional<UserModel> findFirstUser();

    Page<UserModel> findAll(Pageable pageable);

    Page<UserModel> findBySearch(String search, Pageable pageable);

    Page<UserModel> findByRoleAndActive(String role, Boolean active, Pageable pageable);

    Page<UserModel> findByRole(String role, Pageable pageable);

    Page<UserModel> findByActive(Boolean active, Pageable pageable);

    UserModel createUser(UserDTO userDTO);

    UserModel updateUserOrThrow(Long id, UserDTO userDTO);

    Optional<UserModel> updateUserProfile(String username, UserDTO userDTO);

    Optional<UserModel> changeUserRole(Long id, List<String> roles);

    void deleteUserOrThrow(Long id) throws UserNotFoundException;

    Optional<UserModel> toggleUserActive(Long id);

    UserModel setUserActiveOrThrow(Long id, boolean active);

    Optional<UserModel> linkSummoner(String username, String puuid, String summonerName, String region);

    Optional<UserModel> unlinkSummoner(String username);

    List<UserModel> findAllUsers();

    long countUsers();

    UserModel save(UserModel user);
}
