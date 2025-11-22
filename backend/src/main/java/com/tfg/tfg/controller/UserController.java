package com.tfg.tfg.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.model.mapper.UserMapper;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.service.UserAvatarService;
import com.tfg.tfg.service.UserService;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String REGION_KEY = "region";
    private static final String USER_NOT_FOUND_MSG = "User not found";

    private final UserService userService;
    private final RiotService riotService;
    private final UserAvatarService userAvatarService;

    public UserController(UserService userService, 
                         RiotService riotService, UserAvatarService userAvatarService) {
        this.userService = userService;
        this.riotService = riotService;
        this.userAvatarService = userAvatarService;
    }

    /**
     * Check if the authenticated user is active.
     * 
     * @param username The username to check
     * @return ResponseEntity with error if inactive, null if active or not found
     */
    private ResponseEntity<Object> checkUserActive(String username) {
        UserModel user = userService.findByName(username).orElse(null);
        if (user != null && !user.isActive()) {
            Map<String, Object> error = new HashMap<>();
            error.put(SUCCESS_KEY, false);
            error.put(MESSAGE_KEY, "User account is deactivated");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        return null;
    }

    @PutMapping("/me")
    public ResponseEntity<Object> updateMyProfile(@RequestBody UserDTO userDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = auth.getName();
        
        // Check if user is active
        ResponseEntity<Object> activeCheck = checkUserActive(username);
        if (activeCheck != null) {
            return activeCheck;
        }
        
        return userService.updateUserProfile(username, userDTO)
            .map(UserMapper::toDTO)
            .map(dto -> ResponseEntity.ok((Object) dto))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/me")
    public ResponseEntity<Object> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            // Fallback: return first user if exists (for development)
            return userService.findFirstUser()
                .map(UserMapper::toDTO)
                .map(dto -> ResponseEntity.ok((Object) dto))
                .orElseGet(() -> ResponseEntity.notFound().build());
        }

        String username = auth.getName();
        
        // Check if user is active
        ResponseEntity<Object> activeCheck = checkUserActive(username);
        if (activeCheck != null) {
            return activeCheck;
        }
        
        return userService.findByName(username)
            .map(user -> {
                UserDTO dto = UserMapper.toDTO(user);
                // Ensure avatarUrl is set (fallback to image field if avatarUrl is null)
                if (dto.getAvatarUrl() == null && user.getImage() != null && !user.getImage().isEmpty()) {
                    dto.setAvatarUrl(user.getImage());
                }
                return dto;
            })
            .map(dto -> ResponseEntity.ok((Object) dto))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Link a League of Legends account to the authenticated user.
     * 
     * @param request Contains summonerName and region
     * @return Link status
     */
    @PostMapping("/link-summoner")
    public ResponseEntity<Object> linkSummoner(@RequestBody Map<String, String> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        
        // Check if user is active
        ResponseEntity<Object> activeCheck = checkUserActive(username);
        if (activeCheck != null) {
            return activeCheck;
        }

        String summonerName = request.get("summonerName");
        String region = request.get(REGION_KEY);

        if (summonerName == null || summonerName.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put(SUCCESS_KEY, false);
            error.put(MESSAGE_KEY, "Summoner name is required");
            return ResponseEntity.badRequest().body(error);
        }

        if (region == null || region.isEmpty()) {
            region = "EUW"; // Default region
        }

        try {
            // Fetch summoner from Riot API
            SummonerDTO summoner = riotService.getSummonerByName(summonerName);
            
            if (summoner == null) {
                Map<String, Object> error = new HashMap<>();
                error.put(SUCCESS_KEY, false);
                error.put(MESSAGE_KEY, "Summoner not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Update user with linked summoner info through service
            userService.linkSummoner(username, summoner.getPuuid(), summoner.getName(), region)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MSG));

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Account linked successfully");
            response.put("linkedSummoner", Map.of(
                "name", summoner.getName(),
                "level", summoner.getLevel(),
                "profileIcon", summoner.getProfileIconId(),
                REGION_KEY, region
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("Failed to link summoner for user {}: {}", username, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put(SUCCESS_KEY, false);
            error.put(MESSAGE_KEY, "Failed to link account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Unlink the League of Legends account from the authenticated user.
     * 
     * @return Unlink status
     */
    @PostMapping("/unlink-summoner")
    public ResponseEntity<Object> unlinkSummoner() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        
        // Check if user is active
        ResponseEntity<Object> activeCheck = checkUserActive(username);
        if (activeCheck != null) {
            return activeCheck;
        }
        
        try {
            userService.unlinkSummoner(username)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MSG));

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Account unlinked successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("Failed to unlink summoner for user {}: {}", username, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put(SUCCESS_KEY, false);
            error.put(MESSAGE_KEY, "Failed to unlink account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get linked League of Legends account for authenticated user.
     * 
     * @return Linked summoner information
     */
    @GetMapping("/linked-summoner")
    public ResponseEntity<Object> getLinkedSummoner() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        
        // Check if user is active
        ResponseEntity<Object> activeCheck = checkUserActive(username);
        if (activeCheck != null) {
            return activeCheck;
        }
        
        try {
            UserModel user = userService.findByName(username)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MSG));
            
            Map<String, Object> response = new HashMap<>();
            
            if (user.getLinkedSummonerPuuid() != null) {
                response.put("linked", true);
                response.put("summonerName", user.getLinkedSummonerName());
                response.put(REGION_KEY, user.getLinkedSummonerRegion());
                response.put("puuid", user.getLinkedSummonerPuuid());
            } else {
                response.put("linked", false);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving linked summoner for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Upload avatar for the authenticated user.
     * 
     * @param file The avatar image file
     * @return Success response with avatar URL
     */
    @PostMapping("/avatar")
    public ResponseEntity<Object> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();

        // Check if user is active
        ResponseEntity<Object> activeCheck = checkUserActive(username);
        if (activeCheck != null) {
            return activeCheck;
        }

        // Exceptions are handled by GlobalExceptionHandler
        String avatarUrl = userAvatarService.uploadAvatar(username, file);
        
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, true);
        response.put(MESSAGE_KEY, "Avatar uploaded successfully");
        response.put("avatarUrl", avatarUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete avatar for the authenticated user.
     * 
     * @return Success response
     */
    @DeleteMapping("/avatar")
    public ResponseEntity<Object> deleteAvatar() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();

        // Check if user is active
        ResponseEntity<Object> activeCheck = checkUserActive(username);
        if (activeCheck != null) {
            return activeCheck;
        }

        // Exceptions are handled by GlobalExceptionHandler
        userAvatarService.deleteAvatar(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, true);
        response.put(MESSAGE_KEY, "Avatar deleted successfully");

        return ResponseEntity.ok(response);
    }
}
