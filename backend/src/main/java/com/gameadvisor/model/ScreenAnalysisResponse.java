package com.gameadvisor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenAnalysisResponse {
    
    /**
     * 화면 분석 결과 설명
     */
    private String analysis;
    
    /**
     * 상황에 대한 조언
     */
    private String advice;
    
    /**
     * 캐릭터 이름
     */
    private String characterName;
    
    /**
     * 분석 대상 게임
     */
    private String gameContext;
    
    /**
     * 응답 시간 (문자열 형태)
     */
    private String timestamp;
    
    /**
     * 성공 여부
     */
    private boolean success;
    
    /**
     * 오류 메시지 (실패 시)
     */
    private String errorMessage;
} 