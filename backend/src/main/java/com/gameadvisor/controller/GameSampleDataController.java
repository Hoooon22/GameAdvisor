package com.gameadvisor.controller;

import com.gameadvisor.service.vector.BloonsTDSampleDataService;
import com.gameadvisor.service.vector.MasterDuelSampleDataService;
import com.gameadvisor.service.vector.GameVectorServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sample-data")
@CrossOrigin(origins = "*")
public class GameSampleDataController {
    
    private final BloonsTDSampleDataService btdSampleDataService;
    private final MasterDuelSampleDataService masterDuelSampleDataService;
    private final GameVectorServiceFactory vectorServiceFactory;
    
    @Autowired
    public GameSampleDataController(BloonsTDSampleDataService btdSampleDataService,
                                   MasterDuelSampleDataService masterDuelSampleDataService,
                                   GameVectorServiceFactory vectorServiceFactory) {
        this.btdSampleDataService = btdSampleDataService;
        this.masterDuelSampleDataService = masterDuelSampleDataService;
        this.vectorServiceFactory = vectorServiceFactory;
    }
    
    /**
     * 게임별 샘플 데이터 생성
     */
    @PostMapping("/{gameName}/create")
    public ResponseEntity<Map<String, Object>> createSampleData(@PathVariable String gameName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("{} 샘플 데이터 생성 요청", gameName);
            
            if (!vectorServiceFactory.isSupported(gameName)) {
                response.put("success", false);
                response.put("message", gameName + " 게임은 지원하지 않습니다");
                response.put("supportedGames", vectorServiceFactory.getSupportedGames());
                return ResponseEntity.badRequest().body(response);
            }
            
            long beforeCount = 0;
            long afterCount = 0;
            
            switch (gameName.toLowerCase()) {
                case "bloonstd":
                    beforeCount = btdSampleDataService.getKnowledgeCount();
                    btdSampleDataService.createSampleData();
                    afterCount = btdSampleDataService.getKnowledgeCount();
                    break;
                    
                case "masterduel":
                    beforeCount = masterDuelSampleDataService.getKnowledgeCount();
                    masterDuelSampleDataService.createSampleData();
                    afterCount = masterDuelSampleDataService.getKnowledgeCount();
                    break;
                    
                default:
                    response.put("success", false);
                    response.put("message", gameName + " 게임의 샘플 데이터 생성이 구현되지 않았습니다");
                    return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", gameName + " 샘플 데이터 생성 완료");
            response.put("gameName", gameName);
            response.put("beforeCount", beforeCount);
            response.put("afterCount", afterCount);
            response.put("createdCount", afterCount - beforeCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("{} 샘플 데이터 생성 실패", gameName, e);
            response.put("success", false);
            response.put("message", "샘플 데이터 생성 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 게임별 데이터 개수 조회
     */
    @GetMapping("/{gameName}/count")
    public ResponseEntity<Map<String, Object>> getDataCount(@PathVariable String gameName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!vectorServiceFactory.isSupported(gameName)) {
                response.put("success", false);
                response.put("message", gameName + " 게임은 지원하지 않습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
            long count = 0;
            
            switch (gameName.toLowerCase()) {
                case "bloonstd":
                    count = btdSampleDataService.getKnowledgeCount();
                    break;
                    
                case "masterduel":
                    count = masterDuelSampleDataService.getKnowledgeCount();
                    break;
                    
                default:
                    response.put("success", false);
                    response.put("message", gameName + " 게임의 데이터 개수 조회가 구현되지 않았습니다");
                    return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("gameName", gameName);
            response.put("totalCount", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("{} 데이터 개수 조회 실패", gameName, e);
            response.put("success", false);
            response.put("message", "데이터 개수 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 게임별 데이터 삭제
     */
    @DeleteMapping("/{gameName}/clear")
    public ResponseEntity<Map<String, Object>> clearData(@PathVariable String gameName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("{} 데이터 삭제 요청", gameName);
            
            if (!vectorServiceFactory.isSupported(gameName)) {
                response.put("success", false);
                response.put("message", gameName + " 게임은 지원하지 않습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
            long beforeCount = 0;
            long afterCount = 0;
            
            switch (gameName.toLowerCase()) {
                case "bloonstd":
                    beforeCount = btdSampleDataService.getKnowledgeCount();
                    btdSampleDataService.clearAllData();
                    afterCount = btdSampleDataService.getKnowledgeCount();
                    break;
                    
                case "masterduel":
                    beforeCount = masterDuelSampleDataService.getKnowledgeCount();
                    masterDuelSampleDataService.clearAllData();
                    afterCount = masterDuelSampleDataService.getKnowledgeCount();
                    break;
                    
                default:
                    response.put("success", false);
                    response.put("message", gameName + " 게임의 데이터 삭제가 구현되지 않았습니다");
                    return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", gameName + " 데이터 삭제 완료");
            response.put("gameName", gameName);
            response.put("beforeCount", beforeCount);
            response.put("afterCount", afterCount);
            response.put("deletedCount", beforeCount - afterCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("{} 데이터 삭제 실패", gameName, e);
            response.put("success", false);
            response.put("message", "데이터 삭제 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 모든 게임의 샘플 데이터 생성
     */
    @PostMapping("/create-all")
    public ResponseEntity<Map<String, Object>> createAllSampleData() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        
        try {
            log.info("모든 게임의 샘플 데이터 생성 요청");
            
            // BloonsTD 샘플 데이터 생성
            try {
                long btdBefore = btdSampleDataService.getKnowledgeCount();
                btdSampleDataService.createSampleData();
                long btdAfter = btdSampleDataService.getKnowledgeCount();
                
                results.put("BloonsTD", Map.of(
                    "success", true,
                    "beforeCount", btdBefore,
                    "afterCount", btdAfter,
                    "createdCount", btdAfter - btdBefore
                ));
            } catch (Exception e) {
                results.put("BloonsTD", Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
            }
            
            // MasterDuel 샘플 데이터 생성
            try {
                long mdBefore = masterDuelSampleDataService.getKnowledgeCount();
                masterDuelSampleDataService.createSampleData();
                long mdAfter = masterDuelSampleDataService.getKnowledgeCount();
                
                results.put("MasterDuel", Map.of(
                    "success", true,
                    "beforeCount", mdBefore,
                    "afterCount", mdAfter,
                    "createdCount", mdAfter - mdBefore
                ));
            } catch (Exception e) {
                results.put("MasterDuel", Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
            }
            
            response.put("success", true);
            response.put("message", "모든 게임 샘플 데이터 생성 완료");
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("모든 게임 샘플 데이터 생성 실패", e);
            response.put("success", false);
            response.put("message", "샘플 데이터 생성 실패: " + e.getMessage());
            response.put("results", results);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 모든 게임의 데이터 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAllStats() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<String> supportedGames = vectorServiceFactory.getSupportedGames();
            
            // 각 게임별 통계 조회
            for (String gameName : supportedGames) {
                try {
                    long count = 0;
                    
                    switch (gameName.toLowerCase()) {
                        case "bloonstd":
                            count = btdSampleDataService.getKnowledgeCount();
                            break;
                            
                        case "masterduel":
                            count = masterDuelSampleDataService.getKnowledgeCount();
                            break;
                    }
                    
                    stats.put(gameName, Map.of(
                        "totalCount", count,
                        "status", "active"
                    ));
                } catch (Exception e) {
                    stats.put(gameName, Map.of(
                        "totalCount", 0,
                        "status", "error",
                        "error", e.getMessage()
                    ));
                }
            }
            
            response.put("success", true);
            response.put("supportedGames", supportedGames);
            response.put("gameStats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("전체 통계 조회 실패", e);
            response.put("success", false);
            response.put("message", "통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 