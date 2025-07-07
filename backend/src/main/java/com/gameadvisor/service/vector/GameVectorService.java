package com.gameadvisor.service.vector;

import com.gameadvisor.model.vector.BaseGameKnowledge;
import com.gameadvisor.model.vector.VectorSearchResult;

import java.util.List;

public interface GameVectorService {
    
    /**
     * 현재 상황과 유사한 게임 지식 검색
     * @param situation 현재 게임 상황 설명
     * @param limit 최대 검색 결과 수
     * @return 유사도 순으로 정렬된 검색 결과
     */
    List<VectorSearchResult> searchSimilar(String situation, int limit);
    
    /**
     * 게임 지식 저장
     * @param knowledge 저장할 게임 지식
     */
    void saveKnowledge(BaseGameKnowledge knowledge);
    
    /**
     * 게임 지식 사용량 증가
     * @param knowledgeId 지식 ID
     */
    void incrementUsage(String knowledgeId);
    
    /**
     * 지식 성공률 업데이트
     * @param knowledgeId 지식 ID
     * @param successMetric 성공률 (0.0 ~ 1.0)
     */
    void updateSuccessMetric(String knowledgeId, double successMetric);
    
    /**
     * 게임 이름 반환
     * @return 게임 이름
     */
    String getGameName();
    
    /**
     * 지원하는 게임인지 확인
     * @param gameName 게임 이름
     * @return 지원 여부
     */
    boolean supports(String gameName);
} 