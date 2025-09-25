package com.tfg.tfg.model.dto;

import java.time.LocalDateTime;

public class MatchDTO {
    public Long id;
    public String matchId;
    public LocalDateTime timestamp;
    public boolean win;
    public int kills;
    public int deaths;
    public int assists;
}
