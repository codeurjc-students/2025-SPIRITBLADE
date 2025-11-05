package com.tfg.tfg.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "summoners", indexes = {
    @Index(name = "idx_summoner_name", columnList = "name"),
    @Index(name = "idx_summoner_puuid", columnList = "puuid"),
    @Index(name = "idx_summoner_last_searched", columnList = "lastSearchedAt")
})
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

    public Summoner() {
    }

    public Summoner(String riotId, String name, Integer level) {
        this.riotId = riotId;
        this.name = name;
        this.level = level;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRiotId() {
        return riotId;
    }

    public void setRiotId(String riotId) {
        this.riotId = riotId;
    }

    public String getPuuid() {
        return puuid;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getProfileIconId() {
        return profileIconId;
    }

    public void setProfileIconId(Integer profileIconId) {
        this.profileIconId = profileIconId;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Integer getLp() {
        return lp;
    }

    public void setLp(Integer lp) {
        this.lp = lp;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public Integer getLosses() {
        return losses;
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
    }

    public LocalDateTime getLastSearchedAt() {
        return lastSearchedAt;
    }

    public void setLastSearchedAt(LocalDateTime lastSearchedAt) {
        this.lastSearchedAt = lastSearchedAt;
    }
}
