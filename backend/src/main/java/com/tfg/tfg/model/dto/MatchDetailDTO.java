package com.tfg.tfg.model.dto;

import java.util.List;

public class MatchDetailDTO {
    private String matchId;
    private Long gameCreation;
    private Long gameDuration;
    private String gameMode;
    private String gameType;
    private String gameVersion;
    private Integer queueId;
    
    private List<TeamDTO> teams;
    private List<ParticipantDTO> participants;

    // Default constructor required for serialization
    public MatchDetailDTO() {
        // Empty constructor for frameworks
    }

    public MatchDetailDTO(String matchId) {
        this.matchId = matchId;
    }

    // Getters and Setters
    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public Long getGameCreation() {
        return gameCreation;
    }

    public void setGameCreation(Long gameCreation) {
        this.gameCreation = gameCreation;
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

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public List<TeamDTO> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamDTO> teams) {
        this.teams = teams;
    }

    public List<ParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantDTO> participants) {
        this.participants = participants;
    }

    public String getFormattedDuration() {
        if (gameDuration == null) return "0m 0s";
        long minutes = gameDuration / 60;
        long seconds = gameDuration % 60;
        return minutes + "m " + seconds + "s";
    }
}
