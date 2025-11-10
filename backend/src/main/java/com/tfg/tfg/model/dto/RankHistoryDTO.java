package com.tfg.tfg.model.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO for rank history data transfer.
 * Enhanced version with calculated fields and metadata.
 */
public class RankHistoryDTO {
    private Long id;
    private String summonerName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String date; // Keep for backward compatibility
    private String tier;
    private String rank;
    private Integer leaguePoints;
    private Integer wins;
    private Integer losses;
    private String queueType;
    private Integer lpChange;
    
    // Calculated fields
    private String formattedRank;
    private Double winRate;
    private Integer totalGames;

    public RankHistoryDTO() {
    }

    // Backward compatible constructor
    public RankHistoryDTO(String date, String tier, String rank, Integer leaguePoints, 
                         Integer wins, Integer losses) {
        this.date = date;
        this.tier = tier;
        this.rank = rank;
        this.leaguePoints = leaguePoints;
        this.wins = wins;
        this.losses = losses;
        this.formattedRank = formatRank(tier, rank);
        recalculateStats();
    }

    // Enhanced constructor
    public RankHistoryDTO(Long id, String summonerName, LocalDateTime timestamp, String tier, 
                          String rank, Integer leaguePoints, Integer wins, Integer losses, 
                          String queueType, Integer lpChange) {
        this.id = id;
        this.summonerName = summonerName;
        this.timestamp = timestamp;
        this.tier = tier;
        this.rank = rank;
        this.leaguePoints = leaguePoints;
        this.wins = wins;
        this.losses = losses;
        this.queueType = queueType;
        this.lpChange = lpChange;
        this.formattedRank = formatRank(tier, rank);
        recalculateStats();
    }

    private String formatRank(String tier, String rank) {
        if (tier == null) return "Unranked";
        if (rank == null || rank.isEmpty()) return tier;
        return tier + " " + rank;
    }

    private void recalculateStats() {
        this.totalGames = (wins != null ? wins : 0) + (losses != null ? losses : 0);
        if (totalGames > 0) {
            this.winRate = ((wins != null ? wins : 0) * 100.0) / totalGames;
        } else {
            this.winRate = 0.0;
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSummonerName() {
        return summonerName;
    }

    public void setSummonerName(String summonerName) {
        this.summonerName = summonerName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
        this.formattedRank = formatRank(tier, this.rank);
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
        this.formattedRank = formatRank(this.tier, rank);
    }

    public Integer getLeaguePoints() {
        return leaguePoints;
    }

    public void setLeaguePoints(Integer leaguePoints) {
        this.leaguePoints = leaguePoints;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
        recalculateStats();
    }

    public Integer getLosses() {
        return losses;
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
        recalculateStats();
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }

    public Integer getLpChange() {
        return lpChange;
    }

    public void setLpChange(Integer lpChange) {
        this.lpChange = lpChange;
    }

    public String getFormattedRank() {
        return formattedRank;
    }

    public void setFormattedRank(String formattedRank) {
        this.formattedRank = formattedRank;
    }

    public Double getWinRate() {
        return winRate;
    }

    public void setWinRate(Double winRate) {
        this.winRate = winRate;
    }

    public Integer getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(Integer totalGames) {
        this.totalGames = totalGames;
    }

    @Override
    public String toString() {
        return String.format("RankHistoryDTO{id=%d, rank=%s, lp=%d, lpChange=%s, timestamp=%s}",
                id, formattedRank, leaguePoints, lpChange != null ? lpChange : "N/A", timestamp);
    }
}
