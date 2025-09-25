package com.tfg.tfg.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tfg.tfg.model.entity.ChampionStat;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.ChampionStatRepository;
import com.tfg.tfg.repository.MatchRepository;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.repository.UserModelRepository;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserModelRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SummonerRepository summonerRepo;
    @Autowired
    private MatchRepository matchRepo;
    @Autowired
    private ChampionStatRepository champRepo;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Create admin user if not exists
        if (userRepository.findByName("admin").isEmpty()) {
            UserModel admin = new UserModel("admin", passwordEncoder.encode("admin123"), "ADMIN");
            admin.setEmail("admin@example.com");
            userRepository.save(admin);
            System.out.println("Created default admin user: admin / admin123");
        }

        // Create regular user if not exists
        if (userRepository.findByName("user").isEmpty()) {
            UserModel user = new UserModel("user", passwordEncoder.encode("user123"), "USER");
            user.setEmail("user@example.com");
            user.setActive(true);
            userRepository.save(user);
            System.out.println("Created default user: user / user123");
        }

        if (summonerRepo.count() > 0) {
            return; // already initialized
        }

        Summoner s1 = new Summoner("riot-1", "AlphaPlayer", 120);
        s1.setTier("Gold");
        s1.setRank("II");
        s1.setLp(50);

        Summoner s2 = new Summoner("riot-2", "BetaSummoner", 85);
        s2.setTier("Silver");
        s2.setRank("IV");
        s2.setLp(10);

        Summoner s3 = new Summoner("riot-3", "GammaGamer", 200);
        s3.setTier("Platinum");
        s3.setRank("I");
        s3.setLp(95);

        summonerRepo.saveAll(List.of(s1, s2, s3));

        MatchEntity m1 = new MatchEntity();
        m1.setMatchId("m-1001");
        m1.setTimestamp(LocalDateTime.now().minusDays(1));
        m1.setWin(true);
        m1.setKills(10);
        m1.setDeaths(2);
        m1.setAssists(5);
        m1.setSummoner(s1);

        MatchEntity m2 = new MatchEntity();
        m2.setMatchId("m-1002");
        m2.setTimestamp(LocalDateTime.now().minusDays(2));
        m2.setWin(false);
        m2.setKills(3);
        m2.setDeaths(7);
        m2.setAssists(4);
        m2.setSummoner(s1);

        matchRepo.saveAll(List.of(m1, m2));

        ChampionStat c1 = new ChampionStat();
        c1.setChampionId(157); // example champion id
        c1.setGamesPlayed(20);
        c1.setWins(12);
        c1.setKills(150);
        c1.setDeaths(80);
        c1.setAssists(100);
        c1.setSummoner(s1);

        champRepo.save(c1);

        // Associate lists to summoner and save
        s1.setMatches(List.of(m1, m2));
        s1.setChampionStats(List.of(c1));
        summonerRepo.save(s1);

        System.out.println("DatabaseInitializer: sample data inserted (dev profile)");
    }
}
