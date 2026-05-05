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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.tfg.service.storage.MinioStorageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RiotService and related repositories.
 */
@ActiveProfiles("test")
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

        testUser = new UserModel("testuser", "password", "USER");
        testUser = userRepository.save(testUser);
    }

    @Test
    void contextLoads() {
        assertThat(riotService).isNotNull();
        assertThat(summonerRepository).isNotNull();
    }

    @Test
    void testGetDataDragonServiceReturnsInstance() {
        assertThat(riotService.getDataDragonService()).isNotNull();
    }

    @Test
    void testSummonerRepositoryCoreOperations() {

        Summoner summoner = new Summoner();
        summoner.setName("TestSummoner");
        summoner.setPuuid("unique-puuid-123");
        summoner.setRiotId("TestSummoner#EUW");
        summoner.setLevel(30);
        Summoner saved = summonerRepository.save(summoner);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("TestSummoner");

        Summoner foundByPuuid = summonerRepository.findByPuuid("unique-puuid-123").orElse(null);
        assertThat(foundByPuuid).isNotNull();

        Summoner foundByName = summonerRepository.findByName("testsummoner").orElse(null);
        assertThat(foundByName).isNotNull();
        assertThat(foundByName.getName()).isEqualTo("TestSummoner");
    }

    @Test
    void testSummonerRepositoryNoDuplicatePuuid() {
        Summoner summoner1 = new Summoner();
        summoner1.setName("Summoner1");
        summoner1.setPuuid("duplicate-puuid");
        summoner1.setRiotId("Summoner1#EUW");
        summonerRepository.save(summoner1);

        Summoner summoner2 = new Summoner();
        summoner2.setName("Summoner2");
        summoner2.setPuuid("duplicate-puuid");
        summoner2.setRiotId("Summoner2#EUW");

        assertThrows(Exception.class, () -> {
            summonerRepository.save(summoner2);
            summonerRepository.flush();
        });
    }

    @Test
    void testUserRepositoryCanLinkSummoner() {
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
    void testUserRepositoryFavoritesFlow() {

        Summoner favorite = new Summoner();
        favorite.setName("FavoriteSummoner");
        favorite.setPuuid("favorite-puuid");
        favorite.setRiotId("FavoriteSummoner#EUW");
        favorite = summonerRepository.save(favorite);
        summonerRepository.flush();

        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        testUser.addFavoriteSummoner(favorite);
        testUser = userRepository.save(testUser);
        userRepository.flush();

        UserModel foundWithFav = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(foundWithFav).isNotNull();
        assertThat(foundWithFav.getFavoriteSummoners()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(foundWithFav.getFavoriteSummoners().stream()
                .anyMatch(s -> s.getName().equals("FavoriteSummoner"))).isTrue();

        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        testUser.removeFavoriteSummoner(favorite);
        testUser = userRepository.save(testUser);
        userRepository.flush();

        UserModel foundWithoutFav = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(foundWithoutFav).isNotNull();
        assertThat(foundWithoutFav.getFavoriteSummoners().stream()
                .noneMatch(s -> s.getName().equals("FavoriteSummoner"))).isTrue();
    }

    @Test
    void testSummonerRepositoryUpdatesLastSearchedAt() {
        Summoner summoner = new Summoner();
        summoner.setName("SearchedSummoner");
        summoner.setPuuid("searched-puuid");
        summoner.setRiotId("SearchedSummoner#EUW");
        java.time.LocalDateTime firstSearch = java.time.LocalDateTime.now();
        summoner.setLastSearchedAt(firstSearch);
        summoner = summonerRepository.save(summoner);

        java.time.LocalDateTime secondSearch = firstSearch.plusSeconds(1);
        summoner.setLastSearchedAt(secondSearch);
        summoner = summonerRepository.save(summoner);

        assertThat(summoner.getLastSearchedAt()).isAfter(firstSearch);
    }

    @Test
    void testSummonerRepositoryStoresRankInformation() {
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
