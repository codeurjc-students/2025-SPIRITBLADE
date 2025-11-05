package com.tfg.tfg.integration;

import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.RiotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.tfg.service.storage.MinioStorageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para el servicio de Riot API.
 * Verifica la integración entre:
 * - RiotService
 * - SummonerRepository
 * - MatchRepository
 * - API de Riot (mocked en test profile)
 */
@SpringBootTest
@Transactional
class RiotServiceIntegrationTest {

    @Autowired
    private RiotService riotService;

    @Autowired
    private SummonerRepository summonerRepository;

    @Autowired
    private UserModelRepository userRepository;

    @MockitoBean
    private MinioStorageService storageService;

    private UserModel testUser;

    @BeforeEach
    void setup() {
        // Create test user
        testUser = new UserModel("testuser", "password", "USER");
        testUser = userRepository.save(testUser);
    }

    @Test
    void contextLoads() {
        assertThat(riotService).isNotNull();
        assertThat(summonerRepository).isNotNull();
    }

    @Test
    void testGetDataDragonService_ReturnsInstance() {
        assertThat(riotService.getDataDragonService()).isNotNull();
    }

    @Test
    void testSummonerRepository_CanSaveAndRetrieve() {
        Summoner summoner = new Summoner();
        summoner.setName("TestSummoner");
        summoner.setPuuid("test-puuid-12345");
        summoner.setRiotId("TestSummoner#EUW");
        summoner.setLevel(30);
        
        Summoner saved = summonerRepository.save(summoner);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("TestSummoner");
        
        Summoner found = summonerRepository.findByPuuid("test-puuid-12345").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("TestSummoner");
    }

    @Test
    void testSummonerRepository_FindByName_CaseInsensitive() {
        Summoner summoner = new Summoner();
        summoner.setName("CaseSensitive");
        summoner.setPuuid("test-puuid-case");
        summoner.setRiotId("CaseSensitive#EUW");
        summonerRepository.save(summoner);
        
        Summoner found = summonerRepository.findByNameIgnoreCase("casesensitive").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("CaseSensitive");
    }

    @Test
    void testSummonerRepository_FindByPuuid_ReturnsUnique() {
        Summoner summoner = new Summoner();
        summoner.setName("UniquePuuid");
        summoner.setPuuid("unique-puuid-123");
        summoner.setRiotId("UniquePuuid#EUW");
        summonerRepository.save(summoner);
        
        Summoner found = summonerRepository.findByPuuid("unique-puuid-123").orElse(null);
        assertThat(found).isNotNull();
    }

    @Test
    void testSummonerRepository_NoDuplicatePuuid() {
        Summoner summoner1 = new Summoner();
        summoner1.setName("Summoner1");
        summoner1.setPuuid("duplicate-puuid");
        summoner1.setRiotId("Summoner1#EUW");
        summonerRepository.save(summoner1);
        
        // Trying to save another summoner with same PUUID should fail
        Summoner summoner2 = new Summoner();
        summoner2.setName("Summoner2");
        summoner2.setPuuid("duplicate-puuid");
        summoner2.setRiotId("Summoner2#EUW");
        
        assertThrows(Exception.class, () -> {
            summonerRepository.save(summoner2);
            summonerRepository.flush(); // Force DB constraint check
        });
    }

    @Test
    void testUserRepository_CanLinkSummoner() {
        Summoner summoner = new Summoner();
        summoner.setName("LinkedSummoner");
        summoner.setPuuid("linked-puuid");
        summoner.setRiotId("LinkedSummoner#EUW");
        summonerRepository.save(summoner);
        
        testUser.setLinkedSummonerName("LinkedSummoner");
        userRepository.save(testUser);
        
        UserModel found = userRepository.findById(testUser.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getLinkedSummonerName()).isEqualTo("LinkedSummoner");
    }

    @Test
    void testUserRepository_CanAddFavorites() {
        Summoner favorite = new Summoner();
        favorite.setName("FavoriteSummoner");
        favorite.setPuuid("favorite-puuid");
        favorite.setRiotId("FavoriteSummoner#EUW");
        favorite = summonerRepository.save(favorite);
        summonerRepository.flush();
        
        // Refresh the testUser to ensure it's managed and favoriteSummoners is initialized
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        
        // Use the helper method which handles initialization
        testUser.addFavoriteSummoner(favorite);
        testUser = userRepository.save(testUser);
        userRepository.flush();
        
        // Refresh again to get persisted state
        UserModel found = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(found).isNotNull();
        assertThat(found.getFavoriteSummoners()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(found.getFavoriteSummoners().stream()
            .anyMatch(s -> s.getName().equals("FavoriteSummoner"))).isTrue();
    }

    @Test
    void testUserRepository_CanRemoveFavorites() {
        Summoner favorite = new Summoner();
        favorite.setName("ToRemove");
        favorite.setPuuid("remove-puuid");
        favorite.setRiotId("ToRemove#EUW");
        favorite = summonerRepository.save(favorite);
        summonerRepository.flush();
        
        // Refresh to ensure collection is mutable
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        testUser.addFavoriteSummoner(favorite);
        testUser = userRepository.save(testUser);
        userRepository.flush();
        
        // Refresh again before removal
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        testUser.removeFavoriteSummoner(favorite);
        testUser = userRepository.save(testUser);
        userRepository.flush();
        
        UserModel found = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(found).isNotNull();
        assertThat(found.getFavoriteSummoners()).isEmpty();
    }

    @Test
    void testSummonerRepository_UpdatesLastSearchedAt() {
        Summoner summoner = new Summoner();
        summoner.setName("SearchedSummoner");
        summoner.setPuuid("searched-puuid");
        summoner.setRiotId("SearchedSummoner#EUW");
        java.time.LocalDateTime firstSearch = java.time.LocalDateTime.now();
        summoner.setLastSearchedAt(firstSearch);
        summoner = summonerRepository.save(summoner);
        
        // Update with a later timestamp
        java.time.LocalDateTime secondSearch = firstSearch.plusSeconds(1);
        summoner.setLastSearchedAt(secondSearch);
        summoner = summonerRepository.save(summoner);
        
        assertThat(summoner.getLastSearchedAt()).isAfter(firstSearch);
    }

    @Test
    void testSummonerRepository_StoresRankInformation() {
        Summoner summoner = new Summoner();
        summoner.setName("RankedSummoner");
        summoner.setPuuid("ranked-puuid");
        summoner.setRiotId("RankedSummoner#EUW");
        summoner.setTier("GOLD");
        summoner.setRank("II");
        summoner.setLp(45);
        summoner.setWins(100);
        summoner.setLosses(95);
        
        summonerRepository.save(summoner);
        
        Summoner found = summonerRepository.findByPuuid("ranked-puuid").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getTier()).isEqualTo("GOLD");
        assertThat(found.getRank()).isEqualTo("II");
        assertThat(found.getLp()).isEqualTo(45);
        assertThat(found.getWins()).isEqualTo(100);
        assertThat(found.getLosses()).isEqualTo(95);
    }
}
