package com.tfg.tfg.model.entity;

import java.util.List;
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UserModel{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "`name`")
    private String name;
    
    @Column(name = "`image`")
    private String image;
    
    private String email;

    private boolean active = true;

    @Setter(AccessLevel.NONE)
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
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<Summoner> favoriteSummoners = new java.util.ArrayList<>();

    public UserModel(){

    }

    public UserModel(String name, String encodedPassword, String... rols){
        this.name = name;
        this.encodedPassword = encodedPassword;
        this.rols = rols != null ? new java.util.ArrayList<>(List.of(rols)) : new java.util.ArrayList<>();
        this.image = null;
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

    /**
     * New clearer accessor name. Keeps compatibility with existing field name.
     */
    public List<String> getRoles() {
        return getRols();
    }

    public void setRoles(List<String> roles) {
        setRols(roles);
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
}
