package com.tfg.tfg.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.entity.Summoner;
import com.tfg.tfg.repository.SummonerRepository;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.model.entity.UserModel;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private static final String UNRANKED = "Unranked";
    
    private final SummonerRepository summonerRepository;
    private final UserModelRepository userModelRepository;

    public DashboardController(SummonerRepository summonerRepository, UserModelRepository userModelRepository) {
        this.summonerRepository = summonerRepository;
        this.userModelRepository = userModelRepository;
    }

    @GetMapping("/me/stats")
    public ResponseEntity<Map<String, Object>> myStats() {
        // Development: return simple aggregated stats. If a summoner exists, use its info.
        Map<String, Object> result = new HashMap<>();
        var allSummoners = summonerRepository.findAll();
        Summoner s = allSummoners.stream().findFirst().orElse(null);

        // Resolve authenticated username and linked summoner (0 or 1 relation for Phase 2)
        String username = "Guest";
        String linkedSummonerName = null;
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                username = auth.getName();
                var user = userModelRepository.findByName(username).orElse(null);
                if (user != null) {
                    // find summoner with same name as user (simple static link)
                    linkedSummonerName = allSummoners.stream()
                            .filter(sm -> sm.getName() != null && sm.getName().equalsIgnoreCase(user.getName()))
                            .map(Summoner::getName)
                            .findFirst()
                            .orElse(null);
                }
            }
        } catch (Exception ex) {
            // ignore and keep defaults
        }
        if (s != null) {
            String tier = s.getTier() == null ? UNRANKED : s.getTier();
            String rank = s.getRank() == null ? "" : s.getRank();
            String currentRank = tier.equals(UNRANKED) ? tier : tier + " " + rank;
            result.put("currentRank", currentRank);
            result.put("lp7days", 42);
            result.put("mainRole", "Mid Lane");
            result.put("favoriteChampion", s.getChampionStats().stream().findFirst().map(c -> c.getChampionId()).orElse(null));
        } else {
            result.put("currentRank", UNRANKED);
            result.put("lp7days", 0);
            result.put("mainRole", "Unknown");
            result.put("favoriteChampion", null);
        }
        // include username and linked summoner in the response for the UI
        result.put("username", username);
        result.put("linkedSummoner", linkedSummonerName);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/me/favorites")
    public ResponseEntity<List<SummonerDTO>> myFavorites() {
        // Development stub: return up to two favorite summoners for the "own" summoner.
        // For Phase 2 we treat the first summoner in the DB as the user's own summoner and
        // return up to two other summoners as its favorites (static behavior).
        var all = summonerRepository.findAll();

        if (all.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        // Try to resolve own summoner from authenticated user
        Summoner own = null;
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String username = auth.getName();
                UserModel user = userModelRepository.findByName(username).orElse(null);
                if (user != null) {
                    // Try to find a summoner with the same name as the user (simple static link for Phase 2)
                    own = all.stream().filter(s -> s.getName() != null && s.getName().equalsIgnoreCase(user.getName())).findFirst().orElse(null);
                }
            }
        } catch (Exception ex) {
            // ignore and fallback
        }

        // Fallback: if no own resolved, pick the first summoner as before
        if (own == null) {
            own = all.stream().findFirst().orElse(null);
        }

        Summoner finalOwn = own;
        List<SummonerDTO> list = all.stream()
                .filter(s -> finalOwn == null || !s.getId().equals(finalOwn.getId()))
                .limit(2)
                .map(s -> {
                    SummonerDTO dto = new SummonerDTO();
                    dto.setId(s.getId());
                    dto.setName(s.getName());
                    dto.setRiotId(s.getRiotId());
                    dto.setTier(s.getTier());
                    dto.setRank(s.getRank());
                    return dto;
                }).toList();

        return ResponseEntity.ok(list);
    }
}
