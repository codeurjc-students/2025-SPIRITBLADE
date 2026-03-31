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
    private Long id;

    @Column(name = "champion_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

}
