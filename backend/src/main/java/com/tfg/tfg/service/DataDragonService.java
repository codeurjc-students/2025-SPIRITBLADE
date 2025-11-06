package com.tfg.tfg.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to interact with Riot's Data Dragon static data API
 * Data Dragon provides champion names, images, and other static game data
 */
@Service
public class DataDragonService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataDragonService.class);
    
    // Data Dragon version - update periodically or fetch dynamically
    private static final String DATA_DRAGON_VERSION = "14.1.1";
    private static final String DATA_DRAGON_BASE_URL = "https://ddragon.leagueoflegends.com";
    private static final String CDN_BASE = DATA_DRAGON_BASE_URL + "/cdn/" + DATA_DRAGON_VERSION;
    private static final String CHAMPION_JSON_URL = CDN_BASE + "/data/en_US/champion.json";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Cache: championId -> championName
    private final Map<Long, String> championIdToName = new HashMap<>();
    // Cache: championKey (string ID) -> championName
    private final Map<String, String> championKeyToName = new HashMap<>();
    
    public DataDragonService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Load champion data on service initialization
     */
    @PostConstruct
    public void loadChampionData() {
        try {
            logger.info("Loading champion data from Data Dragon...");
            
            String jsonResponse = restTemplate.getForObject(CHAMPION_JSON_URL, String.class);
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode championsData = root.get("data");
            
            if (championsData != null && championsData.isObject()) {
                championsData.fields().forEachRemaining(entry -> {
                    String championKey = entry.getKey(); // e.g., "Aatrox"
                    JsonNode championInfo = entry.getValue();
                    
                    String name = championInfo.get("name").asText(); // e.g., "Aatrox"
                    long id = championInfo.get("key").asLong(); // e.g., 266
                    
                    championIdToName.put(id, name);
                    championKeyToName.put(championKey, name);
                });
                
                logger.info("Successfully loaded {} champions from Data Dragon", championIdToName.size());
            } else {
                logger.warn("No champion data found in Data Dragon response");
            }
            
        } catch (Exception e) {
            logger.error("Failed to load champion data from Data Dragon: {}", e.getMessage());
            logger.debug("Stacktrace:", e);
            // Service will continue but with empty champion data
        }
    }
    
    /**
     * Get champion name by champion ID
     * @param championId Numeric champion ID from Riot API
     * @return Champion name or "Unknown Champion" if not found
     */
    public String getChampionNameById(Long championId) {
        if (championId == null) {
            return "Unknown Champion";
        }
        return championIdToName.getOrDefault(championId, "Champion " + championId);
    }
    
    /**
     * Get champion name by champion key (string identifier)
     * @param championKey String champion key (e.g., "Aatrox")
     * @return Champion name or the key itself if not found
     */
    public String getChampionNameByKey(String championKey) {
        if (championKey == null) {
            return "Unknown Champion";
        }
        return championKeyToName.getOrDefault(championKey, championKey);
    }
    
    /**
     * Get champion icon URL by champion ID
     * @param championId Numeric champion ID
     * @return URL to champion icon image
     */
    public String getChampionIconUrl(Long championId) {
        String championName = championIdToName.get(championId);
        if (championName == null) {
            return "";
        }
        return CDN_BASE + "/img/champion/" + championName + ".png";
    }
    
    /**
     * Get the URL for a summoner's profile icon
     * @param profileIconId The profile icon ID from summoner data
     * @return URL to the profile icon image
     */
    public String getProfileIconUrl(Integer profileIconId) {
        if (profileIconId == null) {
            return "";
        }
        return CDN_BASE + "/img/profileicon/" + profileIconId + ".png";
    }
    
    /**
     * Check if champion data is loaded
     * @return true if champion data is available
     */
    public boolean isDataLoaded() {
        return !championIdToName.isEmpty();
    }
    
    /**
     * Get total number of champions loaded
     * @return number of champions
     */
    public int getChampionCount() {
        return championIdToName.size();
    }
}
