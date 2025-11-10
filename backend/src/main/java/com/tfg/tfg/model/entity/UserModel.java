package com.tfg.tfg.model.entity;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
public class UserModel{

    private static final Logger logger = LoggerFactory.getLogger(UserModel.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "`name`")
    private String name;
    
    @Column(name = "`image`")
    private String image;
    
    private String email;

    private boolean active = true;

    private String encodedPassword;

	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> rols;

    // Linked League of Legends account
    private String linkedSummonerPuuid;
    private String linkedSummonerName;
    private String linkedSummonerRegion;

    // Avatar URL (from file storage service)
    private String avatarUrl;

    // AI Analysis cooldown tracking
    private java.time.LocalDateTime lastAiAnalysisRequest;

    // Favorite summoners for quick access
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_favorite_summoners",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "summoner_id")
    )
    private List<Summoner> favoriteSummoners = new java.util.ArrayList<>();

    public UserModel(){

    }

    public UserModel(String name, String encodedPassword, String... rols){
        this.name = name;
        this.encodedPassword = encodedPassword;
        this.rols = rols != null ? new java.util.ArrayList<>(List.of(rols)) : new java.util.ArrayList<>();
        this.image = null;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setPass(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    /**
     * Backwards-compatible alias used throughout the codebase.
     */
    public void setEncodedPassword(String encodedPassword) {
        setPass(encodedPassword);
    }

    public List<String> getRols() {
        return rols;
    }

    public void setRols(List<String> rols) {
        this.rols = rols;
    }  

    /**
     * New clearer accessor name. Keeps compatibility with existing field name.
     */
    public List<String> getRoles() {
        return getRols();
    }

    public void setRoles(List<String> roles) {
        setRols(roles);
    }

    public String getLinkedSummonerPuuid() {
        return linkedSummonerPuuid;
    }

    public void setLinkedSummonerPuuid(String linkedSummonerPuuid) {
        this.linkedSummonerPuuid = linkedSummonerPuuid;
    }

    public String getLinkedSummonerName() {
        return linkedSummonerName;
    }

    public void setLinkedSummonerName(String linkedSummonerName) {
        this.linkedSummonerName = linkedSummonerName;
    }

    public String getLinkedSummonerRegion() {
        return linkedSummonerRegion;
    }

    public void setLinkedSummonerRegion(String linkedSummonerRegion) {
        this.linkedSummonerRegion = linkedSummonerRegion;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public java.time.LocalDateTime getLastAiAnalysisRequest() {
        return lastAiAnalysisRequest;
    }

    public void setLastAiAnalysisRequest(java.time.LocalDateTime lastAiAnalysisRequest) {
        this.lastAiAnalysisRequest = lastAiAnalysisRequest;
    }

    public List<Summoner> getFavoriteSummoners() {
        if (favoriteSummoners == null) {
            favoriteSummoners = new java.util.ArrayList<>();
        }
        return favoriteSummoners;
    }

    public void setFavoriteSummoners(List<Summoner> favoriteSummoners) {
        this.favoriteSummoners = favoriteSummoners != null ? favoriteSummoners : new java.util.ArrayList<>();
    }

    public void addFavoriteSummoner(Summoner summoner) {
        if (this.favoriteSummoners == null) {
            this.favoriteSummoners = new java.util.ArrayList<>();
        }
        if (!this.favoriteSummoners.contains(summoner)) {
            this.favoriteSummoners.add(summoner);
        }
    }

    public void removeFavoriteSummoner(Summoner summoner) {
        if (this.favoriteSummoners != null) {
            this.favoriteSummoners.remove(summoner);
        }
    }

    public String determineUserType() {
        if (this.getRols().contains("ADMIN")) {
            return "Administrator";
        } else if (this.getRols().contains("USER")) {
            return "Registered User";
        } else {
            return "Unknown";
        }
    }

    private Blob uploadStandardProfilePic() {
        try {
            InputStream imageStream = getClass().getClassLoader().getResourceAsStream("static/img/default-profile.png");
            if (imageStream != null) {
                byte[] imageBytes = imageStream.readAllBytes();
                return new SerialBlob(imageBytes);
            }
            return null;
        } catch (IOException | SQLException e) {
            logger.warn("Failed to load default profile picture: {}", e.getMessage());
            logger.debug("Stacktrace:", e);
            return null;
        }
    }
    
}
