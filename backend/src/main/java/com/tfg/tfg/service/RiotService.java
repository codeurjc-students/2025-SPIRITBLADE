package com.tfg.tfg.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.tfg.tfg.exception.RiotApiException;
import com.tfg.tfg.exception.SummonerNotFoundException;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.MatchHistoryDTO;
import com.tfg.tfg.model.dto.riot.RiotAccountDTO;
import com.tfg.tfg.model.dto.riot.RiotSummonerDTO;
import com.tfg.tfg.model.dto.riot.RiotLeagueEntryDTO;
import com.tfg.tfg.model.dto.riot.RiotChampionMasteryDTO;
import com.tfg.tfg.model.dto.riot.RiotMatchDTO;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.mapper.SummonerMapper;
import com.tfg.tfg.repository.SummonerRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

@Service
public class RiotService {
    
    private static final Logger logger = LoggerFactory.getLogger(RiotService.class);
    private static final String STACKTRACE_LOG_MESSAGE = "Stacktrace:";
    
    @Value("${riot.api.key}")
    private String apiKey;
    
    private static final String RIOT_API_BASE_URL = "https://euw1.api.riotgames.com";
    private static final String RIOT_REGIONAL_BASE_URL = "https://europe.api.riotgames.com";
    
    // Account-v1: Get PUUID by Riot ID (gameName#tagLine)
    private static final String ACCOUNT_BY_RIOT_ID_URL = RIOT_REGIONAL_BASE_URL + "/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}?api_key={apiKey}";
    
    // Summoner-v4: Get summoner data by PUUID
    private static final String SUMMONER_BY_PUUID_URL = RIOT_API_BASE_URL + "/lol/summoner/v4/summoners/by-puuid/{encryptedPUUID}?api_key={apiKey}";
    
    // League-v4: Get league entries by PUUID
    private static final String LEAGUE_BY_PUUID_URL = RIOT_API_BASE_URL + "/lol/league/v4/entries/by-puuid/{encryptedPUUID}?api_key={apiKey}";
    
    // Champion-Mastery-v4: Get top champion masteries by PUUID
    private static final String CHAMPION_MASTERY_BY_PUUID_URL = RIOT_API_BASE_URL + "/lol/champion-mastery/v4/champion-masteries/by-puuid/{encryptedPUUID}/top?count={count}&api_key={apiKey}";
    
    // Match-v5: Get match IDs by PUUID (uses regional endpoint)
    private static final String MATCH_IDS_BY_PUUID_URL = RIOT_REGIONAL_BASE_URL + "/lol/match/v5/matches/by-puuid/{puuid}/ids?start={start}&count={count}&api_key={apiKey}";
    
    // Match-v5: Get match details by match ID (uses regional endpoint)
    private static final String MATCH_BY_ID_URL = RIOT_REGIONAL_BASE_URL + "/lol/match/v5/matches/{matchId}?api_key={apiKey}";
    
    private final SummonerRepository summonerRepository;
    private final com.tfg.tfg.repository.MatchEntityRepository matchRepository;
    private final DataDragonService dataDragonService;
    private final RestTemplate restTemplate;

