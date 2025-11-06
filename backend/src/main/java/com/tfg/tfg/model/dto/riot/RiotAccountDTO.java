package com.tfg.tfg.model.dto.riot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiotAccountDTO {
    private String puuid;
    private String gameName;
    private String tagLine;
}