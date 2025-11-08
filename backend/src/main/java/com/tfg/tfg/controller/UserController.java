package com.tfg.tfg.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.mapper.UserMapper;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.service.UserAvatarService;
import com.tfg.tfg.service.UserService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String REGION_KEY = "region";
    private static final String USER_NOT_FOUND_MSG = "User not found";
    private static final String ROLE_ADMIN = "ADMIN";

    private final UserModelRepository userRepository;
    private final UserService userService;
    private final RiotService riotService;
    private final UserAvatarService userAvatarService;

    public UserController(UserModelRepository userRepository, UserService userService, 
                         RiotService riotService, UserAvatarService userAvatarService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.riotService = riotService;
        this.userAvatarService = userAvatarService;
    }

    /**
     * Get all users with optional pagination and filters (Admin only).
     * 
     * @param page Page number (default 0)
     * @param size Page size (default 20)
     * @param role Filter by role (optional)
     * @param active Filter by active status (optional)
     * @param search Search by name or email (optional)
     * @return Paginated list of users
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserDTO>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<UserModel> usersPage;
        
        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            usersPage = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                search, search, pageable);
        } else if (role != null && active != null) {
            usersPage = userRepository.findByRolsContainingAndActive(role, active, pageable);
        } else if (role != null) {
            usersPage = userRepository.findByRolsContaining(role, pageable);
        } else if (active != null) {
            usersPage = userRepository.findByActive(active, pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }
        
        Page<UserDTO> dtoPage = usersPage.map(UserMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Get user by ID.
     * 
     * @param id User ID
     * @return User data
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(UserMapper::toDTO)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get user by username.
     * 
     * @param name Username
     * @return User data
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<UserDTO> getByName(@PathVariable String name) {
        return userRepository.findByName(name)
            .map(UserMapper::toDTO)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create new user (Admin only).
     * 
     * @param userDTO User data
     * @return Created user
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        // Exceptions are handled by GlobalExceptionHandler
        userService.createUser(userDTO);
        return userRepository.findByName(userDTO.getName())
            .map(UserMapper::toDTO)
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Update user information (Admin only).
     * Admins cannot modify other admin accounts.
     * 
     * @param id User ID
     * @param userDTO Updated user data
     * @return Updated user
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return userRepository.findById(id).map(user -> {
            // Prevent modification of admin users
            if (user.getRols() != null && user.getRols().contains(ROLE_ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<UserDTO>build();
            }
            
            // Update fields
            if (userDTO.getName() != null) {
                user.setName(userDTO.getName());
            }
            if (userDTO.getEmail() != null) {
                user.setEmail(userDTO.getEmail());
            }
            if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
                user.setRols(userDTO.getRoles());
            }
            user.setActive(userDTO.isActive());
            
            UserModel updatedUser = userRepository.save(user);
            return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Change user role (Admin only).
     * Admins cannot change roles of other admin users.
     * 
     * @param id User ID
     * @param role New role
     * @return Updated user
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<UserDTO> changeRole(@PathVariable Long id, @RequestBody String role) {
        return userRepository.findById(id).map(user -> {
            // Prevent role changes on admin users
            if (user.getRols() != null && user.getRols().contains(ROLE_ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<UserDTO>build();
            }
            
            user.setRols(java.util.List.of(role));
            UserModel updatedUser = userRepository.save(user);
            return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Toggle user active status (Admin only).
     * Admins cannot toggle status of other admin users.
     * 
     * @param id User ID
     * @return Updated user
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<UserDTO> toggleActive(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            // Prevent toggling active status on admin users
            if (user.getRols() != null && user.getRols().contains(ROLE_ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<UserDTO>build();
            }
            
            user.setActive(!user.isActive());
            UserModel updatedUser = userRepository.save(user);
            return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete user (Admin only).
     * Admins cannot delete other admin users.
     * 
     * @param id User ID
     * @return No content
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            // Prevent deletion of admin users
            if (user.getRols() != null && user.getRols().contains(ROLE_ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
            }
            
            userRepository.delete(user);
            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get current authenticated user profile.
     * 
     * @return Current user data with avatar URL
     */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMyProfile(@RequestBody UserDTO userDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = auth.getName();
        
        return userRepository.findByName(username)
            .map(user -> {
                // Update fields
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail());
                }
                if (userDTO.getAvatarUrl() != null) {
                    user.setAvatarUrl(userDTO.getAvatarUrl());
                }
                
                UserModel updatedUser = userRepository.save(user);
                return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            // Fallback: return first user if exists (for development)
            return userRepository.findFirstByOrderByIdAsc()
                .map(UserMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        }

        String username = auth.getName();
        
        return userRepository.findByName(username)
            .map(user -> {
                UserDTO dto = UserMapper.toDTO(user);
                // Ensure avatarUrl is set (fallback to image field if avatarUrl is null)
                if (dto.getAvatarUrl() == null && user.getImage() != null && !user.getImage().isEmpty()) {
                    dto.setAvatarUrl(user.getImage());
                }
                return dto;
            })
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Link a League of Legends account to the authenticated user.
     * 
     * @param request Contains summonerName and region
     * @return Link status
     */
    @PostMapping("/link-summoner")
    public ResponseEntity<Map<String, Object>> linkSummoner(@RequestBody Map<String, String> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
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
            
            // Update user with linked summoner info
            UserModel user = userRepository.findByName(username)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MSG));
            
            user.setLinkedSummonerPuuid(summoner.getPuuid());
            user.setLinkedSummonerName(summoner.getName());
            user.setLinkedSummonerRegion(region);
            userRepository.save(user);

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
    public ResponseEntity<Map<String, Object>> unlinkSummoner() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        
        try {
            UserModel user = userRepository.findByName(username)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MSG));
            
            user.setLinkedSummonerPuuid(null);
            user.setLinkedSummonerName(null);
            user.setLinkedSummonerRegion(null);
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Account unlinked successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
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
    public ResponseEntity<Map<String, Object>> getLinkedSummoner() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        
        try {
            UserModel user = userRepository.findByName(username)
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
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();

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
    public ResponseEntity<Map<String, Object>> deleteAvatar() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();

        // Exceptions are handled by GlobalExceptionHandler
        userAvatarService.deleteAvatar(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, true);
        response.put(MESSAGE_KEY, "Avatar deleted successfully");

        return ResponseEntity.ok(response);
    }
}
