package com.gameadvisor.model.vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchResult {
    private BaseGameKnowledge knowledge;
    private Double similarity;      // 코사인 유사도 (0.0 ~ 1.0)
    private String matchReason;     // 매칭 이유
    private List<String> matchedKeywords; // 매칭된 키워드들
    private Long searchTimeMs;      // 검색 시간 (밀리초)
    
    // 정렬을 위한 비교 메서드
    public int compareTo(VectorSearchResult other) {
        // 유사도 내림차순 정렬
        int similarityCompare = Double.compare(other.similarity, this.similarity);
        if (similarityCompare != 0) {
            return similarityCompare;
        }
        
        // 유사도가 같으면 성공률 내림차순 정렬
        Double thisSuccess = this.knowledge.getSuccessMetric();
        Double otherSuccess = other.knowledge.getSuccessMetric();
        if (thisSuccess != null && otherSuccess != null) {
            return Double.compare(otherSuccess, thisSuccess);
        }
        
        // 성공률이 없으면 사용 횟수 내림차순 정렬
        return Integer.compare(
            other.knowledge.getUsageCount() != null ? other.knowledge.getUsageCount() : 0,
            this.knowledge.getUsageCount() != null ? this.knowledge.getUsageCount() : 0
        );
    }
    
    // 검색 결과 품질 판정
    public boolean isHighQuality() {
        return similarity >= 0.8 && 
               knowledge.getConfidence() >= 0.7 && 
               knowledge.getSuccessMetric() >= 0.6;
    }
    
    public boolean isMediumQuality() {
        return similarity >= 0.6 && 
               knowledge.getConfidence() >= 0.5;
    }
} 