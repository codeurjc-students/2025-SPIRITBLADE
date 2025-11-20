package com.tfg.tfg.unit;

import com.tfg.tfg.model.dto.MatchDetailDTO;
import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.dto.ParticipantDTO;
import com.tfg.tfg.model.dto.TeamDTO;
import com.tfg.tfg.model.dto.riot.RiotMatchDTO;
import com.tfg.tfg.model.mapper.RiotMatchMapper;
import com.tfg.tfg.service.DataDragonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RiotMatchMapperTest {

    @Mock
    private DataDragonService dataDragonService;

    private RiotMatchDTO riotMatch;
    private RiotMatchDTO.ParticipantDTO participant;
    private RiotMatchDTO.TeamDTO team;

    @BeforeEach
    void setUp() {
        // Setup mock DataDragonService with lenient stubbing
        lenient().when(dataDragonService.getChampionIconUrl(anyLong())).thenReturn("http://example.com/champion.png");
        lenient().when(dataDragonService.getChampionNameById(anyLong())).thenReturn("TestChampion");

        // Create test data
        riotMatch = new RiotMatchDTO();
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        metadata.setMatchId("EUW1_123456789");
        riotMatch.setMetadata(metadata);

        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        info.setGameDuration(1800L);
        info.setGameEndTimestamp(1700000000000L);
        info.setQueueId(420);
        riotMatch.setInfo(info);

        participant = new RiotMatchDTO.ParticipantDTO();
        participant.setPuuid("test-puuid");
        participant.setChampionName("TestChampion");
        participant.setChampionId(1);
        participant.setKills(10);
        participant.setDeaths(5);
        participant.setAssists(15);
        participant.setWin(true);
        participant.setChampLevel(18);
        participant.setTotalMinionsKilled(200);
        participant.setGoldEarned(15000);
        participant.setTotalDamageDealtToChampions(25000);
        participant.setTeamId(100);
        participant.setTeamPosition("TOP");
        participant.setItem0(1001);
        participant.setItem1(1002);
        participant.setItem2(1003);
        participant.setItem3(1004);
        participant.setItem4(1005);
        participant.setItem5(1006);
        participant.setItem6(1007);

        team = new RiotMatchDTO.TeamDTO();
        team.setTeamId(100);
        team.setWin(true);

        RiotMatchDTO.ObjectivesDTO objectives = new RiotMatchDTO.ObjectivesDTO();
        RiotMatchDTO.ObjectiveDTO baron = new RiotMatchDTO.ObjectiveDTO();
        baron.setKills(1);
        objectives.setBaron(baron);
        RiotMatchDTO.ObjectiveDTO dragon = new RiotMatchDTO.ObjectiveDTO();
        dragon.setKills(3);
        objectives.setDragon(dragon);
        team.setObjectives(objectives);

        RiotMatchDTO.BanDTO ban = new RiotMatchDTO.BanDTO();
        ban.setChampionId(1);
        team.setBans(Arrays.asList(ban));
    }

    @Test
    void testToMatchHistoryDTONullRiotMatch() {
        MatchHistoryDTO result = RiotMatchMapper.toMatchHistoryDTO(null, "test-puuid", dataDragonService);
        assertNull(result);
    }

    @Test
    void testToMatchHistoryDTONullInfo() {
        riotMatch.setInfo(null);
        MatchHistoryDTO result = RiotMatchMapper.toMatchHistoryDTO(riotMatch, "test-puuid", dataDragonService);
        assertNull(result);
    }

    @Test
    void testToMatchHistoryDTONullParticipants() {
        riotMatch.getInfo().setParticipants(null);
        MatchHistoryDTO result = RiotMatchMapper.toMatchHistoryDTO(riotMatch, "test-puuid", dataDragonService);
        assertNull(result);
    }

    @Test
    void testToMatchHistoryDTOParticipantNotFound() {
        riotMatch.getInfo().setParticipants(Arrays.asList(participant));
        MatchHistoryDTO result = RiotMatchMapper.toMatchHistoryDTO(riotMatch, "different-puuid", dataDragonService);
        assertNull(result);
    }

    @Test
    void testToMatchHistoryDTOValid() {
        riotMatch.getInfo().setParticipants(Arrays.asList(participant));

        MatchHistoryDTO result = RiotMatchMapper.toMatchHistoryDTO(riotMatch, "test-puuid", dataDragonService);

        assertNotNull(result);
        assertEquals("EUW1_123456789", result.getMatchId());
        assertEquals("TestChampion", result.getChampionName());
        assertEquals("http://example.com/champion.png", result.getChampionIconUrl());
        assertTrue(result.getWin());
        assertEquals(10, result.getKills());
        assertEquals(5, result.getDeaths());
        assertEquals(15, result.getAssists());
        assertEquals(1800L, result.getGameDuration());
        assertEquals(1700000000L, result.getGameTimestamp());
        assertEquals(420, result.getQueueId());
    }

    @Test
    void testToMatchHistoryDTONullDataDragonService() {
        riotMatch.getInfo().setParticipants(Arrays.asList(participant));

        MatchHistoryDTO result = RiotMatchMapper.toMatchHistoryDTO(riotMatch, "test-puuid", null);

        assertNotNull(result);
        assertNull(result.getChampionIconUrl());
    }

    @Test
    void testToMatchHistoryDTONullMetadata() {
        riotMatch.setMetadata(null);
        riotMatch.getInfo().setParticipants(Arrays.asList(participant));

        MatchHistoryDTO result = RiotMatchMapper.toMatchHistoryDTO(riotMatch, "test-puuid", dataDragonService);

        assertNotNull(result);
        assertNull(result.getMatchId());
    }

    @Test
    void testToMatchDetailDTONullRiotMatch() {
        MatchDetailDTO result = RiotMatchMapper.toMatchDetailDTO(null, dataDragonService);
        assertNull(result);
    }

    @Test
    void testToMatchDetailDTONullInfo() {
        riotMatch.setInfo(null);
        MatchDetailDTO result = RiotMatchMapper.toMatchDetailDTO(riotMatch, dataDragonService);
        assertNull(result);
    }

    @Test
    void testToMatchDetailDTOValid() {
        riotMatch.getInfo().setParticipants(Arrays.asList(participant));
        riotMatch.getInfo().setTeams(Arrays.asList(team));

        MatchDetailDTO result = RiotMatchMapper.toMatchDetailDTO(riotMatch, dataDragonService);

        assertNotNull(result);
        assertEquals("EUW1_123456789", result.getMatchId());
        assertEquals(1800L, result.getGameDuration());
        assertEquals(420, result.getQueueId());
        assertEquals(1, result.getParticipants().size());
        assertEquals(1, result.getTeams().size());
    }

    @Test
    void testToParticipantDTONull() {
        ParticipantDTO result = RiotMatchMapper.toParticipantDTO(null, dataDragonService);
        assertNull(result);
    }

    @Test
    void testToParticipantDTOValid() {
        ParticipantDTO result = RiotMatchMapper.toParticipantDTO(participant, dataDragonService);

        assertNotNull(result);
        assertEquals("TestChampion", result.getChampionName());
        assertEquals("http://example.com/champion.png", result.getChampionIconUrl());
        assertEquals(10, result.getKills());
        assertEquals(5, result.getDeaths());
        assertEquals(15, result.getAssists());
        assertTrue(result.getWin());
        assertEquals(18, result.getLevel());
        assertEquals(100, result.getTeamId());
        assertEquals("TOP", result.getTeamPosition());
        assertEquals(1001, result.getItem0());
        assertEquals(1007, result.getItem6());
    }

    @Test
    void testToParticipantDTONullDataDragonService() {
        ParticipantDTO result = RiotMatchMapper.toParticipantDTO(participant, null);

        assertNotNull(result);
        assertNull(result.getChampionIconUrl());
    }

    @Test
    void testToTeamDTONull() {
        TeamDTO result = RiotMatchMapper.toTeamDTO(null, null, dataDragonService);
        assertNull(result);
    }

    @Test
    void testToTeamDTOValid() {
        List<ParticipantDTO> participants = Arrays.asList(
            RiotMatchMapper.toParticipantDTO(participant, dataDragonService)
        );

        TeamDTO result = RiotMatchMapper.toTeamDTO(team, participants, dataDragonService);

        assertNotNull(result);
        assertEquals(100, result.getTeamId());
        assertTrue(result.getWin());
        assertEquals(1, result.getBaronKills());
        assertEquals(3, result.getDragonKills());
        assertEquals(0, result.getTowerKills()); // Not set in test data
        assertEquals(0, result.getInhibitorKills()); // Not set in test data
        assertEquals(0, result.getRiftHeraldKills()); // Not set in test data
        assertEquals(1, result.getParticipants().size());
        assertEquals(Arrays.asList("TestChampion"), result.getBans());
    }

    @Test
    void testToTeamDTONullObjectives() {
        team.setObjectives(null);
        List<ParticipantDTO> participants = Arrays.asList(
            RiotMatchMapper.toParticipantDTO(participant, dataDragonService)
        );

        TeamDTO result = RiotMatchMapper.toTeamDTO(team, participants, dataDragonService);

        assertNotNull(result);
        assertEquals(0, result.getBaronKills());
        assertEquals(0, result.getDragonKills());
        assertEquals(0, result.getTowerKills());
        assertEquals(0, result.getInhibitorKills());
        assertEquals(0, result.getRiftHeraldKills());
    }

    @Test
    void testToTeamDTONullBans() {
        team.setBans(null);
        List<ParticipantDTO> participants = Arrays.asList(
            RiotMatchMapper.toParticipantDTO(participant, dataDragonService)
        );

        TeamDTO result = RiotMatchMapper.toTeamDTO(team, participants, dataDragonService);

        assertNotNull(result);
        assertNotNull(result.getBans());
        assertTrue(result.getBans().isEmpty());
    }

    @Test
    void testToTeamDTONullDataDragonService() {
        List<ParticipantDTO> participants = Arrays.asList(
            RiotMatchMapper.toParticipantDTO(participant, dataDragonService)
        );

        TeamDTO result = RiotMatchMapper.toTeamDTO(team, participants, null);

        assertNotNull(result);
        assertNotNull(result.getBans());
        assertTrue(result.getBans().isEmpty());
    }

    @Test
    void testGetObjectiveKillsNull() {
        // This is a private method, but we can test it indirectly through toTeamDTO
        team.getObjectives().setBaron(null);
        List<ParticipantDTO> participants = Collections.emptyList();

        TeamDTO result = RiotMatchMapper.toTeamDTO(team, participants, dataDragonService);

        assertEquals(0, result.getBaronKills());
    }
}