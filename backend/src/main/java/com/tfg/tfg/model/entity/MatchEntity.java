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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "matches", indexes = {
        @Index(name = "idx_match_summoner", columnList = "summoner_id"),
        @Index(name = "idx_match_timestamp", columnList = "timestamp"),
        @Index(name = "idx_match_id", columnList = "matchId")
})
@Getter
@Setter
@NoArgsConstructor
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
    private Integer visionScore;

    private String championName;
    private Integer championId;
    private String role;
    private String lane;
    private Long gameDuration;
    private String gameMode;
    private Integer queueId; // Queue ID
    private Integer totalDamageDealt;
    private Integer goldEarned;
    private Integer champLevel;
    private String summonerName; // Denormalized for quick access

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summoner_id")
    private Summoner summoner;

    public MatchEntity(String matchId, Summoner summoner) {
        this.matchId = matchId;
        this.summoner = summoner;
    }
}
