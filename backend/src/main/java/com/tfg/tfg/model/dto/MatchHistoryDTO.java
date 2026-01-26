package com.tfg.tfg.model.dto;

public class MatchHistoryDTO {
    private String matchId;
    private String championName;
    private String championIconUrl;
    private Boolean win;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Long gameDuration; // in seconds
    private Long gameTimestamp;
    private Integer lpAtMatch; // Approximate LP at this match (calculated)
    private Integer queueId; // 420=Solo/Duo, 440=Flex, etc.
    private Integer visionScore;

    public MatchHistoryDTO() {
    }

    public MatchHistoryDTO(String matchId, String championName, Boolean win,
            Integer kills, Integer deaths, Integer assists,
            Long gameDuration, Long gameTimestamp) {
        this.matchId = matchId;
        this.championName = championName;
        this.win = win;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.gameDuration = gameDuration;
        this.gameTimestamp = gameTimestamp;
    }

    // Getters and Setters
    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getChampionName() {
        return championName;
    }

    public void setChampionName(String championName) {
        this.championName = championName;
    }

    public String getChampionIconUrl() {
        return championIconUrl;
    }

    public void setChampionIconUrl(String championIconUrl) {
        this.championIconUrl = championIconUrl;
    }

    public Boolean getWin() {
        return win;
    }

    public void setWin(Boolean win) {
        this.win = win;
    }

    public Integer getKills() {
        return kills;
    }

    public void setKills(Integer kills) {
        this.kills = kills;
    }

    public Integer getDeaths() {
        return deaths;
    }

    public void setDeaths(Integer deaths) {
        this.deaths = deaths;
    }

    public Integer getAssists() {
        return assists;
    }

    public void setAssists(Integer assists) {
        this.assists = assists;
    }

    public Long getGameDuration() {
        return gameDuration;
    }

    public void setGameDuration(Long gameDuration) {
        this.gameDuration = gameDuration;
    }

    public Long getGameTimestamp() {
        return gameTimestamp;
    }

    public void setGameTimestamp(Long gameTimestamp) {
        this.gameTimestamp = gameTimestamp;
    }

    public Integer getLpAtMatch() {
        return lpAtMatch;
    }

    public void setLpAtMatch(Integer lpAtMatch) {
        this.lpAtMatch = lpAtMatch;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public Integer getVisionScore() {
        return visionScore;
    }

    public void setVisionScore(Integer visionScore) {
        this.visionScore = visionScore;
    }

    public String getKda() {
        return kills + "/" + deaths + "/" + assists;
    }

    public String getFormattedDuration() {
        if (gameDuration == null)
            return "0m";
        long minutes = gameDuration / 60;
        return minutes + "m";
    }
}
