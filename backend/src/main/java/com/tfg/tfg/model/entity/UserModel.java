package com.tfg.tfg.model.entity;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity(name = "USERS")
public class UserModel{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String image;
    private String email;

    private boolean active = true;

    @Lob
    private Blob profilePic;

    private String encodedPassword;

	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> rols;

    public UserModel(){

    }

    public UserModel(String name, String encodedPassword, String... rols){
        this.name = name;
        this.encodedPassword = encodedPassword;
        this.rols = rols != null ? List.of(rols) : Collections.emptyList();
        this.image = "/users/" + this.id + "/image";
        this.profilePic= UploadStandardProfilePic();
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

    public void setProfilePic(Blob profilePic) {
        this.profilePic = profilePic;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Blob getProfilePic() {
        return profilePic;
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

    public List<String> getRols() {
        return rols;
    }

    public void setRols(List<String> rols) {
        this.rols = rols;
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

    private Blob UploadStandardProfilePic() {
        try {
            InputStream imageStream = getClass().getClassLoader().getResourceAsStream("static/img/default-profile.jpg");
            if (imageStream != null) {
                byte[] imageBytes = imageStream.readAllBytes();
                return new SerialBlob(imageBytes);
            }
            return null;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
