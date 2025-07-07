package com.gameadvisor.model.vector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseGameKnowledge {
    private String id;
    private String situationType;
    private String title;
    private String content;
    private String advice;
    private List<String> tags;
    private List<Double> embedding;
    private Double confidence;
    private Integer usageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 게임별 특화 필드를 위한 추상 메서드
    public abstract String getGameName();
    public abstract Double getSuccessMetric(); // 성공률 또는 승률
} 