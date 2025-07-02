package com.gameadvisor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameAdviceRequest {
    
    private String gameName;
    private String currentSituation;
    private String playerLevel;
    private String gameGenre;
    private String specificQuestion;
} 