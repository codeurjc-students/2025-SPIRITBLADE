package com.tfg.tfg.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.MatchRepository;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.repository.UserModelRepository;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime; 

@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final String ADMIN_USERNAME = "admin";
    private static final String USER_USERNAME = "user";
    
    private final UserModelRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SummonerRepository summonerRepo;
    private final MatchRepository matchRepository;

    public DataInitializer(UserModelRepository userRepository, 
                          PasswordEncoder passwordEncoder,
                          SummonerRepository summonerRepo,
                          MatchRepository matchRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.summonerRepo = summonerRepo;
        this.matchRepository = matchRepository;
    }

    @PostConstruct
    public void init() {
        // Generate secure default passwords for development (should be externalized in production)
        String adminPassword = System.getenv("ADMIN_DEFAULT_PASSWORD");
        if (adminPassword == null) {
            adminPassword = generateSecurePassword(ADMIN_USERNAME);
        }
        
        String userPassword = System.getenv("USER_DEFAULT_PASSWORD");
        if (userPassword == null) {
            userPassword = generateSecurePassword(USER_USERNAME);
        }
        
        // Create admin user if not exists
        if (userRepository.findByName(ADMIN_USERNAME).isEmpty()) {
            UserModel admin = new UserModel(ADMIN_USERNAME, passwordEncoder.encode(adminPassword), "ADMIN");
            admin.setEmail("admin@example.com");
            userRepository.save(admin);
            logger.info("Created default admin user: {} / [password from env or generated]", ADMIN_USERNAME);
        }

        // Create regular user if not exists
        if (userRepository.findByName(USER_USERNAME).isEmpty()) {
            UserModel user = new UserModel(USER_USERNAME, passwordEncoder.encode(userPassword), "USER");
            user.setEmail("user@example.com");
            user.setActive(true);
            userRepository.save(user);
            logger.info("Created default user: {} / [password from env or generated]", USER_USERNAME);
        }

        
        if (summonerRepo.count() > 0) {
            return; // already initialized
        }
        

        Summoner s1 = new Summoner("riot-1", "AlphaPlayer#EUW", 120);
        s1.setTier("Gold");
        s1.setRank("II");
        s1.setLp(50);
        s1.setWins(153);
        s1.setLosses(115);

        Summoner s2 = new Summoner("riot-2", "BetaSummoner#NA1", 85);
        s2.setTier("Silver");
        s2.setRank("IV");
        s2.setLp(10);
        s2.setWins(45);
        s2.setLosses(52);

        Summoner s3 = new Summoner("riot-3", "GammaGamer#KR", 200);
        s3.setTier("Platinum");
        s3.setRank("I");
        s3.setLp(95);
        s3.setWins(87);
        s3.setLosses(73);

        summonerRepo.saveAll(List.of(s1, s2, s3));

        logger.info("DatabaseInitializer: sample summoners inserted (dev profile)");
        logger.info("Note: Match and champion stats are fetched from Riot API in real-time");
        
        // Initialize ranked matches with LP progression data for charts
        initializeRankedMatches(s1, s2);
    }
    
    /**
     * Initialize sample ranked matches with LP progression data for chart visualization
     * Uses MatchEntity with tierAtMatch, rankAtMatch, lpAtMatch fields instead of separate RankHistory table
     */
    private void initializeRankedMatches(Summoner s1, Summoner s2) {
        if (matchRepository.count() > 0) {
            logger.info("Matches already initialized, skipping...");
            return;
        }
        
        logger.info("Initializing sample ranked matches for LP progression chart...");
        
        LocalDateTime now = LocalDateTime.now();
        List<MatchEntity> matches = new ArrayList<>();
        int matchIdCounter = 1000;
        
        // Summoner 1: Gold IV -> Gold II progression over 30 days (17 ranked matches)
        // Week 1: Gold IV
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(30), true, "GOLD", "IV", 15, 10, 2, 5));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(28), true, "GOLD", "IV", 28, 8, 1, 7));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(26), true, "GOLD", "IV", 45, 12, 3, 6));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(24), true, "GOLD", "IV", 62, 9, 2, 8));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(22), true, "GOLD", "IV", 78, 11, 1, 9));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(20), true, "GOLD", "IV", 95, 7, 0, 12));
        
        // Week 2: Promotion to Gold III
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(18), true, "GOLD", "III", 0, 13, 2, 7));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(16), true, "GOLD", "III", 18, 10, 3, 8));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(14), true, "GOLD", "III", 35, 14, 1, 6));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(12), true, "GOLD", "III", 52, 9, 2, 10));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(10), false, "GOLD", "III", 45, 5, 6, 4));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(8), true, "GOLD", "III", 62, 11, 1, 7));
        
        // Week 3-4: Promotion to Gold II
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(6), true, "GOLD", "III", 78, 8, 2, 9));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(4), true, "GOLD", "III", 95, 12, 1, 8));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(2), true, "GOLD", "II", 0, 10, 2, 11));
        matches.add(createRankedMatch(s1, matchIdCounter++, now.minusDays(1), true, "GOLD", "II", 22, 13, 1, 6));
        matches.add(createRankedMatch(s1, matchIdCounter++, now, true, "GOLD", "II", 42, 9, 2, 7));
        
        // Summoner 2: Silver II -> Silver I progression (7 ranked matches)
        matches.add(createRankedMatch(s2, matchIdCounter++, now.minusDays(30), true, "SILVER", "II", 25, 8, 4, 6));
        matches.add(createRankedMatch(s2, matchIdCounter++, now.minusDays(25), true, "SILVER", "II", 42, 10, 3, 7));
        matches.add(createRankedMatch(s2, matchIdCounter++, now.minusDays(20), true, "SILVER", "II", 58, 7, 2, 9));
        matches.add(createRankedMatch(s2, matchIdCounter++, now.minusDays(15), true, "SILVER", "II", 75, 9, 1, 8));
        matches.add(createRankedMatch(s2, matchIdCounter++, now.minusDays(10), true, "SILVER", "II", 85, 11, 2, 5));
        matches.add(createRankedMatch(s2, matchIdCounter++, now.minusDays(5), true, "SILVER", "II", 95, 6, 3, 10));
        matches.add(createRankedMatch(s2, matchIdCounter++, now, true, "SILVER", "I", 15, 8, 2, 7));
        
        matchRepository.saveAll(matches);
        logger.info("Initialized {} ranked matches for LP progression chart", matches.size());
    }
    
    /**
     * Helper method to create a ranked match with LP data
     */
    private MatchEntity createRankedMatch(Summoner summoner, int matchId, LocalDateTime timestamp, 
                                         boolean win, String tier, String rank, int lp,
                                         int kills, int deaths, int assists) {
        MatchEntity match = new MatchEntity();
        match.setMatchId("EUW1_RANKED_" + matchId);
        match.setSummoner(summoner);
        match.setTimestamp(timestamp);
        match.setWin(win);
        match.setKills(kills);
        match.setDeaths(deaths);
        match.setAssists(assists);
        match.setGameMode("RANKED_SOLO_5x5");
        match.setGameDuration(1800L + (matchId % 600)); // 30-40 minutes
        match.setChampionName("TestChampion" + (matchId % 10));
        match.setChampionId(100 + (matchId % 50));
        match.setSummonerName(summoner.getName());
        match.setCachedAt(LocalDateTime.now());
        
        // Historical rank data for LP chart
        match.setTierAtMatch(tier);
        match.setRankAtMatch(rank);
        match.setLpAtMatch(lp);
        
        return match;
    }
    
    private String generateSecurePassword(String prefix) {
        // Generate a more secure password for development
        return prefix + "Secure" + System.currentTimeMillis() % 10000 + "!";
    }
}
