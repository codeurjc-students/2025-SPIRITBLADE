package com.tfg.tfg.model.dto.riot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RiotMatchDTO {
    private MetadataDTO metadata;
    private InfoDTO info;

    public MetadataDTO getMetadata() { return metadata; }
    public void setMetadata(MetadataDTO metadata) { this.metadata = metadata; }

    public InfoDTO getInfo() { return info; }
    public void setInfo(InfoDTO info) { this.info = info; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetadataDTO {
        private String dataVersion;
        private String matchId;
        private List<String> participants;

        public String getDataVersion() { return dataVersion; }
        public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }

        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }

        public List<String> getParticipants() { return participants; }
        public void setParticipants(List<String> participants) { this.participants = participants; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoDTO {
        private Long gameCreation;
        private Long gameDuration;
        private Long gameEndTimestamp;
        private Long gameId;
        private String gameMode;
        private String gameType;
        private String gameVersion;
        private List<ParticipantDTO> participants;
        private List<TeamDTO> teams;

        public Long getGameCreation() { return gameCreation; }
        public void setGameCreation(Long gameCreation) { this.gameCreation = gameCreation; }

        public Long getGameDuration() { return gameDuration; }
        public void setGameDuration(Long gameDuration) { this.gameDuration = gameDuration; }

        public Long getGameEndTimestamp() { return gameEndTimestamp; }
        public void setGameEndTimestamp(Long gameEndTimestamp) { this.gameEndTimestamp = gameEndTimestamp; }

        public Long getGameId() { return gameId; }
        public void setGameId(Long gameId) { this.gameId = gameId; }

        public String getGameMode() { return gameMode; }
        public void setGameMode(String gameMode) { this.gameMode = gameMode; }

        public String getGameType() { return gameType; }
        public void setGameType(String gameType) { this.gameType = gameType; }

        public String getGameVersion() { return gameVersion; }
        public void setGameVersion(String gameVersion) { this.gameVersion = gameVersion; }

        public List<ParticipantDTO> getParticipants() { return participants; }
        public void setParticipants(List<ParticipantDTO> participants) { this.participants = participants; }

        public List<TeamDTO> getTeams() { return teams; }
        public void setTeams(List<TeamDTO> teams) { this.teams = teams; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParticipantDTO {
        private String puuid;
        private String summonerName;
        private String riotIdGameName;
        private String riotIdTagline;
        private String championName;
        private Integer championId;
        private Integer kills;
        private Integer deaths;
        private Integer assists;
        private Boolean win;
        private Integer champLevel;
        private Integer totalMinionsKilled;
        private Integer goldEarned;
        private Integer totalDamageDealtToChampions;
        private Integer teamId;
        private String teamPosition;
        private Integer item0;
        private Integer item1;
        private Integer item2;
        private Integer item3;
        private Integer item4;
        private Integer item5;
        private Integer item6;

        public String getPuuid() { return puuid; }
        public void setPuuid(String puuid) { this.puuid = puuid; }

        public String getSummonerName() { return summonerName; }
        public void setSummonerName(String summonerName) { this.summonerName = summonerName; }

        public String getRiotIdGameName() { return riotIdGameName; }
        public void setRiotIdGameName(String riotIdGameName) { this.riotIdGameName = riotIdGameName; }

        public String getRiotIdTagline() { return riotIdTagline; }
        public void setRiotIdTagline(String riotIdTagline) { this.riotIdTagline = riotIdTagline; }

        public String getChampionName() { return championName; }
        public void setChampionName(String championName) { this.championName = championName; }

        public Integer getChampionId() { return championId; }
        public void setChampionId(Integer championId) { this.championId = championId; }

        public Integer getKills() { return kills; }
        public void setKills(Integer kills) { this.kills = kills; }

        public Integer getDeaths() { return deaths; }
        public void setDeaths(Integer deaths) { this.deaths = deaths; }

        public Integer getAssists() { return assists; }
        public void setAssists(Integer assists) { this.assists = assists; }

        public Boolean getWin() { return win; }
        public void setWin(Boolean win) { this.win = win; }

        public Integer getChampLevel() { return champLevel; }
        public void setChampLevel(Integer champLevel) { this.champLevel = champLevel; }

        public Integer getTotalMinionsKilled() { return totalMinionsKilled; }
        public void setTotalMinionsKilled(Integer totalMinionsKilled) { this.totalMinionsKilled = totalMinionsKilled; }

        public Integer getGoldEarned() { return goldEarned; }
        public void setGoldEarned(Integer goldEarned) { this.goldEarned = goldEarned; }

        public Integer getTotalDamageDealtToChampions() { return totalDamageDealtToChampions; }
        public void setTotalDamageDealtToChampions(Integer totalDamageDealtToChampions) { this.totalDamageDealtToChampions = totalDamageDealtToChampions; }

        public Integer getTeamId() { return teamId; }
        public void setTeamId(Integer teamId) { this.teamId = teamId; }

        public String getTeamPosition() { return teamPosition; }
        public void setTeamPosition(String teamPosition) { this.teamPosition = teamPosition; }

        public Integer getItem0() { return item0; }
        public void setItem0(Integer item0) { this.item0 = item0; }

        public Integer getItem1() { return item1; }
        public void setItem1(Integer item1) { this.item1 = item1; }

        public Integer getItem2() { return item2; }
        public void setItem2(Integer item2) { this.item2 = item2; }

        public Integer getItem3() { return item3; }
        public void setItem3(Integer item3) { this.item3 = item3; }

        public Integer getItem4() { return item4; }
        public void setItem4(Integer item4) { this.item4 = item4; }

        public Integer getItem5() { return item5; }
        public void setItem5(Integer item5) { this.item5 = item5; }

        public Integer getItem6() { return item6; }
        public void setItem6(Integer item6) { this.item6 = item6; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamDTO {
        private Integer teamId;
        private Boolean win;
        private ObjectivesDTO objectives;
        private List<BanDTO> bans;

        public Integer getTeamId() { return teamId; }
        public void setTeamId(Integer teamId) { this.teamId = teamId; }

        public Boolean getWin() { return win; }
        public void setWin(Boolean win) { this.win = win; }

        public ObjectivesDTO getObjectives() { return objectives; }
        public void setObjectives(ObjectivesDTO objectives) { this.objectives = objectives; }

        public List<BanDTO> getBans() { return bans; }
        public void setBans(List<BanDTO> bans) { this.bans = bans; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ObjectivesDTO {
        private ObjectiveDTO baron;
        private ObjectiveDTO dragon;
        private ObjectiveDTO tower;
        private ObjectiveDTO inhibitor;
        private ObjectiveDTO riftHerald;

        public ObjectiveDTO getBaron() { return baron; }
        public void setBaron(ObjectiveDTO baron) { this.baron = baron; }

        public ObjectiveDTO getDragon() { return dragon; }
        public void setDragon(ObjectiveDTO dragon) { this.dragon = dragon; }

        public ObjectiveDTO getTower() { return tower; }
        public void setTower(ObjectiveDTO tower) { this.tower = tower; }

        public ObjectiveDTO getInhibitor() { return inhibitor; }
        public void setInhibitor(ObjectiveDTO inhibitor) { this.inhibitor = inhibitor; }

        public ObjectiveDTO getRiftHerald() { return riftHerald; }
        public void setRiftHerald(ObjectiveDTO riftHerald) { this.riftHerald = riftHerald; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ObjectiveDTO {
        private Boolean first;
        private Integer kills;

        public Boolean getFirst() { return first; }
        public void setFirst(Boolean first) { this.first = first; }

        public Integer getKills() { return kills; }
        public void setKills(Integer kills) { this.kills = kills; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BanDTO {
        private Integer championId;
        private Integer pickTurn;

        public Integer getChampionId() { return championId; }
        public void setChampionId(Integer championId) { this.championId = championId; }

        public Integer getPickTurn() { return pickTurn; }
        public void setPickTurn(Integer pickTurn) { this.pickTurn = pickTurn; }
    }
}
