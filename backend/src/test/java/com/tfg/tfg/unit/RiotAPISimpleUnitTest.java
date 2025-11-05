package com.tfg.tfg.unit;

import org.junit.jupiter.api.Test;

import com.tfg.tfg.model.dto.riot.RiotAccountDTO;
import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;
import com.tfg.tfg.model.dto.riot.RiotLeagueEntryDTO;
import com.tfg.tfg.model.dto.riot.RiotMatchDTO;
import com.tfg.tfg.model.dto.riot.RiotSummonerDTO;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for all Riot API DTOs.
 * Tests cover all getters, setters, constructors, and Lombok annotations.
 */
class RiotAPISimpleUnitTest {

    // ============ RiotAccountDTO Tests ============
    
    @Test
    void testRiotAccountDTO_NoArgsConstructor() {
        RiotAccountDTO dto = new RiotAccountDTO();
        assertNotNull(dto);
        assertNull(dto.getPuuid());
        assertNull(dto.getGameName());
        assertNull(dto.getTagLine());
    }

    @Test
    void testRiotAccountDTO_AllArgsConstructor() {
        String puuid = "test-puuid-123";
        String gameName = "Faker";
        String tagLine = "KR1";

        RiotAccountDTO dto = new RiotAccountDTO(puuid, gameName, tagLine);

        assertEquals(puuid, dto.getPuuid());
        assertEquals(gameName, dto.getGameName());
        assertEquals(tagLine, dto.getTagLine());
    }

    @Test
    void testRiotAccountDTO_Setters() {
        RiotAccountDTO dto = new RiotAccountDTO();
        
        dto.setPuuid("new-puuid");
        dto.setGameName("TestPlayer");
        dto.setTagLine("EUW");

        assertEquals("new-puuid", dto.getPuuid());
        assertEquals("TestPlayer", dto.getGameName());
        assertEquals("EUW", dto.getTagLine());
    }

