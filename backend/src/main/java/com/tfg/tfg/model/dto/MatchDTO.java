package com.tfg.tfg.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {
    private Long id;
    private String matchId;
    private LocalDateTime timestamp;
    private boolean win;
    private int kills;
    private int deaths;
    private int assists;
}
