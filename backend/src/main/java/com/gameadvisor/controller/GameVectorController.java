package com.gameadvisor.controller;

import com.gameadvisor.model.vector.BaseGameKnowledge;
import com.gameadvisor.model.vector.VectorSearchResult;
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
    
    @Autowired
    public GameVectorController(GameVectorServiceFactory vectorServiceFactory) {
        this.vectorServiceFactory = vectorServiceFactory;
    }
    
    /**
     * 게임별 지식 검색
     * @param gameName 게임 이름 (BloonsTD, MasterDuel 등)
     * @param situation 검색할 상황 설명
     * @param limit 최대 검색 결과 수
     */
    @GetMapping("/{gameName}/search")
    public ResponseEntity<Map<String, Object>> searchGameKnowledge(
            @PathVariable String gameName,
            @RequestParam String situation,
            @RequestParam(defaultValue = "5") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("{} 지식 검색: {}, 제한: {}", gameName, situation, limit);
            
            if (!vectorServiceFactory.isSupported(gameName)) {
                response.put("success", false);
                response.put("message", gameName + " 게임은 지원하지 않습니다");
                response.put("supportedGames", vectorServiceFactory.getSupportedGames());
                return ResponseEntity.badRequest().body(response);
            }
            
            GameVectorService gameService = vectorServiceFactory.getService(gameName);
            List<VectorSearchResult> results = gameService.searchSimilar(situation, limit);
            
            response.put("success", true);
            response.put("gameName", gameName);
            response.put("situation", situation);
            response.put("resultCount", results.size());
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("{} 지식 검색 실패", gameName, e);
            response.put("success", false);
            response.put("message", "검색 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 게임별 지식 추가
     * @param gameName 게임 이름
     * @param knowledge 추가할 지식 데이터
     */
    @PostMapping("/{gameName}/knowledge")
    public ResponseEntity<Map<String, Object>> addGameKnowledge(
            @PathVariable String gameName,
            @RequestBody BaseGameKnowledge knowledge) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("{} 지식 추가: {}", gameName, knowledge.getTitle());
            
            if (!vectorServiceFactory.isSupported(gameName)) {
                response.put("success", false);
                response.put("message", gameName + " 게임은 지원하지 않습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
            GameVectorService gameService = vectorServiceFactory.getService(gameName);
            gameService.saveKnowledge(knowledge);
            
            response.put("success", true);
            response.put("message", gameName + " 지식 추가 완료");
            response.put("knowledgeId", knowledge.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("{} 지식 추가 실패", gameName, e);
            response.put("success", false);
            response.put("message", "지식 추가 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 게임별 지식 사용량 증가
     * @param gameName 게임 이름
     * @param knowledgeId 지식 ID
     */
    @PostMapping("/{gameName}/knowledge/{knowledgeId}/use")
    public ResponseEntity<Map<String, Object>> incrementUsage(
            @PathVariable String gameName,
            @PathVariable String knowledgeId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!vectorServiceFactory.isSupported(gameName)) {
                response.put("success", false);
                response.put("message", gameName + " 게임은 지원하지 않습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
            GameVectorService gameService = vectorServiceFactory.getService(gameName);
            gameService.incrementUsage(knowledgeId);
            
            response.put("success", true);
            response.put("message", "지식 사용량 업데이트 완료");
            response.put("knowledgeId", knowledgeId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("{} 지식 사용량 업데이트 실패", gameName, e);
            response.put("success", false);
            response.put("message", "사용량 업데이트 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 게임별 지식 성공률 업데이트
     * @param gameName 게임 이름
     * @param knowledgeId 지식 ID
     * @param request 성공률 데이터
     */
    @PostMapping("/{gameName}/knowledge/{knowledgeId}/success")
    public ResponseEntity<Map<String, Object>> updateSuccessMetric(
            @PathVariable String gameName,
            @PathVariable String knowledgeId,
            @RequestBody Map<String, Double> request) {
        
        Map<String, Object> response = new HashMap<>();
        Double successMetric = request.get("successMetric");
        
        try {
            if (!vectorServiceFactory.isSupported(gameName)) {
                response.put("success", false);
                response.put("message", gameName + " 게임은 지원하지 않습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
            GameVectorService gameService = vectorServiceFactory.getService(gameName);
            gameService.updateSuccessMetric(knowledgeId, successMetric);
            
            response.put("success", true);
            response.put("message", "지식 성공률 업데이트 완료");
            response.put("knowledgeId", knowledgeId);
            response.put("newSuccessMetric", successMetric);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("{} 지식 성공률 업데이트 실패", gameName, e);
            response.put("success", false);
            response.put("message", "성공률 업데이트 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 지원하는 게임 목록 조회
     */
    @GetMapping("/supported-games")
    public ResponseEntity<Map<String, Object>> getSupportedGames() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> supportedGames = vectorServiceFactory.getSupportedGames();
            
            response.put("success", true);
            response.put("supportedGames", supportedGames);
            response.put("totalGames", supportedGames.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("지원 게임 목록 조회 실패", e);
            response.put("success", false);
            response.put("message", "지원 게임 목록 조회 실패: " + e.getMessage());
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
            for (String gameName : supportedGames) {
                gameSupport.put(gameName, vectorServiceFactory.isSupported(gameName));
            }
            response.put("gameSupport", gameSupport);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("벡터 서비스 상태 확인 실패", e);
            response.put("success", false);
            response.put("message", "상태 확인 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 게임별 통계 조회
     */
    @GetMapping("/{gameName}/stats")
    public ResponseEntity<Map<String, Object>> getGameStats(@PathVariable String gameName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!vectorServiceFactory.isSupported(gameName)) {
                response.put("success", false);
                response.put("message", gameName + " 게임은 지원하지 않습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
            GameVectorService gameService = vectorServiceFactory.getService(gameName);
            
            // 게임별 통계 정보 수집
            Map<String, Object> stats = new HashMap<>();
            stats.put("gameName", gameName);
            stats.put("serviceActive", true);
            
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("{} 게임 통계 조회 실패", gameName, e);
            response.put("success", false);
            response.put("message", "통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 