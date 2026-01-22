package com.tfg.tfg.model.mapper;

import com.tfg.tfg.model.dto.MatchDetailDTO;
import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.dto.ParticipantDTO;
import com.tfg.tfg.model.dto.TeamDTO;
import com.tfg.tfg.model.dto.riot.RiotMatchDTO;
import com.tfg.tfg.service.DataDragonService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mapper helper to convert between Riot API DTOs and internal DTOs
 * Centralizes mapping logic for Riot API responses to keep services thin.
 */
public final class RiotMatchMapper {

    private RiotMatchMapper() {
        // static helper
    }

    /**
     * Convert RiotMatchDTO to MatchHistoryDTO for a specific player
     * 
     * @param riotMatch         The Riot API match data
     * @param puuid             The player's PUUID to find their participant data
     * @param dataDragonService Service for champion icon URLs
     * @return MatchHistoryDTO or null if participant not found
     */
    public static MatchHistoryDTO toMatchHistoryDTO(RiotMatchDTO riotMatch, String puuid,
            DataDragonService dataDragonService) {
        if (riotMatch == null || riotMatch.getInfo() == null ||
                riotMatch.getInfo().getParticipants() == null) {
            return null;
        }

        // Find the participant matching the PUUID
        RiotMatchDTO.ParticipantDTO participant = riotMatch.getInfo().getParticipants().stream()
                .filter(p -> puuid.equals(p.getPuuid()))
                .findFirst()
                .orElse(null);

        if (participant == null) {
            return null;
        }

        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setMatchId(riotMatch.getMetadata() != null ? riotMatch.getMetadata().getMatchId() : null);
        dto.setChampionName(participant.getChampionName());
        dto.setChampionIconUrl(dataDragonService != null ? dataDragonService.getChampionIconUrl(
                participant.getChampionId() != null ? participant.getChampionId().longValue() : null) : null);
        dto.setWin(participant.getWin());
        dto.setKills(participant.getKills());
        dto.setDeaths(participant.getDeaths());
        dto.setAssists(participant.getAssists());
        dto.setVisionScore(participant.getVisionScore());
        dto.setGameDuration(riotMatch.getInfo().getGameDuration());
        Long gameEndTimestamp = riotMatch.getInfo().getGameEndTimestamp();
        dto.setGameTimestamp(gameEndTimestamp != null ? gameEndTimestamp / 1000 : null);
        dto.setQueueId(riotMatch.getInfo().getQueueId());

        return dto;
    }

    /**
     * Convert RiotMatchDTO to MatchDetailDTO with all participants and teams
     * 
     * @param riotMatch         The Riot API match data
     * @param dataDragonService Service for champion icon URLs
     * @return MatchDetailDTO with complete match information
     */
    public static MatchDetailDTO toMatchDetailDTO(RiotMatchDTO riotMatch, DataDragonService dataDragonService) {
        if (riotMatch == null || riotMatch.getInfo() == null) {
            return null;
        }

        MatchDetailDTO dto = new MatchDetailDTO();

        // Basic match info
        dto.setMatchId(riotMatch.getMetadata() != null ? riotMatch.getMetadata().getMatchId() : null);
        dto.setGameCreation(riotMatch.getInfo().getGameCreation());
        dto.setGameDuration(riotMatch.getInfo().getGameDuration());
        dto.setGameMode(riotMatch.getInfo().getGameMode());
        dto.setGameType(riotMatch.getInfo().getGameType());
        dto.setGameVersion(riotMatch.getInfo().getGameVersion());
        dto.setQueueId(riotMatch.getInfo().getQueueId());

        // Map all participants
        List<ParticipantDTO> participants = new ArrayList<>();
        if (riotMatch.getInfo().getParticipants() != null) {
            for (RiotMatchDTO.ParticipantDTO riotParticipant : riotMatch.getInfo().getParticipants()) {
                participants.add(toParticipantDTO(riotParticipant, dataDragonService));
            }
        }
        dto.setParticipants(participants);

        // Map teams
        List<TeamDTO> teams = new ArrayList<>();
        if (riotMatch.getInfo().getTeams() != null) {
            for (RiotMatchDTO.TeamDTO riotTeam : riotMatch.getInfo().getTeams()) {
                teams.add(toTeamDTO(riotTeam, participants, dataDragonService));
            }
        }
        dto.setTeams(teams);

        return dto;
    }

