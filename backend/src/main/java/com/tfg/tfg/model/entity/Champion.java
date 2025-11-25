package com.tfg.tfg.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "champions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Champion {

    @Id
    private Long id; // Riot's numeric Champion ID (e.g., 266 for Aatrox)

    @Column(name = "champion_key", nullable = false)
    private String key; // Riot's string key (e.g., "Aatrox")

    @Column(nullable = false)
    private String name; // Display name (e.g., "Aatrox")

    @Column(name = "image_url")
    private String imageUrl; // Full URL to the champion's icon

}
