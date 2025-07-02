package com.gameadvisor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenAnalysisRequest {
    
    /**
     * Base64로 인코딩된 스크린샷 이미지
     */
    private String imageBase64;
    
    /**
     * 현재 플레이 중인 게임 이름
     */
    private String gameName;
    
    /**
     * 추가 컨텍스트 정보
     */
    private String additionalContext;
} 