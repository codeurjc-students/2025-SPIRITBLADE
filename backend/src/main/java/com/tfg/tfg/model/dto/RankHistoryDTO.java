package com.tfg.tfg.model.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for rank history data transfer.
 * Enhanced version with calculated fields and metadata.
 */
@Getter
@Setter
public class RankHistoryDTO {
    private Long id;
    private String summonerName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String date;
    @Setter(AccessLevel.NONE)
    private String tier;
    @Setter(AccessLevel.NONE)
    private String rank;
    private Integer leaguePoints;
    @Setter(AccessLevel.NONE)
    private Integer wins;
    @Setter(AccessLevel.NONE)
    private Integer losses;
    private String queueType;
    private Integer lpChange;
    
    private String formattedRank;
    private Double winRate;
    private Integer totalGames;

    public RankHistoryDTO() {
    }

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

    public void setTier(String tier) {
        this.tier = tier;
        this.formattedRank = formatRank(tier, this.rank);
    }

    public void setRank(String rank) {
        this.rank = rank;
        this.formattedRank = formatRank(this.tier, rank);
    }

    public void setWins(Integer wins) {
        this.wins = wins;
        recalculateStats();
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
        recalculateStats();
    }

    @Override
    public String toString() {
        return String.format("RankHistoryDTO{id=%d, rank=%s, lp=%d, lpChange=%s, timestamp=%s}",
                id, formattedRank, leaguePoints, lpChange != null ? lpChange : "N/A", timestamp);
    }
}
