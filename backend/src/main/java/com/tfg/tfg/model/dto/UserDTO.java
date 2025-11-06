package com.tfg.tfg.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    private String image;
    private String email;
    private String password;
    private List<String> roles;
    private boolean active;
    private String avatarUrl;
    
    // Alias for 'name' field to support both 'name' and 'username' in JSON
    @JsonProperty("username")
    public String getUsername() {
        return name;
    }
    
    public void setUsername(String username) {
        this.name = username;
    }
}
