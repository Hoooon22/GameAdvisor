package com.gameadvisor.service.vector;

import com.gameadvisor.model.vector.BaseGameKnowledge;
import com.gameadvisor.model.vector.BloonsTDKnowledge;
import com.gameadvisor.model.vector.VectorSearchResult;
import com.gameadvisor.repository.vector.BloonsTDVectorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class BloonsTDVectorService implements GameVectorService {
    
    private final BloonsTDVectorRepository repository;
    
    @Autowired
    public BloonsTDVectorService(BloonsTDVectorRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public List<VectorSearchResult> searchSimilar(String situation, int limit) {
        log.info("BloonsTD 상황 검색: {}", situation);
        
        try {
            // 상황에서 정보 추출
            int round = extractRoundNumber(situation);
            String roundRange = BloonsTDKnowledge.getRoundRange(round);
            String situationType = extractSituationType(situation);
            String difficulty = extractDifficulty(situation);
            
            log.info("추출된 정보 - 라운드: {}, 범위: {}, 유형: {}, 난이도: {}", 
                    round, roundRange, situationType, difficulty);
            
            // 상황을 임베딩으로 변환 (현재는 가상의 임베딩 사용)
            List<Double> queryEmbedding = generateQueryEmbedding(situation);
            
            // 우선 상황 유형으로 필터링하여 검색
            List<VectorSearchResult> results;
            if (!"general".equals(situationType)) {
                results = repository.findSimilarByType(queryEmbedding, situationType, limit);
            } else {
                results = repository.findSimilar(queryEmbedding, 0.5, limit);
            }
            
            log.info("BloonsTD 검색 완료: {} 개 결과", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("BloonsTD 검색 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public void saveKnowledge(BaseGameKnowledge knowledge) {
        if (knowledge instanceof BloonsTDKnowledge btdKnowledge) {
            log.info("BloonsTD 지식 저장: {}", btdKnowledge.getTitle());
            try {
                repository.save(btdKnowledge);
                log.info("BloonsTD 지식 저장 완료: {}", btdKnowledge.getId());
            } catch (Exception e) {
                log.error("BloonsTD 지식 저장 실패: {}", e.getMessage(), e);
            }
        }
    }
    
    @Override
    public void incrementUsage(String knowledgeId) {
        log.info("BloonsTD 지식 사용량 증가: {}", knowledgeId);
        try {
            repository.incrementUsageCount(knowledgeId);
        } catch (Exception e) {
            log.error("BloonsTD 지식 사용량 업데이트 실패: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void updateSuccessMetric(String knowledgeId, double successMetric) {
        log.info("BloonsTD 지식 성공률 업데이트: {} -> {}", knowledgeId, successMetric);
        try {
            repository.updateSuccessMetric(knowledgeId, successMetric);
        } catch (Exception e) {
            log.error("BloonsTD 지식 성공률 업데이트 실패: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public String getGameName() {
        return "BloonsTD";
    }
    
    @Override
    public boolean supports(String gameName) {
        return gameName != null && 
               (gameName.toLowerCase().contains("bloons") || 
                gameName.toLowerCase().contains("btd") ||
                gameName.toLowerCase().contains("bloonstd"));
    }
    
    // 상황을 벡터로 변환하는 임시 메서드 (실제로는 임베딩 API 사용)
    private List<Double> generateQueryEmbedding(String situation) {
        // 임시로 간단한 해시 기반 벡터 생성 (실제로는 제미나이나 다른 임베딩 API 사용)
        int hash = situation.hashCode();
        List<Double> embedding = new java.util.ArrayList<>();
        
        for (int i = 0; i < 768; i++) { // 일반적인 임베딩 차원
            embedding.add((double) ((hash + i) % 1000) / 1000.0);
        }
        
        return embedding;
    }
    
    // 상황에서 라운드 번호 추출
    private int extractRoundNumber(String situation) {
        Pattern pattern = Pattern.compile("라운드\\s*(\\d+)|round\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(situation);
        
        if (matcher.find()) {
            String roundStr = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            try {
                return Integer.parseInt(roundStr);
            } catch (NumberFormatException e) {
                log.warn("라운드 번호 파싱 실패: {}", roundStr);
            }
        }
        
        return 1; // 기본값
    }
    
    // 상황 유형 추출
    private String extractSituationType(String situation) {
        String lower = situation.toLowerCase();
        
        if (lower.contains("세라믹") || lower.contains("ceramic")) {
            return BloonsTDKnowledge.SituationType.CERAMIC_DEFENSE;
        }
        if (lower.contains("moab") || lower.contains("모압")) {
            return BloonsTDKnowledge.SituationType.MOAB_BATTLE;
        }
        if (lower.contains("camo") || lower.contains("카모") || lower.contains("위장")) {
            return BloonsTDKnowledge.SituationType.CAMO_DETECTION;
        }
        if (lower.contains("lead") || lower.contains("리드") || lower.contains("납")) {
            return BloonsTDKnowledge.SituationType.LEAD_POPPING;
        }
        if (lower.contains("boss") || lower.contains("보스")) {
            return BloonsTDKnowledge.SituationType.BOSS_BATTLE;
        }
        if (lower.contains("combo") || lower.contains("조합")) {
            return BloonsTDKnowledge.SituationType.TOWER_COMBO;
        }
        if (lower.contains("economy") || lower.contains("경제") || lower.contains("돈")) {
            return BloonsTDKnowledge.SituationType.ECONOMY_BUILD;
        }
        
        return "general"; // 기본값
    }
    
    // 난이도 추출
    private String extractDifficulty(String situation) {
        String lower = situation.toLowerCase();
        
        if (lower.contains("expert") || lower.contains("전문가")) {
            return "expert";
        }
        if (lower.contains("hard") || lower.contains("어려움")) {
            return "hard";
        }
        if (lower.contains("medium") || lower.contains("보통")) {
            return "medium";
        }
        if (lower.contains("easy") || lower.contains("쉬움")) {
            return "easy";
        }
        
        return "medium"; // 기본값
    }
} 