package com.tfg.tfg.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "matches", indexes = {
    @Index(name = "idx_match_summoner", columnList = "summoner_id"),
    @Index(name = "idx_match_timestamp", columnList = "timestamp"),
    @Index(name = "idx_match_id", columnList = "matchId")
})
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String matchId;
    private LocalDateTime timestamp;
    private boolean win;
    private int kills;
    private int deaths;
    private int assists;
    
    // Additional match details for caching
    private String championName;
    private Integer championId;
    private String role;
    private String lane;
    private Long gameDuration;
    private String gameMode;
    private Integer totalDamageDealt;
    private Integer goldEarned;
    private Integer champLevel;
    private String summonerName;  // Denormalized for quick access
    
    // Rank information at the time of the match (for LP progression tracking)
    private String tierAtMatch;      // e.g., "GOLD", "SILVER"
    private String rankAtMatch;      // e.g., "I", "II", "III", "IV"
    private Integer lpAtMatch;       // League Points at match time (0-100)
    
    // Timestamps for cache management
    private LocalDateTime cachedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summoner_id")
    private Summoner summoner;

    public MatchEntity() {
        // Default constructor for JPA
    }

    public MatchEntity(String matchId, Summoner summoner) {
        this.matchId = matchId;
        this.summoner = summoner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
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

    public String getChampionName() {
        return championName;
    }

    public void setChampionName(String championName) {
        this.championName = championName;
    }

    public Integer getChampionId() {
        return championId;
    }

    public void setChampionId(Integer championId) {
        this.championId = championId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public Long getGameDuration() {
        return gameDuration;
    }

    public void setGameDuration(Long gameDuration) {
        this.gameDuration = gameDuration;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public Integer getTotalDamageDealt() {
        return totalDamageDealt;
    }

    public void setTotalDamageDealt(Integer totalDamageDealt) {
        this.totalDamageDealt = totalDamageDealt;
    }

    public Integer getGoldEarned() {
        return goldEarned;
    }

    public void setGoldEarned(Integer goldEarned) {
        this.goldEarned = goldEarned;
    }

    public Integer getChampLevel() {
        return champLevel;
    }

    public void setChampLevel(Integer champLevel) {
        this.champLevel = champLevel;
    }

    public String getSummonerName() {
        return summonerName;
    }

    public void setSummonerName(String summonerName) {
        this.summonerName = summonerName;
    }

    public LocalDateTime getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(LocalDateTime cachedAt) {
        this.cachedAt = cachedAt;
    }

    public String getTierAtMatch() {
        return tierAtMatch;
    }

    public void setTierAtMatch(String tierAtMatch) {
        this.tierAtMatch = tierAtMatch;
    }

    public String getRankAtMatch() {
        return rankAtMatch;
    }

    public void setRankAtMatch(String rankAtMatch) {
        this.rankAtMatch = rankAtMatch;
    }

    public Integer getLpAtMatch() {
        return lpAtMatch;
    }

    public void setLpAtMatch(Integer lpAtMatch) {
        this.lpAtMatch = lpAtMatch;
    }
}
