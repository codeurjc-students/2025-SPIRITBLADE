package com.tfg.tfg.model.dto;

import java.util.List;

public class TeamDTO {
    private Integer teamId;
    private Boolean win;
    private List<ParticipantDTO> participants;
    
    // Objectives
    private Integer baronKills;
    private Integer dragonKills;
    private Integer towerKills;
    private Integer inhibitorKills;
    private Integer riftHeraldKills;
    
    // Bans
    private List<String> bans;

    // Default constructor required for serialization
    public TeamDTO() {
        // Empty constructor for frameworks
    }

    public TeamDTO(Integer teamId, Boolean win) {
        this.teamId = teamId;
        this.win = win;
    }

    // Getters and Setters
    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Boolean getWin() {
        return win;
    }

    public void setWin(Boolean win) {
        this.win = win;
    }

    public List<ParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantDTO> participants) {
        this.participants = participants;
    }

    public Integer getBaronKills() {
        return baronKills;
    }

    public void setBaronKills(Integer baronKills) {
        this.baronKills = baronKills;
    }

    public Integer getDragonKills() {
        return dragonKills;
    }

    public void setDragonKills(Integer dragonKills) {
        this.dragonKills = dragonKills;
    }

    public Integer getTowerKills() {
        return towerKills;
    }

    public void setTowerKills(Integer towerKills) {
        this.towerKills = towerKills;
    }

    public Integer getInhibitorKills() {
        return inhibitorKills;
    }

    public void setInhibitorKills(Integer inhibitorKills) {
        this.inhibitorKills = inhibitorKills;
    }

    public Integer getRiftHeraldKills() {
        return riftHeraldKills;
    }

    public void setRiftHeraldKills(Integer riftHeraldKills) {
        this.riftHeraldKills = riftHeraldKills;
    }

    public List<String> getBans() {
        return bans;
    }

    public void setBans(List<String> bans) {
        this.bans = bans;
    }
}
