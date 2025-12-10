package com.tfg.tfg.model.dto;

public class ParticipantDTO {
    private String summonerName;
    private String riotIdGameName;
    private String riotIdTagline;
    private String championName;
    private String championIconUrl;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Integer level;
    private Integer totalMinionsKilled;
    private Integer goldEarned;
    private Integer totalDamageDealtToChampions;
    private Integer visionScore;
    private Boolean win;
    private Integer teamId;
    private String teamPosition; // TOP, JUNGLE, MIDDLE, BOTTOM, UTILITY
    
    // Items (0-6)
    private Integer item0;
    private Integer item1;
    private Integer item2;
    private Integer item3;
    private Integer item4;
    private Integer item5;
    private Integer item6; // Trinket

    // Default constructor required for serialization
    public ParticipantDTO() {
        // Empty constructor for frameworks
    }

    // Getters and Setters
    public String getSummonerName() {
        return summonerName;
    }

    public void setSummonerName(String summonerName) {
        this.summonerName = summonerName;
    }

    public String getRiotIdGameName() {
        return riotIdGameName;
    }

    public void setRiotIdGameName(String riotIdGameName) {
        this.riotIdGameName = riotIdGameName;
    }

    public String getRiotIdTagline() {
        return riotIdTagline;
    }

    public void setRiotIdTagline(String riotIdTagline) {
        this.riotIdTagline = riotIdTagline;
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

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getTotalMinionsKilled() {
        return totalMinionsKilled;
    }

    public void setTotalMinionsKilled(Integer totalMinionsKilled) {
        this.totalMinionsKilled = totalMinionsKilled;
    }

    public Integer getGoldEarned() {
        return goldEarned;
    }

    public void setGoldEarned(Integer goldEarned) {
        this.goldEarned = goldEarned;
    }

    public Integer getTotalDamageDealtToChampions() {
        return totalDamageDealtToChampions;
    }

    public void setTotalDamageDealtToChampions(Integer totalDamageDealtToChampions) {
        this.totalDamageDealtToChampions = totalDamageDealtToChampions;
    }

    public Integer getVisionScore() {
        return visionScore;
    }

    public void setVisionScore(Integer visionScore) {
        this.visionScore = visionScore;
    }

    public Boolean getWin() {
        return win;
    }

    public void setWin(Boolean win) {
        this.win = win;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public String getTeamPosition() {
        return teamPosition;
    }

    public void setTeamPosition(String teamPosition) {
        this.teamPosition = teamPosition;
    }

    public Integer getItem0() {
        return item0;
    }

    public void setItem0(Integer item0) {
        this.item0 = item0;
    }

    public Integer getItem1() {
        return item1;
    }

    public void setItem1(Integer item1) {
        this.item1 = item1;
    }

    public Integer getItem2() {
        return item2;
    }

    public void setItem2(Integer item2) {
        this.item2 = item2;
    }

    public Integer getItem3() {
        return item3;
    }

    public void setItem3(Integer item3) {
        this.item3 = item3;
    }

    public Integer getItem4() {
        return item4;
    }

    public void setItem4(Integer item4) {
        this.item4 = item4;
    }

    public Integer getItem5() {
        return item5;
    }

    public void setItem5(Integer item5) {
        this.item5 = item5;
    }

    public Integer getItem6() {
        return item6;
    }

    public void setItem6(Integer item6) {
        this.item6 = item6;
    }

    public String getKda() {
        return kills + "/" + deaths + "/" + assists;
    }

    public Double getKdaRatio() {
        if (deaths == null || deaths == 0) {
            return (kills != null && assists != null) ? (double)(kills + assists) : 0.0;
        }
        return (kills != null && assists != null) ? (double)(kills + assists) / deaths : 0.0;
    }
}
