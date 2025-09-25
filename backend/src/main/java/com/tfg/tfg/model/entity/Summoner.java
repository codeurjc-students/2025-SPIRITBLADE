package com.tfg.tfg.model.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity(name = "SUMMONERS")
public class Summoner {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String riotId;
    private String name;
    private Integer level;
    private Integer profileIconId;
    private String tier;
    private String rank;
    private Integer lp;

    @OneToMany(mappedBy = "summoner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MatchEntity> matches = new ArrayList<>();

    @OneToMany(mappedBy = "summoner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChampionStat> championStats = new ArrayList<>();

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

    public List<MatchEntity> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchEntity> matches) {
        this.matches = matches;
    }

    public List<ChampionStat> getChampionStats() {
        return championStats;
    }

    public void setChampionStats(List<ChampionStat> championStats) {
        this.championStats = championStats;
    }
}
