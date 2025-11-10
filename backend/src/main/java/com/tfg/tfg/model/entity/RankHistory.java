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

/**
 * Entity representing a snapshot of a summoner's rank at a specific point in time.
 * This allows tracking rank progression over time and analyzing LP gains/losses.
 */
@Entity
@Table(name = "rank_history", indexes = {
    @Index(name = "idx_rank_summoner", columnList = "summoner_id"),
    @Index(name = "idx_rank_timestamp", columnList = "timestamp"),
    @Index(name = "idx_rank_summoner_queue", columnList = "summoner_id, queueType")
})
public class RankHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summoner_id", nullable = false)
    private Summoner summoner;

    private LocalDateTime timestamp;

    // Rank information
    private String tier;           // IRON, BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER, CHALLENGER
    private String rank;           // I, II, III, IV (null for MASTER+)
    private Integer leaguePoints;  // 0-100 (or higher for MASTER+)

    // Stats at that moment
    private Integer wins;
    private Integer losses;

    private String queueType;      // RANKED_SOLO_5x5, RANKED_FLEX_SR

    // Relation to match that triggered this snapshot
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private MatchEntity triggeringMatch;

    // LP change from previous entry (calculated)
    private Integer lpChange;      // +18, -15, etc. (null for first entry)

    public RankHistory() {
        // Default constructor for JPA
    }

    public RankHistory(Summoner summoner, LocalDateTime timestamp, String tier, String rank, 
                       Integer leaguePoints, Integer wins, Integer losses, String queueType) {
        this.summoner = summoner;
        this.timestamp = timestamp;
        this.tier = tier;
        this.rank = rank;
        this.leaguePoints = leaguePoints;
        this.wins = wins;
        this.losses = losses;
        this.queueType = queueType;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Summoner getSummoner() {
        return summoner;
    }

    public void setSummoner(Summoner summoner) {
        this.summoner = summoner;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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
    }

    public Integer getLosses() {
        return losses;
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }

    public MatchEntity getTriggeringMatch() {
        return triggeringMatch;
    }

    public void setTriggeringMatch(MatchEntity triggeringMatch) {
        this.triggeringMatch = triggeringMatch;
    }

    public Integer getLpChange() {
        return lpChange;
    }

    public void setLpChange(Integer lpChange) {
        this.lpChange = lpChange;
    }

    /**
     * Calculates total games played at this snapshot.
     */
    public int getTotalGames() {
        return (wins != null ? wins : 0) + (losses != null ? losses : 0);
    }

    /**
     * Calculates win rate percentage at this snapshot.
     */
    public double getWinRate() {
        int total = getTotalGames();
        if (total == 0) return 0.0;
        return ((wins != null ? wins : 0) * 100.0) / total;
    }

    /**
     * Gets a formatted rank string (e.g., "GOLD II")
     */
    public String getFormattedRank() {
        if (tier == null) return "Unranked";
        if (rank == null || rank.isEmpty()) return tier; // For MASTER, GRANDMASTER, CHALLENGER
        return tier + " " + rank;
    }

    /**
     * Gets full rank string with LP (e.g., "GOLD II (67 LP)")
     */
    public String getFullRankString() {
        String formatted = getFormattedRank();
        if (leaguePoints != null) {
            formatted += String.format(" (%d LP)", leaguePoints);
        }
        return formatted;
    }

    @Override
    public String toString() {
        return String.format("RankHistory{id=%d, summoner=%s, rank=%s, lp=%d, timestamp=%s}",
                id, summoner != null ? summoner.getName() : "null", getFormattedRank(), leaguePoints, timestamp);
    }
}
