package com.tfg.tfg.mapper;

import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.model.dto.UserDTO;

/**
 * Mapper helper to convert between UserModel entity and UserDTO
 * Centralizes mapping logic to eliminate duplication across controllers.
 */
public final class UserMapper {

    private UserMapper() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts UserModel entity to UserDTO
     * @param user The UserModel entity to convert
     * @return UserDTO with mapped fields, or null if input is null
     */
    public static UserDTO toDTO(UserModel user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setImage(user.getImage());
        dto.setRoles(user.getRols());
        dto.setActive(user.isActive());
        dto.setAvatarUrl(user.getAvatarUrl());
        // Note: password is intentionally NOT mapped for security
        return dto;
    }

    /**
     * Converts UserDTO to UserModel entity (for creation/update operations)
     * @param dto The UserDTO to convert
     * @return UserModel entity with mapped fields, or null if input is null
     */
    public static UserModel toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        
        UserModel user = new UserModel();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setImage(dto.getImage());
        user.setRols(dto.getRoles());
        user.setActive(dto.isActive());
        user.setAvatarUrl(dto.getAvatarUrl());
        // Note: ID and password are managed separately
        return user;
    }
}
