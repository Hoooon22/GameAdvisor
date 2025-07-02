package com.gameadvisor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameAdviceResponse {
    
    private String advice;
    private String characterName;
    private String gameContext;
    private LocalDateTime timestamp;
    private boolean success;
    private String errorMessage;
} 