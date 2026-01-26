package com.tfg.tfg.model.dto.riot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiotMatchDTO {
    private MetadataDTO metadata;
    private InfoDTO info;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetadataDTO {
        private String dataVersion;
        private String matchId;
        private List<String> participants;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoDTO {
        private Long gameCreation;
        private Long gameDuration;
        private Long gameEndTimestamp;
        private Long gameId;
        private String gameMode;
        private String gameType;
        private String gameVersion;
        private Integer queueId;
        private List<ParticipantDTO> participants;
        private List<TeamDTO> teams;
    }

    @Data
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
        private Integer visionScore;
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
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamDTO {
        private Integer teamId;
        private Boolean win;
        private ObjectivesDTO objectives;
        private List<BanDTO> bans;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ObjectivesDTO {
        private ObjectiveDTO baron;
        private ObjectiveDTO dragon;
        private ObjectiveDTO tower;
        private ObjectiveDTO inhibitor;
        private ObjectiveDTO riftHerald;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ObjectiveDTO {
        private Boolean first;
        private Integer kills;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BanDTO {
        private Integer championId;
        private Integer pickTurn;
    }
}
