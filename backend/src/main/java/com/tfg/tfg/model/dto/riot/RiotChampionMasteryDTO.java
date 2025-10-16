package com.tfg.tfg.model.dto.riot;

/**
 * DTO for Riot API Champion Mastery response
 * API: /lol/champion-mastery/v4/champion-masteries/by-puuid/{encryptedPUUID}/top
 */
public class RiotChampionMasteryDTO {
    private String puuid;
    private Long championId;
    private Integer championLevel;
    private Integer championPoints;
    private Long lastPlayTime;
    private Integer championPointsSinceLastLevel;
    private Integer championPointsUntilNextLevel;
    private Boolean chestGranted;
    private Integer tokensEarned;
    
    // Fields added by our service (not from Riot API)
    private String championName;
    private String championIconUrl;

    public RiotChampionMasteryDTO() {}

    // Getters and Setters
    public String getPuuid() {
        return puuid;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public Long getChampionId() {
        return championId;
    }

    public void setChampionId(Long championId) {
        this.championId = championId;
    }

    public Integer getChampionLevel() {
        return championLevel;
    }

    public void setChampionLevel(Integer championLevel) {
        this.championLevel = championLevel;
    }

    public Integer getChampionPoints() {
        return championPoints;
    }

    public void setChampionPoints(Integer championPoints) {
        this.championPoints = championPoints;
    }

    public Long getLastPlayTime() {
        return lastPlayTime;
    }

    public void setLastPlayTime(Long lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    public Integer getChampionPointsSinceLastLevel() {
        return championPointsSinceLastLevel;
    }

    public void setChampionPointsSinceLastLevel(Integer championPointsSinceLastLevel) {
        this.championPointsSinceLastLevel = championPointsSinceLastLevel;
    }

    public Integer getChampionPointsUntilNextLevel() {
        return championPointsUntilNextLevel;
    }

    public void setChampionPointsUntilNextLevel(Integer championPointsUntilNextLevel) {
        this.championPointsUntilNextLevel = championPointsUntilNextLevel;
    }

    public Boolean getChestGranted() {
        return chestGranted;
    }

    public void setChestGranted(Boolean chestGranted) {
        this.chestGranted = chestGranted;
    }

    public Integer getTokensEarned() {
        return tokensEarned;
    }

    public void setTokensEarned(Integer tokensEarned) {
        this.tokensEarned = tokensEarned;
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
}
