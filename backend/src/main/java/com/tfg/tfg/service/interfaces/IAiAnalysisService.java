package com.tfg.tfg.service.interfaces;

import java.io.IOException;
import java.util.List;

import com.tfg.tfg.model.dto.AiAnalysisResponseDto;
import com.tfg.tfg.model.entity.MatchEntity;
import com.tfg.tfg.model.entity.Summoner;

public interface IAiAnalysisService {

    AiAnalysisResponseDto analyzePerformance(Summoner summoner, List<MatchEntity> matches) throws IOException;
}
