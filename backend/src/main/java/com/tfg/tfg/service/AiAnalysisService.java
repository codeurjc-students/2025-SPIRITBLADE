package com.tfg.tfg.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tfg.tfg.dto.AiAnalysisResponseDto;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;

/**
 * Service for AI-powered performance analysis using Google Gemini API.
 * Analyzes player match history and provides insights on performance,
 * strengths, weaknesses, and recommendations.
 */
@Service
public class AiAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(AiAnalysisService.class);
    
    @Value("${google.ai.api.key:}")
    private String geminiApiKey;
    
    private final WebClient webClient;
    private final Gson gson;
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    
    public AiAnalysisService() {
        this.webClient = WebClient.builder().build();
        this.gson = new Gson();
    }
    
    /**
     * Generates AI-powered analysis of player performance based on match history.
     * 
     * @param summoner The summoner to analyze
     * @param matches List of recent matches
     * @return DTO containing the AI analysis
     * @throws IOException if API call fails
     */
    public AiAnalysisResponseDto analyzePerformance(Summoner summoner, List<MatchEntity> matches) throws IOException {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            throw new IllegalStateException("Google AI API key not configured");
        }
        
        if (matches.isEmpty()) {
            return new AiAnalysisResponseDto(
                "**Insufficient Match Data**\n\nThere are not enough matches to generate a meaningful analysis. Please play at least 5 ranked matches and try again.\n\n*Analysis requires a minimum match history to provide accurate insights.*",
                LocalDateTime.now(),
                0,
                summoner.getName()
            );
        }
        
        logger.info("Generating AI analysis for summoner: {} with {} matches", summoner.getName(), matches.size());
        
        // Build statistics summary
        String statsPrompt = buildStatsPrompt(summoner, matches);
        
        // Call Gemini API
        String analysis = callGeminiApi(statsPrompt);
        
        return new AiAnalysisResponseDto(
            analysis,
            LocalDateTime.now(),
            matches.size(),
            summoner.getName()
        );
    }
    
    /**
     * Builds a detailed prompt with complete match data for the AI.
     */
    private String buildStatsPrompt(Summoner summoner, List<MatchEntity> matches) {
        int totalMatches = matches.size();
        long wins = matches.stream().filter(MatchEntity::isWin).count();
        long losses = totalMatches - wins;
        double winRate = (wins * 100.0) / totalMatches;
        
        // Build detailed match history with ALL available data from each match
        StringBuilder matchDetails = new StringBuilder();
        for (int i = 0; i < matches.size(); i++) {
            MatchEntity match = matches.get(i);
            double kda = match.getDeaths() > 0 
                ? (double)(match.getKills() + match.getAssists()) / match.getDeaths() 
                : (match.getKills() + match.getAssists());
            
            long durationMinutes = match.getGameDuration() != null ? match.getGameDuration() / 60 : 0;
            
            matchDetails.append(String.format("""
                
                MATCH %d:
                - Result: %s
                - Champion: %s (Level %d)
                - Role/Lane: %s / %s
                - KDA: %d/%d/%d (%.2f ratio)
                - Game Duration: %d minutes
                - Queue Type: %s
                - Gold Earned: %s
                - Total Damage Dealt: %s
                - Rank at Match: %s
                - Date: %s
                """,
                i + 1,
                match.isWin() ? "VICTORY ✓" : "DEFEAT ✗",
                match.getChampionName() != null ? match.getChampionName() : "Unknown",
                match.getChampLevel() != null ? match.getChampLevel() : 0,
                match.getRole() != null ? match.getRole() : "Unknown",
                match.getLane() != null ? match.getLane() : "Unknown",
                match.getKills(),
                match.getDeaths(),
                match.getAssists(),
                kda,
                durationMinutes,
                getQueueTypeName(match.getQueueId()),
                match.getGoldEarned() != null ? String.format("%,d gold", match.getGoldEarned()) : "N/A",
                match.getTotalDamageDealt() != null ? String.format("%,d damage", match.getTotalDamageDealt()) : "N/A",
                getRankString(match.getTierAtMatch(), match.getRankAtMatch(), match.getLpAtMatch()),
                match.getTimestamp() != null ? match.getTimestamp().toString() : "Unknown"
            ));
        }
        
        return String.format("""
            You are SPIRITBLADE, Shen's mystical blade and expert League of Legends analyst. Analyze the performance of player "%s" based on the following detailed data from their last %d matches:
            
            OVERALL SUMMARY:
            - Total Matches: %d
            - Wins: %d (%.1f%%)
            - Losses: %d (%.1f%%)
            
            DETAILED MATCH-BY-MATCH HISTORY:
            %s
            
            Based on this complete match history data, provide a comprehensive analysis in English that includes:
            
            1. **Performance Overview**: Overall assessment of the player's current form and recent trends
            2. **Key Strengths**: What the player excels at (be specific with data from individual matches)
            3. **Critical Weaknesses**: Areas that need immediate attention (cite specific match examples)
            4. **Strategic Recommendations**: Concrete tactical advice to improve gameplay based on observed patterns
            5. **Champion Pool Analysis**: Evaluate their champion choices and suggest improvements
            6. **Pattern Recognition**: Identify trends across matches (consistency, adaptation, tilting patterns, champion comfort)
            7. **Role & Lane Performance**: How well they perform in different positions
            
            Be specific, constructive, and motivating. Reference actual match data to support your conclusions. 
            Use Markdown formatting with headers, bullet points, and bold text for emphasis.
            Make sure to answer the question directly and concisely. It's important that the information presented to the end user is brief and valuable.
            """,
            summoner.getName(),
            totalMatches,
            totalMatches,
            wins,
            winRate,
            losses,
            (losses * 100.0) / totalMatches,
            matchDetails.toString()
        );
    }
    
    /**
     * Helper method to get queue type name from queue ID
     */
    private String getQueueTypeName(Integer queueId) {
        if (queueId == null) return "Unknown";
        return switch (queueId) {
            case 420 -> "Ranked Solo/Duo";
            case 440 -> "Ranked Flex";
            case 400 -> "Normal Draft";
            case 430 -> "Normal Blind";
            case 450 -> "ARAM";
            default -> "Queue " + queueId;
        };
    }
    
    /**
     * Helper method to format rank string
     */
    private String getRankString(String tier, String rank, Integer lp) {
        if (tier == null) return "Unranked";
        String rankStr = tier + (rank != null ? " " + rank : "");
        if (lp != null) {
            rankStr += String.format(" (%d LP)", lp);
        }
        return rankStr;
    }
    
    /**
     * Makes API call to Google Gemini.
     */
    private String callGeminiApi(String prompt) throws IOException {
        try {
            JsonObject requestBody = new JsonObject();
            
            com.google.gson.JsonArray contentsArray = new com.google.gson.JsonArray();
            JsonObject contentItem = new JsonObject();
            
            com.google.gson.JsonArray partsArray = new com.google.gson.JsonArray();
            JsonObject partItem = new JsonObject();
            partItem.addProperty("text", prompt);
            partsArray.add(partItem);
            
            contentItem.add("parts", partsArray);
            contentsArray.add(contentItem);
            
            requestBody.add("contents", contentsArray);
            
            // Add generation config to ensure we get a response
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.7);
            generationConfig.addProperty("topK", 40);
            generationConfig.addProperty("topP", 0.95);
            generationConfig.addProperty("maxOutputTokens", 2048);
            requestBody.add("generationConfig", generationConfig);
            
            // Add safety settings to be more permissive (game analysis should not be blocked)
            com.google.gson.JsonArray safetySettings = new com.google.gson.JsonArray();
            for (String category : new String[]{"HARM_CATEGORY_HARASSMENT", "HARM_CATEGORY_HATE_SPEECH", 
                                                  "HARM_CATEGORY_SEXUALLY_EXPLICIT", "HARM_CATEGORY_DANGEROUS_CONTENT"}) {
                JsonObject setting = new JsonObject();
                setting.addProperty("category", category);
                setting.addProperty("threshold", "BLOCK_ONLY_HIGH");
                safetySettings.add(setting);
            }
            requestBody.add("safetySettings", safetySettings);
            
            String requestJson = gson.toJson(requestBody);
            logger.debug("Sending request to Gemini API");
            
            String response = webClient.post()
                .uri(GEMINI_API_URL + "?key=" + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestJson)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            if (response == null) {
                throw new IOException("Empty response from Gemini API");
            }
            
            // Parse response
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            
            // Log the full response for debugging
            logger.info("Gemini API response: {}", response);
            
            // Check for error in response
            if (jsonResponse.has("error")) {
                JsonObject error = jsonResponse.getAsJsonObject("error");
                String errorMessage = error.has("message") ? error.get("message").getAsString() : "Unknown error";
                throw new IOException("Gemini API error: " + errorMessage);
            }
            
            // Extract the generated text
            if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates") != null) {
                var candidates = jsonResponse.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    var candidate = candidates.get(0).getAsJsonObject();
                    
                    // Check finish reason
                    if (candidate.has("finishReason")) {
                        String finishReason = candidate.get("finishReason").getAsString();
                        if (!"STOP".equals(finishReason) && !"MAX_TOKENS".equals(finishReason)) {
                            logger.warn("Unexpected finish reason from Gemini: {}", finishReason);
                            throw new IOException("Content generation stopped unexpectedly: " + finishReason);
                        }
                    }
                    
                    if (candidate.has("content")) {
                        var content = candidate.getAsJsonObject("content");
                        if (content.has("parts") && content.getAsJsonArray("parts") != null) {
                            var parts = content.getAsJsonArray("parts");
                            if (parts.size() > 0 && parts.get(0).getAsJsonObject().has("text")) {
                                return parts.get(0).getAsJsonObject().get("text").getAsString();
                            }
                        }
                        // Content exists but no parts - likely blocked or empty
                        logger.warn("Gemini returned content without parts. Full response: {}", response);
                        throw new IOException("Gemini returned empty content. This may be due to safety filters or content restrictions.");
                    }
                }
            }
            
            throw new IOException("Unexpected response format from Gemini API. Response: " + response);
            
        } catch (Exception e) {
            logger.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new IOException("Failed to generate AI analysis: " + e.getMessage(), e);
        }
    }
}