    public RiotService(SummonerRepository summonerRepository, 
                      com.tfg.tfg.repository.MatchEntityRepository matchRepository,
                      DataDragonService dataDragonService) {
        this.summonerRepository = summonerRepository;
        this.matchRepository = matchRepository;
        this.dataDragonService = dataDragonService;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Exposes DataDragonService for use by controllers
     */
    public DataDragonService getDataDragonService() {
        return dataDragonService;
    }

    /**
     * Gets summoner data from Riot API by Riot ID (gameName#tagLine)
     * @param riotId Full Riot ID in format "gameName#tagLine" (e.g., "Player#EUW")
     * @return SummonerDTO with complete data from Riot API
     */
    public SummonerDTO getSummonerByName(String riotId) {
        try {
            // Parse Riot ID (gameName#tagLine)
            String[] parts = riotId.split("#");
            if (parts.length != 2) {
                logger.warn("Invalid Riot ID format: {}. Expected format: gameName#tagLine", riotId);
                // Try to find in database by name (fallback for old data)
                Optional<Summoner> found = summonerRepository.findByName(riotId);
                return found.map(this::mapSummonerEntityToDTO).orElse(null);
            }
            
            String gameName = parts[0];
            String tagLine = parts[1];
            
            // Step 1: Get PUUID from Account API
            ResponseEntity<RiotAccountDTO> accountResponse = restTemplate.exchange(
                ACCOUNT_BY_RIOT_ID_URL,
                HttpMethod.GET,
                null,
                RiotAccountDTO.class,
                gameName,
                tagLine,
                apiKey
            );
            
            RiotAccountDTO account = accountResponse.getBody();
            if (account == null || account.getPuuid() == null) {
                logger.warn("Account not found for Riot ID: {}", riotId);
                return null;
            }
            
            String puuid = account.getPuuid();
            
            // Step 2: Get summoner data by PUUID
            ResponseEntity<RiotSummonerDTO> summonerResponse = restTemplate.exchange(
                SUMMONER_BY_PUUID_URL,
                HttpMethod.GET,
                null,
                RiotSummonerDTO.class,
                puuid,
                apiKey
            );
            
            RiotSummonerDTO riotSummoner = summonerResponse.getBody();
            if (riotSummoner == null) {
                return null;
            }
            
            // Step 3: Get rank information by PUUID
            ResponseEntity<RiotLeagueEntryDTO[]> leagueResponse = restTemplate.exchange(
                LEAGUE_BY_PUUID_URL,
                HttpMethod.GET,
                null,
                RiotLeagueEntryDTO[].class,
                puuid,
                apiKey
            );
            
            // Find RANKED_SOLO_5x5 entry
            RiotLeagueEntryDTO rankedEntry = null;
            if (leagueResponse.getBody() != null) {
                rankedEntry = Arrays.stream(leagueResponse.getBody())
                    .filter(entry -> "RANKED_SOLO_5x5".equals(entry.getQueueType()))
                    .findFirst()
                    .orElse(null);
            }
            
            // Map to SummonerDTO
            SummonerDTO dto = new SummonerDTO();
            dto.setRiotId(riotSummoner.getId());
            dto.setPuuid(puuid);
            dto.setName(riotId); // Store full Riot ID (gameName#tagLine)
            dto.setLevel(riotSummoner.getSummonerLevel());
            dto.setProfileIconId(riotSummoner.getProfileIconId());
            dto.setProfileIconUrl(dataDragonService.getProfileIconUrl(riotSummoner.getProfileIconId()));
            
            if (rankedEntry != null) {
                dto.setTier(rankedEntry.getTier());
                dto.setRank(rankedEntry.getRank());
                dto.setLp(rankedEntry.getLeaguePoints());
                dto.setWins(rankedEntry.getWins());
                dto.setLosses(rankedEntry.getLosses());
            } else {
                dto.setTier("UNRANKED");
                dto.setRank("");
                dto.setLp(0);
                dto.setWins(0);
                dto.setLosses(0);
            }
            
            // Save to database for caching
            saveSummonerToDatabase(dto);
            
            return dto;
            
        } catch (HttpClientErrorException.NotFound e) {
            // Summoner not found in Riot API
            logger.warn("Summoner not found in Riot API: {}", riotId);
            throw new SummonerNotFoundException("Summoner '" + riotId + "' not found in Riot API");
        } catch (HttpClientErrorException e) { // NOSONAR - Exception is logged and rethrown with context
            // Handled: Log with context, try database fallback, then rethrow with specific context
            logger.error("Riot API error for summoner {}: {} (Status: {})", riotId, e.getMessage(), e.getStatusCode().value(), e);
            Optional<Summoner> found = summonerRepository.findByName(riotId);
            if (found.isPresent()) {
                logger.info("Returning cached data for summoner: {}", riotId);
                return mapSummonerEntityToDTO(found.get());
            }
            throw new RiotApiException("Riot API is currently unavailable for summoner '" + riotId + "'. Status: " + e.getStatusCode(), e.getStatusCode().value());
        } catch (Exception e) { // NOSONAR - Exception is logged and rethrown with context
            // Handled: Log unexpected error, try database fallback, then rethrow with context
            logger.error("Unexpected error fetching summoner {}: {}", riotId, e.getMessage());
            logger.debug(STACKTRACE_LOG_MESSAGE, e);
            Optional<Summoner> found = summonerRepository.findByName(riotId);
            if (found.isPresent()) {
                return mapSummonerEntityToDTO(found.get());
            }
            throw new RiotApiException("Unexpected error while fetching summoner data: " + e.getMessage(), 500);
        }
    }
    
    /**
     * Saves summoner data to database for caching
     * Uses PUUID as the unique identifier to prevent duplicates
     */
    private void saveSummonerToDatabase(SummonerDTO dto) {
        try {
            if (dto.getPuuid() == null || dto.getPuuid().isEmpty()) {
                logger.warn("Cannot save summoner without PUUID: {}", dto.getName());
                return;
            }
            
            // Search by PUUID (unique identifier) instead of name
            Optional<Summoner> existing = summonerRepository.findByPuuid(dto.getPuuid());
            Summoner summoner;
            
            if (existing.isPresent()) {
                summoner = existing.get();
                logger.debug("Updating existing summoner with PUUID: {}", dto.getPuuid());
            } else {
                // Use mapper to create entity from DTO (keeps mapping centralised)
                summoner = SummonerMapper.toEntity(dto);
                logger.debug("Creating new summoner with PUUID: {}", dto.getPuuid());
            }
            
            // Update fields from DTO on existing entity (only fields we care to persist)
            summoner.setRiotId(dto.getRiotId());
            summoner.setPuuid(dto.getPuuid());
            summoner.setName(dto.getName());
            summoner.setLevel(dto.getLevel());
            summoner.setProfileIconId(dto.getProfileIconId());
            summoner.setTier(dto.getTier());
            summoner.setRank(dto.getRank());
            summoner.setLp(dto.getLp());
            summoner.setWins(dto.getWins());
            summoner.setLosses(dto.getLosses());
            summoner.setLastSearchedAt(java.time.LocalDateTime.now());
            
            summonerRepository.save(summoner);
        } catch (org.springframework.dao.DataAccessException dae) {
            // Database-related exception: log at warn and return
            logger.warn("Failed to cache summoner data (DB error): {}", dae.getMessage());
            logger.debug(STACKTRACE_LOG_MESSAGE, dae);
        } catch (Exception e) {
            // Generic fallback: log and swallow - caching failure should not affect main operation
            logger.warn("Failed to cache summoner data for {}: {}", dto.getName(), e.getMessage(), e);
            logger.debug(STACKTRACE_LOG_MESSAGE, e);
        }
    }
    
    /**
     * Maps Summoner entity to SummonerDTO
     */
    private SummonerDTO mapSummonerEntityToDTO(Summoner summoner) {
        return SummonerDTO.fromEntity(summoner, dataDragonService);
    }
    
    /**
     * Gets top champion masteries for a summoner by PUUID
     * @param puuid Summoner's PUUID
     * @param count Number of champions to retrieve (default 3)
     * @return List of RiotChampionMasteryDTO with championName populated
     */
    public List<RiotChampionMasteryDTO> getTopChampionMasteries(String puuid, int count) {
        try {
            ResponseEntity<RiotChampionMasteryDTO[]> response = restTemplate.exchange(
                CHAMPION_MASTERY_BY_PUUID_URL,
                HttpMethod.GET,
                null,
                RiotChampionMasteryDTO[].class,
                puuid,
                count,
                apiKey
            );
            
            RiotChampionMasteryDTO[] masteries = response.getBody();
            if (masteries == null || masteries.length == 0) {
                return Collections.emptyList();
            }
            
            // Enrich with champion names and icon URLs from Data Dragon
            return Arrays.stream(masteries)
                .map(mastery -> {
                    mastery.setChampionName(
                        dataDragonService.getChampionNameById(mastery.getChampionId())
                    );
                    mastery.setChampionIconUrl(
                        dataDragonService.getChampionIconUrl(mastery.getChampionId())
                    );
                    return mastery;
                })
                .toList();
                
        } catch (HttpClientErrorException hce) {
            logger.warn("Riot API error fetching champion masteries for PUUID {}: {}", puuid, hce.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            // Fully handled: log error and return empty list as fallback
            logger.error("Error fetching champion masteries for PUUID {}: {}", puuid, e.getMessage(), e);
            logger.debug(STACKTRACE_LOG_MESSAGE, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Fetches a single match from Riot API, adds it to the match history list,
     * and saves it to the database for future cache hits.
     * 
     * @param matchId The match ID to fetch
     * @param puuid The player's PUUID
     * @param matches The list to add the match to
     */
    private void fetchAndAddMatchToHistory(String matchId, String puuid, List<MatchHistoryDTO> matches) {
        try {
            ResponseEntity<RiotMatchDTO> matchResponse = restTemplate.exchange(
                MATCH_BY_ID_URL,
                HttpMethod.GET,
                null,
                RiotMatchDTO.class,
                matchId,
                apiKey
            );
            
            RiotMatchDTO match = matchResponse.getBody();
            if (match != null && match.getInfo() != null) {
                MatchHistoryDTO matchDTO = mapToMatchHistoryDTO(match, puuid);
                if (matchDTO != null) {
                    matches.add(matchDTO);
                    
                    // Save match to database for future cache hits
                    saveMatchToDatabase(match, puuid);
                }
            }
        } catch (HttpClientErrorException hce) {
            logger.warn("Riot API error fetching match {}: {}", matchId, hce.getMessage());
        } catch (Exception e) {
            // Fully handled: log and continue - single match failure shouldn't stop processing
            logger.warn("Error fetching match {}: {}", matchId, e.getMessage(), e);
            logger.debug(STACKTRACE_LOG_MESSAGE, e);
        }
    }
    
    /**
     * Saves match data to database for caching.
     * Prevents duplicate API calls for historical matches.
     * 
     * @param riotMatch The match data from Riot API
     * @param puuid The player's PUUID to find the correct participant data
     */
    private void saveMatchToDatabase(RiotMatchDTO riotMatch, String puuid) {
        try {
            // Find the summoner
            Optional<Summoner> summonerOpt = summonerRepository.findByPuuid(puuid);
            if (summonerOpt.isEmpty()) {
                logger.debug("Summoner not found for PUUID {}, skipping match cache", puuid);
                return;
            }
            
            String matchId = riotMatch.getMetadata() != null ? riotMatch.getMetadata().getMatchId() : null;
            if (matchId == null) {
                logger.warn("Match ID is null, cannot cache match");
                return;
            }
            
            // Check if match already exists
            if (matchRepository.findByMatchId(matchId).isPresent()) {
                logger.debug("Match {} already in cache, skipping", matchId);
                return;
            }
            
            // Find participant data for this player
            RiotMatchDTO.ParticipantDTO participant = findParticipantByPuuid(riotMatch, puuid);
            if (participant == null) {
                logger.warn("Participant not found for PUUID {} in match {}", puuid, matchId);
                return;
            }
            
            // Create and populate MatchEntity
            com.tfg.tfg.model.entity.MatchEntity matchEntity = new com.tfg.tfg.model.entity.MatchEntity();
            matchEntity.setMatchId(matchId);
            matchEntity.setSummoner(summonerOpt.get());
            matchEntity.setTimestamp(java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(riotMatch.getInfo().getGameCreation()),
                java.time.ZoneId.systemDefault()
            ));
            matchEntity.setWin(Boolean.TRUE.equals(participant.getWin()));
            matchEntity.setKills(participant.getKills() != null ? participant.getKills() : 0);
            matchEntity.setDeaths(participant.getDeaths() != null ? participant.getDeaths() : 0);
            matchEntity.setAssists(participant.getAssists() != null ? participant.getAssists() : 0);
            matchEntity.setChampionName(participant.getChampionName());
            matchEntity.setChampionId(participant.getChampionId());
            matchEntity.setRole(participant.getTeamPosition());  // teamPosition is the role
            matchEntity.setLane(participant.getTeamPosition());   // Using teamPosition as lane
            matchEntity.setGameDuration(riotMatch.getInfo().getGameDuration());
            matchEntity.setGameMode(riotMatch.getInfo().getGameMode());
            matchEntity.setQueueId(riotMatch.getInfo().getQueueId());  // Save queueId for filtering
            matchEntity.setTotalDamageDealt(participant.getTotalDamageDealtToChampions());
            matchEntity.setGoldEarned(participant.getGoldEarned());
            matchEntity.setChampLevel(participant.getChampLevel());
            matchEntity.setSummonerName(participant.getSummonerName());
            matchEntity.setCachedAt(java.time.LocalDateTime.now());
            
            // DO NOT set LP here - it will be calculated correctly by DashboardController.saveMatchesToDatabase()
            // which calculates historical LP progression working backwards from current LP
            // Setting it here would give all matches the same current LP, making charts flat
            matchEntity.setLpAtMatch(null);
            matchEntity.setTierAtMatch(null);
            matchEntity.setRankAtMatch(null);
            
            matchRepository.save(matchEntity);
            logger.debug("Saved match {} to database cache (LP will be calculated on first ranked matches request)", matchId);
            
        } catch (Exception e) {
            // Fully handled: log and swallow - match caching failure shouldn't stop processing
            logger.warn("Failed to save match to database for PUUID {}: {}", puuid, e.getMessage(), e);
            logger.debug(STACKTRACE_LOG_MESSAGE, e);
        }
    }
    
    /**
     * Converts cached MatchEntity to MatchHistoryDTO.
     * 
     * @param match The cached match entity
     * @return MatchHistoryDTO with match data
     */
    private MatchHistoryDTO convertMatchEntityToDTO(com.tfg.tfg.model.entity.MatchEntity match) {
        MatchHistoryDTO dto = new MatchHistoryDTO();
        dto.setMatchId(match.getMatchId());
        dto.setChampionName(match.getChampionName());
        dto.setWin(match.isWin());
        dto.setKills(match.getKills());
        dto.setDeaths(match.getDeaths());
        dto.setAssists(match.getAssists());
        dto.setGameDuration(match.getGameDuration());
        dto.setGameTimestamp(match.getTimestamp().atZone(java.time.ZoneId.systemDefault())
            .toInstant().getEpochSecond());
        dto.setQueueId(match.getQueueId());
        dto.setLpAtMatch(match.getLpAtMatch());  // Include saved LP for historical tracking
        
        // Enrich with champion icon from DataDragon
        if (match.getChampionId() != null) {
            try {
                dto.setChampionIconUrl(dataDragonService.getChampionIconUrl(match.getChampionId().longValue()));
            } catch (Exception e) {
                // Fully handled: log and continue - icon fetch failure is non-critical
                logger.warn("Could not get champion icon for championId {}: {}", match.getChampionId(), e.getMessage(), e);
            }
        }
        
        return dto;
    }
    
    /**
     * Finds a participant in a match by their PUUID.
     * 
     * @param riotMatch The match data from Riot API
     * @param puuid The player's PUUID
     * @return The participant data, or null if not found
     */
    private RiotMatchDTO.ParticipantDTO findParticipantByPuuid(RiotMatchDTO riotMatch, String puuid) {
        if (riotMatch.getInfo() == null || riotMatch.getInfo().getParticipants() == null) {
            return null;
        }
        
        for (RiotMatchDTO.ParticipantDTO participant : riotMatch.getInfo().getParticipants()) {
            if (puuid.equals(participant.getPuuid())) {
                return participant;
            }
        }
        
        return null;
    }
    
    /**
     * Gets recent match history for a summoner by PUUID with pagination.
     * HYBRID CACHE STRATEGY:
     * 1. First checks database for cached matches
     * 2. Only calls Riot API for missing/recent matches
     * 3. Saves new matches to database for future requests
     * 
     * @param puuid Summoner's PUUID
     * @param start Starting index for pagination (0-based)
     * @param count Number of matches to retrieve per page (default 5)
     * @return List of MatchHistoryDTO
     */
    public List<MatchHistoryDTO> getMatchHistory(String puuid, int start, int count) {
        try {
            logger.info("Fetching match history for PUUID: {}, start: {}, count: {}", puuid, start, count);
            
            // STEP 1: Verify cache freshness - check if there are new matches
            if (!isCacheFresh(puuid)) {
                logger.info("Cache is outdated, need to fetch new matches from API");
                // Cache is outdated, proceed to fetch from API
            } else {
                // STEP 2: Cache is fresh, try to use it
                List<MatchHistoryDTO> cachedMatches = getCachedMatches(puuid, start, count);
                if (cachedMatches.size() >= count) {
                    logger.info("Returning {} matches from fresh cache (no API call needed)", count);
                    return cachedMatches;
                }
            }
            
            // STEP 3: Get match IDs from Riot API (cache outdated or insufficient)
            String[] matchIds = fetchMatchIdsFromAPI(puuid, start, count);
            if (matchIds == null || matchIds.length == 0) {
                // Fallback to cached matches if API returns nothing
                List<MatchHistoryDTO> cachedMatches = getCachedMatches(puuid, start, count);
                return cachedMatches.isEmpty() ? Collections.emptyList() : cachedMatches;
            }
            
            // STEP 4: Get match details (from cache or API)
            return fetchMatchDetails(matchIds, puuid);
            
        } catch (HttpClientErrorException hce) {
            logger.warn("Riot API error fetching match history for PUUID {}: {}", puuid, hce.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error fetching match history for PUUID {}: {}", puuid, e.getMessage());
            logger.debug(STACKTRACE_LOG_MESSAGE, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Verifies if the cache is fresh by comparing the most recent match ID 
     * in the database with the most recent match ID from Riot API.
     * This method makes a lightweight API call (only 1 match ID).
     * 
     * @param puuid The player's PUUID
     * @return true if cache is up-to-date, false if there are new matches
     */
    private boolean isCacheFresh(String puuid) {
        try {
            // Get the summoner by PUUID from database
            Optional<Summoner> summonerOpt = summonerRepository.findByPuuid(puuid);
            if (summonerOpt.isEmpty()) {
                logger.debug("No summoner found in DB for PUUID {}, cache not fresh", puuid);
                return false; // No cache exists
            }
            
            Summoner summoner = summonerOpt.get();
            List<com.tfg.tfg.model.entity.MatchEntity> recentMatches = matchRepository
                .findRecentMatchesBySummoner(summoner, 
                    org.springframework.data.domain.PageRequest.of(0, 1));
            
            if (recentMatches.isEmpty()) {
                logger.debug("No cached matches found for summoner {}, cache not fresh", summoner.getName());
                return false; // No cached matches
            }
            
            String mostRecentCachedMatchId = recentMatches.get(0).getMatchId();
            logger.debug("Most recent cached match ID: {}", mostRecentCachedMatchId);
            
            // Get only the most recent match ID from Riot API (lightweight call)
            ResponseEntity<String[]> matchIdsResponse = restTemplate.exchange(
                MATCH_IDS_BY_PUUID_URL,
                HttpMethod.GET,
                null,
                String[].class,
                puuid,
                0,  // start at 0
                1,  // only get 1 match ID
                apiKey
            );
            
            String[] apiMatchIds = matchIdsResponse.getBody();
            if (apiMatchIds == null || apiMatchIds.length == 0) {
                logger.debug("No matches returned from API, using cache");
                return true; // API has no matches, cache is valid
            }
            
            String mostRecentApiMatchId = apiMatchIds[0];
            logger.debug("Most recent API match ID: {}", mostRecentApiMatchId);
            
            // Compare: if IDs match, cache is fresh
            boolean isFresh = mostRecentCachedMatchId.equals(mostRecentApiMatchId);
            logger.info("Cache freshness check: {}", isFresh ? "FRESH ✅" : "OUTDATED ⚠️");
            return isFresh;
            
        } catch (Exception e) {
            logger.warn("Error checking cache freshness for PUUID {}: {}", puuid, e.getMessage());
            // On error, assume cache might be outdated (safe approach)
            return false;
        }
    }
    
    /**
     * Gets cached matches from database for the summoner.
     * 
     * @param puuid The player's PUUID
     * @param start Starting index
     * @param count Number of matches to retrieve
     * @return List of cached match DTOs
     */
    private List<MatchHistoryDTO> getCachedMatches(String puuid, int start, int count) {
        Optional<Summoner> summonerOpt = summonerRepository.findByPuuid(puuid);
        if (summonerOpt.isEmpty()) {
            return Collections.emptyList();
        }
        
        Summoner summoner = summonerOpt.get();
        List<com.tfg.tfg.model.entity.MatchEntity> dbMatches = matchRepository
            .findRecentMatchesBySummoner(summoner, 
                org.springframework.data.domain.PageRequest.of(0, start + count));
        
        List<MatchHistoryDTO> cachedMatches = new ArrayList<>();
        for (com.tfg.tfg.model.entity.MatchEntity match : dbMatches) {
            if (cachedMatches.size() >= start + count) break;
            cachedMatches.add(convertMatchEntityToDTO(match));
        }
        
        // Return only the requested page
        if (cachedMatches.size() > start) {
            return cachedMatches.subList(start, Math.min(start + count, cachedMatches.size()));
        }
        return Collections.emptyList();
    }
    
    /**
     * Fetches match IDs from Riot API.
     * 
     * @param puuid The player's PUUID
     * @param start Starting index
     * @param count Number of matches to retrieve
     * @return Array of match IDs
     */
    /**
     * Fetch a single batch of match IDs from Riot API
     * @return true if more batches might be available, false if we should stop
     */
    private boolean fetchMatchIdBatch(String puuid, int start, int count, int maxBatch, List<String> accumulated) {
        int toFetch = Math.min(count - accumulated.size(), maxBatch);
        int currentStart = start + accumulated.size();

        logger.debug("Requesting match IDs from Riot: start={}, count={}", currentStart, toFetch);
        
        ResponseEntity<String[]> matchIdsResponse = restTemplate.exchange(
            MATCH_IDS_BY_PUUID_URL,
            HttpMethod.GET,
            null,
            String[].class,
            puuid,
            currentStart,
            toFetch,
            apiKey
        );

        String[] ids = matchIdsResponse.getBody();

        if (ids == null || ids.length == 0) {
            logger.debug("Riot returned no more match IDs (start={})", currentStart);
            return false;
        }

        // Add returned IDs, avoiding duplicates
        for (String id : ids) {
            if (id != null && !accumulated.contains(id)) {
                accumulated.add(id);
            }
        }

        // If Riot returned fewer than requested, assume no more results
        if (ids.length < toFetch) {
            logger.debug("Riot returned fewer IDs ({}) than requested ({}) in this batch, stopping.", ids.length, toFetch);
            return false;
        }

        return true; // More batches might be available
    }
    
    private String[] fetchMatchIdsFromAPI(String puuid, int start, int count) {
        logger.info("Cache insufficient, calling Riot API for match IDs: start={}, count={}", start, count);

        // Riot may limit the number of IDs returned in a single call (or ignore large `count` values).
        // To be robust, page through results in batches until we have `count` IDs or Riot returns no more.
        final int MAX_BATCH = 50; // safe per-call batch size (Riot commonly allows up to 100)
        List<String> accumulated = new ArrayList<>();

        try {
            while (accumulated.size() < count) {
                if (!fetchMatchIdBatch(puuid, start, count, MAX_BATCH, accumulated)) {
                    break; // No more results available
                }
            }
        } catch (HttpClientErrorException hce) {
            logger.warn("Riot API error while fetching match IDs for PUUID {}: {}", puuid, hce.getMessage());
        } catch (Exception e) {
            logger.warn("Unexpected error while fetching match IDs for PUUID {}: {}", puuid, e.getMessage());
            logger.debug(STACKTRACE_LOG_MESSAGE, e);
        }

        // Return an empty array when no IDs could be fetched to keep return type stable
        return accumulated.isEmpty() ? new String[0] : accumulated.toArray(new String[0]);
    }
    
    /**
     * Fetches match details from cache or API.
     * 
     * @param matchIds Array of match IDs to fetch
     * @param puuid The player's PUUID
     * @return List of match history DTOs
     */
    private List<MatchHistoryDTO> fetchMatchDetails(String[] matchIds, String puuid) {
        List<MatchHistoryDTO> matches = new ArrayList<>();
        for (String matchId : matchIds) {
            Optional<com.tfg.tfg.model.entity.MatchEntity> cachedMatch = 
                matchRepository.findByMatchId(matchId);
            
            if (cachedMatch.isPresent()) {
                logger.debug("Match {} found in cache", matchId);
                matches.add(convertMatchEntityToDTO(cachedMatch.get()));
            } else {
                logger.debug("Match {} not in cache, fetching from API", matchId);
                fetchAndAddMatchToHistory(matchId, puuid, matches);
            }
        }
        return matches;
    }
    
    /**
     * Gets complete match details including all 10 participants
     * @param matchId The match ID to fetch
     * @return MatchDetailDTO with complete match information
     */
    public com.tfg.tfg.model.dto.MatchDetailDTO getMatchDetails(String matchId) {
        try {
            logger.info("Fetching detailed match information for match ID: {}", matchId);
            
            ResponseEntity<RiotMatchDTO> matchResponse = restTemplate.exchange(
                MATCH_BY_ID_URL,
                HttpMethod.GET,
                null,
                RiotMatchDTO.class,
                matchId,
                apiKey
            );
            
            RiotMatchDTO riotMatch = matchResponse.getBody();
            if (riotMatch == null || riotMatch.getInfo() == null) {
                logger.warn("No match data found for match ID: {}", matchId);
                return null;
            }
            
            return mapToMatchDetailDTO(riotMatch);
            
        } catch (HttpClientErrorException hce) {
            logger.warn("Riot API error fetching match details for {}: {}", matchId, hce.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error fetching match details for {}: {}", matchId, e.getMessage());
            logger.debug(STACKTRACE_LOG_MESSAGE, e);
            return null;
        }
    }
    
    /**
     * Maps RiotMatchDTO to MatchDetailDTO with all participants and teams
     */
    private com.tfg.tfg.model.dto.MatchDetailDTO mapToMatchDetailDTO(RiotMatchDTO riotMatch) {
        com.tfg.tfg.model.dto.MatchDetailDTO dto = new com.tfg.tfg.model.dto.MatchDetailDTO();
        
        // Basic match info
        dto.setMatchId(riotMatch.getMetadata() != null ? riotMatch.getMetadata().getMatchId() : null);
        dto.setGameCreation(riotMatch.getInfo().getGameCreation());
        dto.setGameDuration(riotMatch.getInfo().getGameDuration());
        dto.setGameMode(riotMatch.getInfo().getGameMode());
        dto.setGameType(riotMatch.getInfo().getGameType());
        dto.setGameVersion(riotMatch.getInfo().getGameVersion());
        dto.setQueueId(riotMatch.getInfo().getQueueId());
        
        // Map all participants
        List<com.tfg.tfg.model.dto.ParticipantDTO> participants = new ArrayList<>();
        if (riotMatch.getInfo().getParticipants() != null) {
            for (RiotMatchDTO.ParticipantDTO riotParticipant : riotMatch.getInfo().getParticipants()) {
                participants.add(mapToParticipantDTO(riotParticipant));
            }
        }
        dto.setParticipants(participants);
        
        // Map teams
        List<com.tfg.tfg.model.dto.TeamDTO> teams = new ArrayList<>();
        if (riotMatch.getInfo().getTeams() != null) {
            for (RiotMatchDTO.TeamDTO riotTeam : riotMatch.getInfo().getTeams()) {
                teams.add(mapToTeamDTO(riotTeam, participants));
            }
        }
        dto.setTeams(teams);
        
        return dto;
    }
    
    /**
     * Maps Riot ParticipantDTO to our ParticipantDTO
     */
    private com.tfg.tfg.model.dto.ParticipantDTO mapToParticipantDTO(RiotMatchDTO.ParticipantDTO riotParticipant) {
        com.tfg.tfg.model.dto.ParticipantDTO dto = new com.tfg.tfg.model.dto.ParticipantDTO();
        
        dto.setSummonerName(riotParticipant.getSummonerName());
        dto.setRiotIdGameName(riotParticipant.getRiotIdGameName());
        dto.setRiotIdTagline(riotParticipant.getRiotIdTagline());
        dto.setChampionName(riotParticipant.getChampionName());
        dto.setChampionIconUrl(dataDragonService.getChampionIconUrl(
            riotParticipant.getChampionId() != null ? riotParticipant.getChampionId().longValue() : null
        ));
        dto.setKills(riotParticipant.getKills());
        dto.setDeaths(riotParticipant.getDeaths());
        dto.setAssists(riotParticipant.getAssists());
        dto.setLevel(riotParticipant.getChampLevel());
        dto.setTotalMinionsKilled(riotParticipant.getTotalMinionsKilled());
        dto.setGoldEarned(riotParticipant.getGoldEarned());
        dto.setTotalDamageDealtToChampions(riotParticipant.getTotalDamageDealtToChampions());
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
     * Maps Riot TeamDTO to our TeamDTO
     */
    private com.tfg.tfg.model.dto.TeamDTO mapToTeamDTO(RiotMatchDTO.TeamDTO riotTeam, 
                                                        List<com.tfg.tfg.model.dto.ParticipantDTO> allParticipants) {
        com.tfg.tfg.model.dto.TeamDTO dto = new com.tfg.tfg.model.dto.TeamDTO();
        
        dto.setTeamId(riotTeam.getTeamId());
        dto.setWin(riotTeam.getWin());
        
        // Filter participants by team
        dto.setParticipants(allParticipants.stream()
            .filter(p -> riotTeam.getTeamId().equals(p.getTeamId()))
            .toList());
        
        // Objectives
        mapTeamObjectives(riotTeam, dto);
        
        // Bans
        mapTeamBans(riotTeam, dto);
        
        return dto;
    }
    
    /**
     * Maps team objectives from Riot API to DTO
     */
    private void mapTeamObjectives(RiotMatchDTO.TeamDTO riotTeam, com.tfg.tfg.model.dto.TeamDTO dto) {
        if (riotTeam.getObjectives() != null) {
            dto.setBaronKills(getObjectiveKills(riotTeam.getObjectives().getBaron()));
            dto.setDragonKills(getObjectiveKills(riotTeam.getObjectives().getDragon()));
            dto.setTowerKills(getObjectiveKills(riotTeam.getObjectives().getTower()));
            dto.setInhibitorKills(getObjectiveKills(riotTeam.getObjectives().getInhibitor()));
            dto.setRiftHeraldKills(getObjectiveKills(riotTeam.getObjectives().getRiftHerald()));
        }
    }
    
    /**
     * Gets kills from an objective, returns 0 if null
     */
    private Integer getObjectiveKills(RiotMatchDTO.ObjectiveDTO objective) {
        return objective != null ? objective.getKills() : 0;
    }
    
    /**
     * Maps team bans from Riot API to DTO
     */
    private void mapTeamBans(RiotMatchDTO.TeamDTO riotTeam, com.tfg.tfg.model.dto.TeamDTO dto) {
        if (riotTeam.getBans() != null) {
            dto.setBans(riotTeam.getBans().stream()
                .map(ban -> dataDragonService.getChampionNameById(
                    ban.getChampionId() != null ? ban.getChampionId().longValue() : null))
                .filter(name -> name != null && !name.isEmpty())
                .toList());
        }
    }
    
    /**
     * Maps RiotMatchDTO to MatchHistoryDTO for a specific player
     */
    private MatchHistoryDTO mapToMatchHistoryDTO(RiotMatchDTO riotMatch, String puuid) {
        if (riotMatch.getInfo() == null || riotMatch.getInfo().getParticipants() == null) {
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
        dto.setChampionIconUrl(dataDragonService.getChampionIconUrl(
            participant.getChampionId() != null ? participant.getChampionId().longValue() : null
        ));
        dto.setWin(participant.getWin());
        dto.setKills(participant.getKills());
        dto.setDeaths(participant.getDeaths());
        dto.setAssists(participant.getAssists());
        dto.setGameDuration(riotMatch.getInfo().getGameDuration());
        Long gameEndTimestamp = riotMatch.getInfo().getGameEndTimestamp();
        dto.setGameTimestamp(gameEndTimestamp != null ? gameEndTimestamp / 1000 : null);
        dto.setQueueId(riotMatch.getInfo().getQueueId());
        
        return dto;
    }
}