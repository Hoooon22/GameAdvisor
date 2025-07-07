package com.gameadvisor.controller;

import com.gameadvisor.model.vector.BloonsTDKnowledge;
import com.gameadvisor.model.vector.VectorSearchResult;
import com.gameadvisor.service.vector.BloonsTDSampleDataService;
import com.gameadvisor.service.vector.BloonsTDVectorService;
import com.gameadvisor.service.vector.BTDAdvancedDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/btd-vector")
@CrossOrigin(origins = "*")
public class BTDVectorController {
    
    private final BloonsTDVectorService vectorService;
    private final BloonsTDSampleDataService sampleDataService;
    private final BTDAdvancedDataService advancedDataService;
    
    @Autowired
    public BTDVectorController(BloonsTDVectorService vectorService, BloonsTDSampleDataService sampleDataService, BTDAdvancedDataService advancedDataService) {
        this.vectorService = vectorService;
        this.sampleDataService = sampleDataService;
        this.advancedDataService = advancedDataService;
    }
    
    /**
     * BTD 샘플 지식 데이터 생성
     */
    @PostMapping("/sample-data")
    public ResponseEntity<Map<String, Object>> createSampleData() {
        log.info("BTD 샘플 데이터 생성 요청");
        
        try {
            // 기존 데이터 개수 확인
            long beforeCount = sampleDataService.getKnowledgeCount();
            
            // 샘플 데이터 생성
            sampleDataService.createSampleData();
            
            // 생성 후 데이터 개수 확인
            long afterCount = sampleDataService.getKnowledgeCount();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "BTD 샘플 데이터 생성 완료",
                "beforeCount", beforeCount,
                "afterCount", afterCount,
                "addedCount", afterCount - beforeCount
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("BTD 샘플 데이터 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "샘플 데이터 생성 실패: " + e.getMessage()));
        }
    }
    
    /**
     * BTD 상황별 지식 검색
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchKnowledge(@RequestBody Map<String, String> request) {
        String situation = request.get("situation");
        int limit = Integer.parseInt(request.getOrDefault("limit", "5"));
        
        log.info("BTD 지식 검색 요청: {}", situation);
        
        try {
            List<VectorSearchResult> results = vectorService.searchSimilar(situation, limit);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "situation", situation,
                "resultCount", results.size(),
                "results", results
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("BTD 지식 검색 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "지식 검색 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 새로운 BTD 지식 추가
     */
    @PostMapping("/knowledge")
    public ResponseEntity<Map<String, Object>> addKnowledge(@RequestBody BloonsTDKnowledge knowledge) {
        log.info("새 BTD 지식 추가 요청: {}", knowledge.getTitle());
        
        try {
            vectorService.saveKnowledge(knowledge);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "BTD 지식 추가 완료",
                "knowledgeId", knowledge.getId()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("BTD 지식 추가 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "지식 추가 실패: " + e.getMessage()));
        }
    }
    
    /**
     * BTD 벡터 DB 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            long totalCount = sampleDataService.getKnowledgeCount();
            
            Map<String, Object> stats = Map.of(
                "success", true,
                "totalKnowledgeCount", totalCount,
                "gameSupported", "BloonsTD",
                "searchCapabilities", List.of(
                    "상황별 검색 (세라믹, MOAB, 카모, 납, 보스)",
                    "라운드 범위별 검색 (early, mid, late, freeplay)", 
                    "난이도별 검색 (easy, medium, hard, expert)",
                    "타워 조합별 검색"
                )
            );
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("BTD 통계 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "통계 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * BTD 지식 사용량 증가 (피드백용)
     */
    @PostMapping("/knowledge/{knowledgeId}/use")
    public ResponseEntity<Map<String, Object>> incrementUsage(@PathVariable String knowledgeId) {
        try {
            vectorService.incrementUsage(knowledgeId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "지식 사용량 업데이트 완료",
                "knowledgeId", knowledgeId
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("BTD 지식 사용량 업데이트 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "사용량 업데이트 실패: " + e.getMessage()));
        }
    }
    
    /**
     * BTD 지식 성공률 업데이트 (피드백용)
     */
    @PostMapping("/knowledge/{knowledgeId}/success")
    public ResponseEntity<Map<String, Object>> updateSuccessRate(
            @PathVariable String knowledgeId, 
            @RequestBody Map<String, Double> request) {
        
        Double successRate = request.get("successRate");
        
        try {
            vectorService.updateSuccessMetric(knowledgeId, successRate);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "지식 성공률 업데이트 완료",
                "knowledgeId", knowledgeId,
                "newSuccessRate", successRate
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("BTD 지식 성공률 업데이트 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "성공률 업데이트 실패: " + e.getMessage()));
        }
    }
    
    /**
     * BTD 고급 전략 데이터 생성
     */
    @PostMapping("/advanced-data")
    public ResponseEntity<Map<String, Object>> createAdvancedData() {
        log.info("BTD 고급 전략 데이터 생성 요청");
        
        try {
            long beforeCount = sampleDataService.getKnowledgeCount();
            
            // 고급 전략 데이터 생성
            advancedDataService.createAdvancedStrategies();
            
            long afterCount = sampleDataService.getKnowledgeCount();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "BTD 고급 전략 데이터 생성 완료",
                "beforeCount", beforeCount,
                "afterCount", afterCount,
                "addedCount", afterCount - beforeCount
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("BTD 고급 전략 데이터 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "고급 데이터 생성 실패: " + e.getMessage()));
        }
    }
    
    /**
     * BTD 상황별 맞춤 데이터 생성
     */
    @PostMapping("/situation-data")
    public ResponseEntity<Map<String, Object>> createSituationData() {
        log.info("BTD 상황별 맞춤 데이터 생성 요청");
        
        try {
            long beforeCount = sampleDataService.getKnowledgeCount();
            
            // 상황별 맞춤 데이터 생성
            advancedDataService.createSituationSpecificKnowledge();
            
            long afterCount = sampleDataService.getKnowledgeCount();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "BTD 상황별 맞춤 데이터 생성 완료",
                "beforeCount", beforeCount,
                "afterCount", afterCount,
                "addedCount", afterCount - beforeCount
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("BTD 상황별 맞춤 데이터 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "상황별 데이터 생성 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 모든 BTD 데이터 삭제 (초기화용)
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        log.info("BTD 모든 데이터 삭제 요청");
        
        try {
            sampleDataService.clearAllData();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "BTD 모든 데이터 삭제 완료"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("BTD 데이터 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "데이터 삭제 실패: " + e.getMessage()));
        }
    }
} 