package com.gameadvisor.controller;

import com.gameadvisor.model.vector.VectorSearchResult;
import com.gameadvisor.service.vector.BloonsTDSampleDataService;
import com.gameadvisor.service.vector.GameVectorServiceFactory;
import com.gameadvisor.service.vector.GameVectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/vector")
@CrossOrigin(origins = "*")
public class GameVectorController {
    
    private final GameVectorServiceFactory vectorServiceFactory;
    private final BloonsTDSampleDataService sampleDataService;
    
    @Autowired
    public GameVectorController(GameVectorServiceFactory vectorServiceFactory,
                               BloonsTDSampleDataService sampleDataService) {
        this.vectorServiceFactory = vectorServiceFactory;
        this.sampleDataService = sampleDataService;
    }
    
    /**
     * BTD 샘플 데이터 생성
     */
    @PostMapping("/btd/sample-data")
    public ResponseEntity<Map<String, Object>> createBTDSampleData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("BTD 샘플 데이터 생성 요청");
            
            long beforeCount = sampleDataService.getKnowledgeCount();
            sampleDataService.createSampleData();
            long afterCount = sampleDataService.getKnowledgeCount();
            
            response.put("success", true);
            response.put("message", "BTD 샘플 데이터 생성 완료");
            response.put("beforeCount", beforeCount);
            response.put("afterCount", afterCount);
            response.put("createdCount", afterCount - beforeCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("BTD 샘플 데이터 생성 실패", e);
            response.put("success", false);
            response.put("message", "샘플 데이터 생성 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * BTD 지식 검색 테스트
     */
    @GetMapping("/btd/search")
    public ResponseEntity<Map<String, Object>> searchBTDKnowledge(
            @RequestParam String situation,
            @RequestParam(defaultValue = "3") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("BTD 지식 검색: {}, 제한: {}", situation, limit);
            
            if (!vectorServiceFactory.isSupported("BloonsTD")) {
                response.put("success", false);
                response.put("message", "BloonsTD 벡터 서비스를 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
            GameVectorService btdService = vectorServiceFactory.getService("BloonsTD");
            List<VectorSearchResult> results = btdService.searchSimilar(situation, limit);
            
            response.put("success", true);
            response.put("situation", situation);
            response.put("resultCount", results.size());
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("BTD 지식 검색 실패", e);
            response.put("success", false);
            response.put("message", "검색 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * BTD 지식 통계 조회
     */
    @GetMapping("/btd/stats")
    public ResponseEntity<Map<String, Object>> getBTDStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long totalCount = sampleDataService.getKnowledgeCount();
            List<String> supportedGames = vectorServiceFactory.getSupportedGames();
            String serviceStats = vectorServiceFactory.getServiceStats();
            
            response.put("success", true);
            response.put("totalKnowledgeCount", totalCount);
            response.put("supportedGames", supportedGames);
            response.put("serviceStats", serviceStats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("BTD 통계 조회 실패", e);
            response.put("success", false);
            response.put("message", "통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * BTD 모든 데이터 삭제
     */
    @DeleteMapping("/btd/clear")
    public ResponseEntity<Map<String, Object>> clearBTDData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("BTD 데이터 삭제 요청");
            
            long beforeCount = sampleDataService.getKnowledgeCount();
            sampleDataService.clearAllData();
            long afterCount = sampleDataService.getKnowledgeCount();
            
            response.put("success", true);
            response.put("message", "BTD 데이터 삭제 완료");
            response.put("deletedCount", beforeCount - afterCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("BTD 데이터 삭제 실패", e);
            response.put("success", false);
            response.put("message", "데이터 삭제 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 벡터 서비스 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getVectorServiceStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> supportedGames = vectorServiceFactory.getSupportedGames();
            String serviceStats = vectorServiceFactory.getServiceStats();
            
            response.put("success", true);
            response.put("supportedGames", supportedGames);
            response.put("serviceStats", serviceStats);
            
            // 각 게임별 지원 여부 확인
            Map<String, Boolean> gameSupport = new HashMap<>();
            gameSupport.put("BloonsTD", vectorServiceFactory.isSupported("BloonsTD"));
            gameSupport.put("MasterDuel", vectorServiceFactory.isSupported("MasterDuel"));
            response.put("gameSupport", gameSupport);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("벡터 서비스 상태 확인 실패", e);
            response.put("success", false);
            response.put("message", "상태 확인 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 