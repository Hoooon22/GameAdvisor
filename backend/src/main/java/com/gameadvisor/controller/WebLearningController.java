package com.gameadvisor.controller;

import com.gameadvisor.service.vector.WebDataCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/web-learning")
@CrossOrigin(origins = "*")
public class WebLearningController {
    
    private final WebDataCollectionService webDataCollectionService;
    
    @Autowired
    public WebLearningController(WebDataCollectionService webDataCollectionService) {
        this.webDataCollectionService = webDataCollectionService;
    }
    
    /**
     * 인터넷에서 BTD 관련 자료를 수집하고 벡터 DB에 학습시킵니다.
     */
    @PostMapping("/collect-all")
    public ResponseEntity<Map<String, Object>> collectAllWebData() {
        log.info("웹 자료 전체 수집 요청");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 비동기로 수집 작업 실행
            CompletableFuture.runAsync(() -> {
                webDataCollectionService.collectAndLearnFromWeb();
            });
            
            response.put("success", true);
            response.put("message", "웹 자료 수집이 백그라운드에서 시작되었습니다.");
            response.put("status", "processing");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("웹 자료 수집 시작 실패: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "웹 자료 수집 시작에 실패했습니다: " + e.getMessage());
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 특정 주제에 대한 웹 자료를 수집하고 학습시킵니다.
     */
    @PostMapping("/collect-topic")
    public ResponseEntity<Map<String, Object>> collectTopicData(@RequestParam String topic) {
        log.info("특정 주제 웹 자료 수집 요청: {}", topic);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (topic == null || topic.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "주제가 제공되지 않았습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 비동기로 주제별 수집 작업 실행
            CompletableFuture.runAsync(() -> {
                webDataCollectionService.collectSpecificTopic(topic);
            });
            
            response.put("success", true);
            response.put("message", "주제 '" + topic + "'에 대한 웹 자료 수집이 시작되었습니다.");
            response.put("topic", topic);
            response.put("status", "processing");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("주제별 웹 자료 수집 시작 실패: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "주제별 웹 자료 수집 시작에 실패했습니다: " + e.getMessage());
            response.put("error", e.getMessage());
            response.put("topic", topic);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 웹 학습 상태를 확인합니다.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("message", "웹 학습 서비스가 정상 작동 중입니다.");
            response.put("available_topics", new String[]{
                "ceramic defense", "MOAB strategy", "camo detection", 
                "boss battle", "tower combinations", "economy build",
                "late game strategy", "hero abilities"
            });
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("상태 확인 실패: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "서비스 상태 확인에 실패했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 특정 키워드로 즉시 웹 자료를 수집합니다.
     */
    @PostMapping("/collect-keyword")
    public ResponseEntity<Map<String, Object>> collectByKeyword(@RequestParam String keyword) {
        log.info("키워드 기반 웹 자료 수집 요청: {}", keyword);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "키워드가 제공되지 않았습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // BTD 관련 키워드로 강화
            String enhancedKeyword = "BTD6 " + keyword + " strategy guide";
            
            // 비동기로 키워드별 수집 작업 실행
            CompletableFuture.runAsync(() -> {
                webDataCollectionService.collectSpecificTopic(enhancedKeyword);
            });
            
            response.put("success", true);
            response.put("message", "키워드 '" + keyword + "'에 대한 웹 자료 수집이 시작되었습니다.");
            response.put("keyword", keyword);
            response.put("enhanced_keyword", enhancedKeyword);
            response.put("status", "processing");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("키워드별 웹 자료 수집 시작 실패: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "키워드별 웹 자료 수집 시작에 실패했습니다: " + e.getMessage());
            response.put("error", e.getMessage());
            response.put("keyword", keyword);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 사용자가 직접 지정한 URL에서 웹 자료를 수집합니다.
     */
    @PostMapping("/collect-url")
    public ResponseEntity<Map<String, Object>> collectFromUrl(@RequestParam String url, 
                                                             @RequestParam(required = false) String category) {
        log.info("URL 직접 수집 요청: {}", url);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (url == null || url.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "URL이 제공되지 않았습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // URL 형식 검증
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            
            // 카테고리가 없으면 기본값 설정
            if (category == null || category.trim().isEmpty()) {
                category = "사용자 지정";
            }
            
            // 최종 URL과 카테고리 설정
            final String finalUrl = url;
            final String finalCategory = category;
            
            // 비동기로 URL 수집 작업 실행
            CompletableFuture.runAsync(() -> {
                webDataCollectionService.collectFromSpecificUrl(finalUrl, finalCategory);
            });
            
            response.put("success", true);
            response.put("message", "지정된 URL에서 웹 자료 수집이 시작되었습니다.");
            response.put("url", finalUrl);
            response.put("category", finalCategory);
            response.put("status", "processing");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("URL 직접 수집 시작 실패: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "URL 수집 시작에 실패했습니다: " + e.getMessage());
            response.put("error", e.getMessage());
            response.put("url", url);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 여러 URL을 한 번에 수집합니다.
     */
    @PostMapping("/collect-urls")
    public ResponseEntity<Map<String, Object>> collectFromUrls(@RequestBody List<String> urls,
                                                              @RequestParam(required = false) String category) {
        log.info("다중 URL 수집 요청: {} 개 URL", urls.size());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (urls == null || urls.isEmpty()) {
                response.put("success", false);
                response.put("message", "URL 목록이 제공되지 않았습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 카테고리가 없으면 기본값 설정
            if (category == null || category.trim().isEmpty()) {
                category = "사용자 지정 다중";
            }
            
            // URL 형식 검증 및 정규화
            List<String> normalizedUrls = new ArrayList<>();
            for (String url : urls) {
                if (url != null && !url.trim().isEmpty()) {
                    String normalizedUrl = url.trim();
                    if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
                        normalizedUrl = "https://" + normalizedUrl;
                    }
                    normalizedUrls.add(normalizedUrl);
                }
            }
            
            final String finalCategory = category;
            
            // 비동기로 다중 URL 수집 작업 실행
            CompletableFuture.runAsync(() -> {
                webDataCollectionService.collectFromMultipleUrls(normalizedUrls, finalCategory);
            });
            
            response.put("success", true);
            response.put("message", normalizedUrls.size() + "개의 URL에서 웹 자료 수집이 시작되었습니다.");
            response.put("urls", normalizedUrls);
            response.put("url_count", normalizedUrls.size());
            response.put("category", finalCategory);
            response.put("status", "processing");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("다중 URL 수집 시작 실패: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "다중 URL 수집 시작에 실패했습니다: " + e.getMessage());
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 사이트를 깊이 크롤링하여 하위 페이지들도 함께 수집합니다.
     */
    @PostMapping("/collect-site-deep")
    public ResponseEntity<Map<String, Object>> collectSiteDeep(@RequestParam String baseUrl,
                                                              @RequestParam(required = false) String category,
                                                              @RequestParam(required = false, defaultValue = "2") int maxDepth,
                                                              @RequestParam(required = false, defaultValue = "10") int maxPages) {
        log.info("사이트 깊이 크롤링 요청: {} (깊이: {}, 최대 페이지: {})", baseUrl, maxDepth, maxPages);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "기본 URL이 제공되지 않았습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // URL 형식 검증
            if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
                baseUrl = "https://" + baseUrl;
            }
            
            // 카테고리가 없으면 기본값 설정
            if (category == null || category.trim().isEmpty()) {
                category = "사이트 깊이 수집";
            }
            
            // 매개변수 검증
            if (maxDepth < 1 || maxDepth > 5) {
                maxDepth = 2; // 기본값으로 제한
            }
            if (maxPages < 1 || maxPages > 50) {
                maxPages = 10; // 기본값으로 제한
            }
            
            final String finalBaseUrl = baseUrl;
            final String finalCategory = category;
            final int finalMaxDepth = maxDepth;
            final int finalMaxPages = maxPages;
            
            // 비동기로 깊이 크롤링 작업 실행
            CompletableFuture.runAsync(() -> {
                webDataCollectionService.collectSiteDeep(finalBaseUrl, finalCategory, finalMaxDepth, finalMaxPages);
            });
            
            response.put("success", true);
            response.put("message", "사이트 깊이 크롤링이 시작되었습니다.");
            response.put("base_url", finalBaseUrl);
            response.put("category", finalCategory);
            response.put("max_depth", finalMaxDepth);
            response.put("max_pages", finalMaxPages);
            response.put("status", "processing");
            response.put("estimated_time", "몇 분에서 최대 10분 정도 소요될 수 있습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("사이트 깊이 크롤링 시작 실패: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "사이트 깊이 크롤링 시작에 실패했습니다: " + e.getMessage());
            response.put("error", e.getMessage());
            response.put("base_url", baseUrl);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 도움말 정보를 제공합니다.
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> endpoints = new HashMap<>();
        
        endpoints.put("POST /collect-all", "모든 BTD 관련 웹 자료를 수집하고 학습시킵니다.");
        endpoints.put("POST /collect-topic?topic={topic}", "특정 주제에 대한 웹 자료를 수집합니다.");
        endpoints.put("POST /collect-keyword?keyword={keyword}", "특정 키워드로 웹 자료를 수집합니다.");
        endpoints.put("POST /collect-url?url={url}&category={category}", "사용자가 지정한 URL에서 웹 자료를 수집합니다.");
        endpoints.put("POST /collect-urls (JSON body)", "여러 URL을 한 번에 수집합니다.");
        endpoints.put("POST /collect-site-deep?baseUrl={url}&category={category}&maxDepth={depth}&maxPages={pages}", "사이트를 깊이 크롤링하여 하위 페이지들도 함께 수집합니다.");
        endpoints.put("GET /status", "웹 학습 서비스 상태를 확인합니다.");
        endpoints.put("GET /help", "사용 가능한 API 목록을 보여줍니다.");
        
        response.put("success", true);
        response.put("service", "웹 자료 수집 및 학습 서비스");
        response.put("description", "인터넷에서 게임 관련 자료를 수집하여 벡터 DB에 학습시키는 서비스입니다.");
        response.put("endpoints", endpoints);
        response.put("recommended_topics", new String[]{
            "ceramic defense", "MOAB popping", "camo detection", "lead popping",
            "boss battle", "tower combinations", "economy strategy", "late game",
            "hero guides", "monkey knowledge"
        });
        response.put("url_examples", new String[]{
            "https://bloons.fandom.com/wiki/Strategies",
            "https://www.reddit.com/r/btd6/comments/strategy_guide",
            "https://steamcommunity.com/app/960090/guides"
        });
        response.put("deep_crawling_info", Map.of(
            "description", "깊이 크롤링은 지정된 사이트의 하위 페이지들을 자동으로 탐색하여 수집합니다.",
            "max_depth_limit", "최대 5단계까지 탐색 가능",
            "max_pages_limit", "최대 50페이지까지 수집 가능",
            "filtering", "같은 도메인 내의 관련 페이지만 수집하며, 이미지/로그인 페이지 등은 제외"
        ));
        
        return ResponseEntity.ok(response);
    }
} 