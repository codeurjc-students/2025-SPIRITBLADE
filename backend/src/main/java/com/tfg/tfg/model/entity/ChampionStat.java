package com.tfg.tfg.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity(name = "CHAMPION_STATS")
public class ChampionStat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer championId;
    private int gamesPlayed;
    private int wins;
    private int kills;
    private int deaths;
    private int assists;

    @ManyToOne(fetch = FetchType.LAZY)
    private Summoner summoner;

    public ChampionStat() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getChampionId() {
        return championId;
    }

    public void setChampionId(Integer championId) {
        this.championId = championId;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public Summoner getSummoner() {
        return summoner;
    }

    public void setSummoner(Summoner summoner) {
        this.summoner = summoner;
    }
}
