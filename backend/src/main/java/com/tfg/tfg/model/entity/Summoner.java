package com.tfg.tfg.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "summoners", indexes = {
    @Index(name = "idx_summoner_name", columnList = "name"),
    @Index(name = "idx_summoner_puuid", columnList = "puuid"),
    @Index(name = "idx_summoner_last_searched", columnList = "lastSearchedAt")
})
@Getter
@Setter
@NoArgsConstructor
public class Summoner {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String riotId;
    
    @Column(unique = true, nullable = false)
    private String puuid;
    
    @Column(name = "`name`")
    private String name;
    
    @Column(name = "`level`")
    private Integer level;
    
    private Integer profileIconId;
    private String tier;
    
    @Column(name = "`rank`")
    private String rank;
    
    private Integer lp;
    private Integer wins;
    private Integer losses;
    private LocalDateTime lastSearchedAt;

    public Summoner(String riotId, String name, Integer level) {
        this.riotId = riotId;
        this.name = name;
        this.level = level;
    }
}