    @Test
    void testRiotAccountDTO_EqualsAndHashCode() {
        RiotAccountDTO dto1 = new RiotAccountDTO("puuid1", "Player1", "NA1");
        RiotAccountDTO dto2 = new RiotAccountDTO("puuid1", "Player1", "NA1");
        RiotAccountDTO dto3 = new RiotAccountDTO("puuid2", "Player2", "EUW");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    void testRiotAccountDTO_ToString() {
        RiotAccountDTO dto = new RiotAccountDTO("puuid", "Name", "Tag");
        String result = dto.toString();
        
        assertTrue(result.contains("puuid"));
        assertTrue(result.contains("Name"));
        assertTrue(result.contains("Tag"));
    }

    // ============ RiotSummonerDTO Tests ============

    @Test
    void testRiotSummonerDTO_NoArgsConstructor() {
        RiotSummonerDTO dto = new RiotSummonerDTO();
        assertNotNull(dto);
        assertNull(dto.getId());
    }

    @Test
    void testRiotSummonerDTO_AllArgsConstructor() {
        RiotSummonerDTO dto = new RiotSummonerDTO(
            "id1", "accId1", "puuid1", "SummonerName", 15, 1234567890L, 150
        );

        assertEquals("id1", dto.getId());
        assertEquals("accId1", dto.getAccountId());
        assertEquals("puuid1", dto.getPuuid());
        assertEquals("SummonerName", dto.getName());
        assertEquals(15, dto.getProfileIconId());
        assertEquals(1234567890L, dto.getRevisionDate());
        assertEquals(150, dto.getSummonerLevel());
    }

    @Test
    void testRiotSummonerDTO_Setters() {
        RiotSummonerDTO dto = new RiotSummonerDTO();
        
        dto.setId("testId");
        dto.setAccountId("testAccId");
        dto.setPuuid("testPuuid");
        dto.setName("TestName");
        dto.setProfileIconId(999);
        dto.setRevisionDate(9999999999L);
        dto.setSummonerLevel(500);

        assertEquals("testId", dto.getId());
        assertEquals("testAccId", dto.getAccountId());
        assertEquals("testPuuid", dto.getPuuid());
        assertEquals("TestName", dto.getName());
        assertEquals(999, dto.getProfileIconId());
        assertEquals(9999999999L, dto.getRevisionDate());
        assertEquals(500, dto.getSummonerLevel());
    }

    @Test
    void testRiotSummonerDTO_EqualsAndHashCode() {
        RiotSummonerDTO dto1 = new RiotSummonerDTO("id", "accId", "puuid", "Name", 1, 100L, 30);
        RiotSummonerDTO dto2 = new RiotSummonerDTO("id", "accId", "puuid", "Name", 1, 100L, 30);
        RiotSummonerDTO dto3 = new RiotSummonerDTO("id2", "accId2", "puuid2", "Name2", 2, 200L, 40);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    // ============ RiotLeagueEntryDTO Tests ============

    @Test
    void testRiotLeagueEntryDTO_NoArgsConstructor() {
        RiotLeagueEntryDTO dto = new RiotLeagueEntryDTO();
        assertNotNull(dto);
        assertNull(dto.getLeagueId());
    }

    @Test
    void testRiotLeagueEntryDTO_AllArgsConstructor() {
        RiotLeagueEntryDTO dto = new RiotLeagueEntryDTO(
            "league1", "RANKED_SOLO_5x5", "DIAMOND", "II", 
            "summId", "SummName", 75, 100, 50
        );

        assertEquals("league1", dto.getLeagueId());
        assertEquals("RANKED_SOLO_5x5", dto.getQueueType());
        assertEquals("DIAMOND", dto.getTier());
        assertEquals("II", dto.getRank());
        assertEquals("summId", dto.getSummonerId());
        assertEquals("SummName", dto.getSummonerName());
        assertEquals(75, dto.getLeaguePoints());
        assertEquals(100, dto.getWins());
        assertEquals(50, dto.getLosses());
    }

    @Test
    void testRiotLeagueEntryDTO_Setters() {
        RiotLeagueEntryDTO dto = new RiotLeagueEntryDTO();
        
        dto.setLeagueId("testLeague");
        dto.setQueueType("RANKED_FLEX_SR");
        dto.setTier("PLATINUM");
        dto.setRank("IV");
        dto.setSummonerId("summ123");
        dto.setSummonerName("PlayerX");
        dto.setLeaguePoints(99);
        dto.setWins(200);
        dto.setLosses(150);

        assertEquals("testLeague", dto.getLeagueId());
        assertEquals("RANKED_FLEX_SR", dto.getQueueType());
        assertEquals("PLATINUM", dto.getTier());
        assertEquals("IV", dto.getRank());
        assertEquals("summ123", dto.getSummonerId());
        assertEquals("PlayerX", dto.getSummonerName());
        assertEquals(99, dto.getLeaguePoints());
        assertEquals(200, dto.getWins());
        assertEquals(150, dto.getLosses());
    }

    @Test
    void testRiotLeagueEntryDTO_AllTiers() {
        String[] tiers = {"IRON", "BRONZE", "SILVER", "GOLD", "PLATINUM", 
                         "DIAMOND", "MASTER", "GRANDMASTER", "CHALLENGER"};
        
        for (String tier : tiers) {
            RiotLeagueEntryDTO dto = new RiotLeagueEntryDTO();
            dto.setTier(tier);
            assertEquals(tier, dto.getTier());
        }
    }

    // ============ RiotChampionMasteryDTO Tests ============

    @Test
    void testRiotChampionMasteryDTO_Constructor() {
        RiotChampionMasteryDTO dto = new RiotChampionMasteryDTO();
        assertNotNull(dto);
    }

    @Test
    void testRiotChampionMasteryDTO_AllFields() {
        RiotChampionMasteryDTO dto = new RiotChampionMasteryDTO();
        
        dto.setPuuid("champ-puuid");
        dto.setChampionId(157L); // Yasuo
        dto.setChampionLevel(7);
        dto.setChampionPoints(500000);
        dto.setLastPlayTime(1234567890L);
        dto.setChampionPointsSinceLastLevel(100000);
        dto.setChampionPointsUntilNextLevel(0);
        dto.setChestGranted(true);
        dto.setTokensEarned(3);
        dto.setChampionName("Yasuo");
        dto.setChampionIconUrl("http://ddragon.leagueoflegends.com/cdn/13.1.1/img/champion/Yasuo.png");

        assertEquals("champ-puuid", dto.getPuuid());
        assertEquals(157L, dto.getChampionId());
        assertEquals(7, dto.getChampionLevel());
        assertEquals(500000, dto.getChampionPoints());
        assertEquals(1234567890L, dto.getLastPlayTime());
        assertEquals(100000, dto.getChampionPointsSinceLastLevel());
        assertEquals(0, dto.getChampionPointsUntilNextLevel());
        assertTrue(dto.getChestGranted());
        assertEquals(3, dto.getTokensEarned());
        assertEquals("Yasuo", dto.getChampionName());
        assertTrue(dto.getChampionIconUrl().contains("Yasuo"));
    }

    @Test
    void testRiotChampionMasteryDTO_ChestNotGranted() {
        RiotChampionMasteryDTO dto = new RiotChampionMasteryDTO();
        dto.setChestGranted(false);
        assertFalse(dto.getChestGranted());
    }

    @Test
    void testRiotChampionMasteryDTO_MultipleMasteryLevels() {
        for (int level = 1; level <= 7; level++) {
            RiotChampionMasteryDTO dto = new RiotChampionMasteryDTO();
            dto.setChampionLevel(level);
            assertEquals(level, dto.getChampionLevel());
        }
    }

    // ============ RiotMatchDTO.MetadataDTO Tests ============

    @Test
    void testRiotMatchDTO_MetadataDTO() {
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        
        metadata.setDataVersion("2");
        metadata.setMatchId("EUW1_123456");
        metadata.setParticipants(Arrays.asList("puuid1", "puuid2", "puuid3"));

        assertEquals("2", metadata.getDataVersion());
        assertEquals("EUW1_123456", metadata.getMatchId());
        assertEquals(3, metadata.getParticipants().size());
        assertTrue(metadata.getParticipants().contains("puuid1"));
    }

    @Test
    void testRiotMatchDTO_MetadataDTO_TenPlayers() {
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        List<String> participants = Arrays.asList(
            "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10"
        );
        
        metadata.setParticipants(participants);
        
        assertEquals(10, metadata.getParticipants().size());
    }

    // ============ RiotMatchDTO.InfoDTO Tests ============

    @Test
    void testRiotMatchDTO_InfoDTO_AllFields() {
        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        
        info.setGameCreation(1609459200000L);
        info.setGameDuration(1800L);
        info.setGameEndTimestamp(1609461000000L);
        info.setGameId(123456789L);
        info.setGameMode("CLASSIC");
        info.setGameType("MATCHED_GAME");
        info.setGameVersion("11.1.1");
        info.setQueueId(420);

        assertEquals(1609459200000L, info.getGameCreation());
        assertEquals(1800L, info.getGameDuration());
        assertEquals(1609461000000L, info.getGameEndTimestamp());
        assertEquals(123456789L, info.getGameId());
        assertEquals("CLASSIC", info.getGameMode());
        assertEquals("MATCHED_GAME", info.getGameType());
        assertEquals("11.1.1", info.getGameVersion());
        assertEquals(420, info.getQueueId());
    }

    @Test
    void testRiotMatchDTO_InfoDTO_WithParticipantsAndTeams() {
        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        
        RiotMatchDTO.ParticipantDTO participant = new RiotMatchDTO.ParticipantDTO();
        RiotMatchDTO.TeamDTO team = new RiotMatchDTO.TeamDTO();
        
        info.setParticipants(Arrays.asList(participant));
        info.setTeams(Arrays.asList(team));

        assertEquals(1, info.getParticipants().size());
        assertEquals(1, info.getTeams().size());
    }

    // ============ RiotMatchDTO.ParticipantDTO Tests ============

    @Test
    void testRiotMatchDTO_ParticipantDTO_AllFields() {
        RiotMatchDTO.ParticipantDTO participant = new RiotMatchDTO.ParticipantDTO();
        
        participant.setPuuid("player-puuid");
        participant.setSummonerName("ProPlayer");
        participant.setRiotIdGameName("ProPlayer");
        participant.setRiotIdTagline("EUW");
        participant.setChampionName("Ahri");
        participant.setChampionId(103);
        participant.setKills(10);
        participant.setDeaths(2);
        participant.setAssists(15);
        participant.setWin(true);
        participant.setChampLevel(18);
        participant.setTotalMinionsKilled(250);
        participant.setGoldEarned(15000);
        participant.setTotalDamageDealtToChampions(25000);
        participant.setTeamId(100);
        participant.setTeamPosition("MIDDLE");
        participant.setItem0(3089);
        participant.setItem1(3157);
        participant.setItem2(3020);
        participant.setItem3(3135);
        participant.setItem4(3165);
        participant.setItem5(3136);
        participant.setItem6(3364);

        assertEquals("player-puuid", participant.getPuuid());
        assertEquals("ProPlayer", participant.getSummonerName());
        assertEquals("ProPlayer", participant.getRiotIdGameName());
        assertEquals("EUW", participant.getRiotIdTagline());
        assertEquals("Ahri", participant.getChampionName());
        assertEquals(103, participant.getChampionId());
        assertEquals(10, participant.getKills());
        assertEquals(2, participant.getDeaths());
        assertEquals(15, participant.getAssists());
        assertTrue(participant.getWin());
        assertEquals(18, participant.getChampLevel());
        assertEquals(250, participant.getTotalMinionsKilled());
        assertEquals(15000, participant.getGoldEarned());
        assertEquals(25000, participant.getTotalDamageDealtToChampions());
        assertEquals(100, participant.getTeamId());
        assertEquals("MIDDLE", participant.getTeamPosition());
        assertEquals(3089, participant.getItem0());
        assertEquals(3157, participant.getItem1());
        assertEquals(3020, participant.getItem2());
        assertEquals(3135, participant.getItem3());
        assertEquals(3165, participant.getItem4());
        assertEquals(3136, participant.getItem5());
        assertEquals(3364, participant.getItem6());
    }

    @Test
    void testRiotMatchDTO_ParticipantDTO_AllPositions() {
        String[] positions = {"TOP", "JUNGLE", "MIDDLE", "BOTTOM", "UTILITY"};
        
        for (String position : positions) {
            RiotMatchDTO.ParticipantDTO participant = new RiotMatchDTO.ParticipantDTO();
            participant.setTeamPosition(position);
            assertEquals(position, participant.getTeamPosition());
        }
    }

    @Test
    void testRiotMatchDTO_ParticipantDTO_LossScenario() {
        RiotMatchDTO.ParticipantDTO participant = new RiotMatchDTO.ParticipantDTO();
        participant.setWin(false);
        participant.setKills(2);
        participant.setDeaths(10);
        participant.setAssists(3);
        
        assertFalse(participant.getWin());
        assertTrue(participant.getDeaths() > participant.getKills());
    }

    // ============ RiotMatchDTO.TeamDTO Tests ============

    @Test
    void testRiotMatchDTO_TeamDTO_AllFields() {
        RiotMatchDTO.TeamDTO team = new RiotMatchDTO.TeamDTO();
        RiotMatchDTO.ObjectivesDTO objectives = new RiotMatchDTO.ObjectivesDTO();
        RiotMatchDTO.BanDTO ban = new RiotMatchDTO.BanDTO();
        
        team.setTeamId(100);
        team.setWin(true);
        team.setObjectives(objectives);
        team.setBans(Arrays.asList(ban));

        assertEquals(100, team.getTeamId());
        assertTrue(team.getWin());
        assertNotNull(team.getObjectives());
        assertEquals(1, team.getBans().size());
    }

    @Test
    void testRiotMatchDTO_TeamDTO_BlueAndRed() {
        RiotMatchDTO.TeamDTO blueTeam = new RiotMatchDTO.TeamDTO();
        RiotMatchDTO.TeamDTO redTeam = new RiotMatchDTO.TeamDTO();
        
        blueTeam.setTeamId(100);
        blueTeam.setWin(true);
        
        redTeam.setTeamId(200);
        redTeam.setWin(false);

        assertEquals(100, blueTeam.getTeamId());
        assertEquals(200, redTeam.getTeamId());
        assertTrue(blueTeam.getWin());
        assertFalse(redTeam.getWin());
    }

    // ============ RiotMatchDTO.ObjectivesDTO Tests ============

    @Test
    void testRiotMatchDTO_ObjectivesDTO_AllObjectives() {
        RiotMatchDTO.ObjectivesDTO objectives = new RiotMatchDTO.ObjectivesDTO();
        
        RiotMatchDTO.ObjectiveDTO baron = new RiotMatchDTO.ObjectiveDTO();
        baron.setFirst(true);
        baron.setKills(2);
        
        RiotMatchDTO.ObjectiveDTO dragon = new RiotMatchDTO.ObjectiveDTO();
        dragon.setFirst(false);
        dragon.setKills(3);
        
        RiotMatchDTO.ObjectiveDTO tower = new RiotMatchDTO.ObjectiveDTO();
        tower.setKills(9);
        
        RiotMatchDTO.ObjectiveDTO inhibitor = new RiotMatchDTO.ObjectiveDTO();
        inhibitor.setKills(2);
        
        RiotMatchDTO.ObjectiveDTO riftHerald = new RiotMatchDTO.ObjectiveDTO();
        riftHerald.setFirst(true);
        riftHerald.setKills(1);
        
        objectives.setBaron(baron);
        objectives.setDragon(dragon);
        objectives.setTower(tower);
        objectives.setInhibitor(inhibitor);
        objectives.setRiftHerald(riftHerald);

        assertNotNull(objectives.getBaron());
        assertTrue(objectives.getBaron().getFirst());
        assertEquals(2, objectives.getBaron().getKills());
        
        assertNotNull(objectives.getDragon());
        assertFalse(objectives.getDragon().getFirst());
        assertEquals(3, objectives.getDragon().getKills());
        
        assertEquals(9, objectives.getTower().getKills());
        assertEquals(2, objectives.getInhibitor().getKills());
        
        assertTrue(objectives.getRiftHerald().getFirst());
        assertEquals(1, objectives.getRiftHerald().getKills());
    }

    // ============ RiotMatchDTO.ObjectiveDTO Tests ============

    @Test
    void testRiotMatchDTO_ObjectiveDTO() {
        RiotMatchDTO.ObjectiveDTO objective = new RiotMatchDTO.ObjectiveDTO();
        
        objective.setFirst(true);
        objective.setKills(5);

        assertTrue(objective.getFirst());
        assertEquals(5, objective.getKills());
    }

    @Test
    void testRiotMatchDTO_ObjectiveDTO_NoFirst() {
        RiotMatchDTO.ObjectiveDTO objective = new RiotMatchDTO.ObjectiveDTO();
        objective.setFirst(false);
        objective.setKills(0);

        assertFalse(objective.getFirst());
        assertEquals(0, objective.getKills());
    }

    // ============ RiotMatchDTO.BanDTO Tests ============

    @Test
    void testRiotMatchDTO_BanDTO() {
        RiotMatchDTO.BanDTO ban = new RiotMatchDTO.BanDTO();
        
        ban.setChampionId(236); // Lucian
        ban.setPickTurn(1);

        assertEquals(236, ban.getChampionId());
        assertEquals(1, ban.getPickTurn());
    }

    @Test
    void testRiotMatchDTO_BanDTO_MultipleBans() {
        int[] bannedChampions = {157, 238, 555, 777, 999}; // Champion IDs
        
        for (int i = 0; i < bannedChampions.length; i++) {
            RiotMatchDTO.BanDTO ban = new RiotMatchDTO.BanDTO();
            ban.setChampionId(bannedChampions[i]);
            ban.setPickTurn(i + 1);
            
            assertEquals(bannedChampions[i], ban.getChampionId());
            assertEquals(i + 1, ban.getPickTurn());
        }
    }

    // ============ RiotMatchDTO Integration Tests ============

    @Test
    void testRiotMatchDTO_FullStructure() {
        RiotMatchDTO match = new RiotMatchDTO();
        
        // Metadata
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        metadata.setMatchId("EUW1_123456");
        metadata.setDataVersion("2");
        match.setMetadata(metadata);
        
        // Info
        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        info.setGameDuration(1800L);
        info.setQueueId(420);
        match.setInfo(info);

        assertNotNull(match.getMetadata());
        assertEquals("EUW1_123456", match.getMetadata().getMatchId());
        assertNotNull(match.getInfo());
        assertEquals(1800L, match.getInfo().getGameDuration());
    }

    @Test
    void testRiotMatchDTO_CompleteMatchData() {
        RiotMatchDTO match = new RiotMatchDTO();
        
        // Metadata with 10 players
        RiotMatchDTO.MetadataDTO metadata = new RiotMatchDTO.MetadataDTO();
        List<String> participants = Arrays.asList(
            "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10"
        );
        metadata.setParticipants(participants);
        match.setMetadata(metadata);
        
        // Info with game data
        RiotMatchDTO.InfoDTO info = new RiotMatchDTO.InfoDTO();
        info.setGameMode("CLASSIC");
        info.setQueueId(420); // Ranked Solo/Duo
        
        // Two teams
        RiotMatchDTO.TeamDTO blueTeam = new RiotMatchDTO.TeamDTO();
        blueTeam.setTeamId(100);
        blueTeam.setWin(true);
        
        RiotMatchDTO.TeamDTO redTeam = new RiotMatchDTO.TeamDTO();
        redTeam.setTeamId(200);
        redTeam.setWin(false);
        
        info.setTeams(Arrays.asList(blueTeam, redTeam));
        match.setInfo(info);

        assertEquals(10, match.getMetadata().getParticipants().size());
        assertEquals(2, match.getInfo().getTeams().size());
        assertEquals("CLASSIC", match.getInfo().getGameMode());
    }
}
