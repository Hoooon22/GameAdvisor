package com.gameadvisor.repository.vector;

import com.gameadvisor.model.vector.BaseGameKnowledge;
import com.gameadvisor.model.vector.VectorSearchResult;

import java.util.List;
import java.util.Optional;

public interface GameVectorRepository<T extends BaseGameKnowledge> {
    
    // 기본 CRUD 작업
    void save(T knowledge);
    Optional<T> findById(String id);
    void update(T knowledge);
    void deleteById(String id);
    List<T> findAll();
    
    // 벡터 검색 관련
    List<VectorSearchResult> findSimilar(List<Double> queryEmbedding, int limit);
    List<VectorSearchResult> findSimilar(List<Double> queryEmbedding, double minSimilarity, int limit);
    
    // 필터링 검색
    List<VectorSearchResult> findSimilarByType(List<Double> queryEmbedding, String situationType, int limit);
    List<VectorSearchResult> findSimilarByConfidence(List<Double> queryEmbedding, double minConfidence, int limit);
    
    // 통계 및 관리
    long count();
    long countBySituationType(String situationType);
    List<T> findTopByUsageCount(int limit);
    List<T> findTopBySuccessMetric(int limit);
    
    // 사용량 업데이트
    void incrementUsageCount(String id);
    void updateSuccessMetric(String id, double successMetric);
    
    // 벡터 임베딩 업데이트
    void updateEmbedding(String id, List<Double> embedding);
} 