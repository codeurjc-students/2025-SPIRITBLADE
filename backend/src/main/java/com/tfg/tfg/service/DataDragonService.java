package com.tfg.tfg.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.tfg.model.entity.Champion;
import com.tfg.tfg.repository.ChampionRepository;

import java.util.Optional;

/**
 * Service to interact with Riot's Data Dragon static data API
 * Data Dragon provides champion names, images, and other static game data
 */
@Service
public class DataDragonService {

    private static final Logger logger = LoggerFactory.getLogger(DataDragonService.class);

    // Data Dragon version - update periodically or fetch dynamically
    private static final String DATA_DRAGON_VERSION = "15.22.1";
    private static final String DATA_DRAGON_BASE_URL = "https://ddragon.leagueoflegends.com";
    private static final String CDN_BASE = DATA_DRAGON_BASE_URL + "/cdn/" + DATA_DRAGON_VERSION;
    private static final String CHAMPION_JSON_URL = CDN_BASE + "/data/en_US/champion.json";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChampionRepository championRepository;

    public DataDragonService(ChampionRepository championRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.championRepository = championRepository;
    }

    /**
     * Update champion data in the database from Data Dragon
     * This should be called on application startup
     */
    @Transactional
    public void updateChampionDatabase() {
        try {
            logger.info("Updating champion database from Data Dragon...");

            String jsonResponse = restTemplate.getForObject(CHAMPION_JSON_URL, String.class);
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode championsData = root.get("data");

            if (championsData != null && championsData.isObject()) {
                int count = 0;
                // We can't use forEachRemaining with a non-final variable inside lambda easily,
                // so we use an iterator loop or just a simple counter wrapper if needed.
                // Here we just iterate.
                var fields = championsData.fields();
                while (fields.hasNext()) {
                    var entry = fields.next();
                    String championKey = entry.getKey(); // e.g., "Aatrox"
                    JsonNode championInfo = entry.getValue();

                    String name = championInfo.get("name").asText(); // e.g., "Aatrox"
                    long id = Long.parseLong(championInfo.get("key").asText()); // e.g., "266" -> 266

                    String imageUrl = CDN_BASE + "/img/champion/" + championKey + ".png";

                    Champion champion = new Champion(id, championKey, name, imageUrl);
                    championRepository.save(champion);
                    count++;
                }

                logger.info("Successfully updated {} champions in database", count);
            } else {
                logger.warn("No champion data found in Data Dragon response");
            }

        } catch (Exception e) {
            logger.error("Failed to update champion database: {}", e.getMessage());
            logger.debug("Stacktrace:", e);
        }
    }

    /**
     * Get champion name by champion ID
     * 
     * @param championId Numeric champion ID from Riot API
     * @return Champion name or "Unknown Champion" if not found
     */
    @Cacheable(value = "champions", key = "#championId")
    public String getChampionNameById(Long championId) {
        if (championId == null) {
            return "Unknown Champion";
        }
        return championRepository.findById(championId)
                .map(Champion::getName)
                .orElse("Champion " + championId);
    }

    /**
     * Get champion icon URL by champion ID
     * 
     * @param championId Numeric champion ID
     * @return URL to champion icon image
     */
    @Cacheable(value = "champions", key = "'icon_' + #championId")
    public String getChampionIconUrl(Long championId) {
        if (championId == null)
            return "";

        return championRepository.findById(championId)
                .map(Champion::getImageUrl)
                .orElse("");
    }

    /**
     * Get the URL for a summoner's profile icon
     * 
     * @param profileIconId The profile icon ID from summoner data
     * @return URL to the profile icon image
     */
    public String getProfileIconUrl(Integer profileIconId) {
        if (profileIconId == null) {
            return "";
        }
        return CDN_BASE + "/img/profileicon/" + profileIconId + ".png";
    }
}
