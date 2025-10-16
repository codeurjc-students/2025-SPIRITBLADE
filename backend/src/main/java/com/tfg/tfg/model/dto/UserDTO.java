package com.tfg.tfg.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String image;
    private String email;
    private String password;
    private List<String> roles;
    private boolean active;
    private String avatarUrl;
}