    /**
     * Convert Riot ParticipantDTO to internal ParticipantDTO
     * 
     * @param riotParticipant   The Riot API participant data
     * @param dataDragonService Service for champion icon URLs
     * @return ParticipantDTO
     */
    public static ParticipantDTO toParticipantDTO(RiotMatchDTO.ParticipantDTO riotParticipant,
            DataDragonService dataDragonService) {
        if (riotParticipant == null)
            return null;

        ParticipantDTO dto = new ParticipantDTO();

        dto.setSummonerName(riotParticipant.getSummonerName());
        dto.setRiotIdGameName(riotParticipant.getRiotIdGameName());
        dto.setRiotIdTagline(riotParticipant.getRiotIdTagline());
        dto.setChampionName(riotParticipant.getChampionName());
        dto.setChampionIconUrl(dataDragonService != null ? dataDragonService.getChampionIconUrl(
                riotParticipant.getChampionId() != null ? riotParticipant.getChampionId().longValue() : null) : null);
        dto.setKills(riotParticipant.getKills());
        dto.setDeaths(riotParticipant.getDeaths());
        dto.setAssists(riotParticipant.getAssists());
        dto.setLevel(riotParticipant.getChampLevel());
        dto.setTotalMinionsKilled(riotParticipant.getTotalMinionsKilled());
        dto.setGoldEarned(riotParticipant.getGoldEarned());
        dto.setTotalDamageDealtToChampions(riotParticipant.getTotalDamageDealtToChampions());
        dto.setVisionScore(riotParticipant.getVisionScore());
        dto.setWin(riotParticipant.getWin());
        dto.setTeamId(riotParticipant.getTeamId());
        dto.setTeamPosition(riotParticipant.getTeamPosition());

        // Items
        dto.setItem0(riotParticipant.getItem0());
        dto.setItem1(riotParticipant.getItem1());
        dto.setItem2(riotParticipant.getItem2());
        dto.setItem3(riotParticipant.getItem3());
        dto.setItem4(riotParticipant.getItem4());
        dto.setItem5(riotParticipant.getItem5());
        dto.setItem6(riotParticipant.getItem6());

        return dto;
    }

    /**
     * Convert Riot TeamDTO to internal TeamDTO
     * 
     * @param riotTeam          The Riot API team data
     * @param allParticipants   All participants in the match
     * @param dataDragonService Service for champion names
     * @return TeamDTO
     */
    public static TeamDTO toTeamDTO(RiotMatchDTO.TeamDTO riotTeam, List<ParticipantDTO> allParticipants,
            DataDragonService dataDragonService) {
        if (riotTeam == null)
            return null;

        TeamDTO dto = new TeamDTO();

        dto.setTeamId(riotTeam.getTeamId());
        dto.setWin(riotTeam.getWin());

        // Filter participants by team
        if (allParticipants != null) {
            dto.setParticipants(allParticipants.stream()
                    .filter(p -> riotTeam.getTeamId().equals(p.getTeamId()))
                    .toList());
        }

        // Objectives - initialize to 0 if not present
        dto.setBaronKills(
                getObjectiveKills(riotTeam.getObjectives() != null ? riotTeam.getObjectives().getBaron() : null));
        dto.setDragonKills(
                getObjectiveKills(riotTeam.getObjectives() != null ? riotTeam.getObjectives().getDragon() : null));
        dto.setTowerKills(
                getObjectiveKills(riotTeam.getObjectives() != null ? riotTeam.getObjectives().getTower() : null));
        dto.setInhibitorKills(
                getObjectiveKills(riotTeam.getObjectives() != null ? riotTeam.getObjectives().getInhibitor() : null));
        dto.setRiftHeraldKills(
                getObjectiveKills(riotTeam.getObjectives() != null ? riotTeam.getObjectives().getRiftHerald() : null));

        // Bans - initialize to empty list if not present or service unavailable
        if (riotTeam.getBans() != null && dataDragonService != null) {
            dto.setBans(riotTeam.getBans().stream()
                    .map(ban -> ban.getChampionId() != null
                            ? dataDragonService.getChampionNameById(ban.getChampionId().longValue())
                            : null)
                    .filter(name -> name != null && !name.isEmpty())
                    .toList());
        } else {
            dto.setBans(Collections.emptyList());
        }

        return dto;
    }

    /**
     * Get kills from an objective, returns 0 if null
     */
    private static Integer getObjectiveKills(RiotMatchDTO.ObjectiveDTO objective) {
        return objective != null ? objective.getKills() : 0;
    }
}