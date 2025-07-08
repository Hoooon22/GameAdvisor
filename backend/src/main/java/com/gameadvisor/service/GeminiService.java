package com.gameadvisor.service;

import com.gameadvisor.model.*;
import com.gameadvisor.model.vector.VectorSearchResult;
import com.gameadvisor.service.vector.GameVectorServiceFactory;
import com.gameadvisor.service.vector.GameVectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
public class GeminiService {
    
    private final RestTemplate restTemplate;
    private final WebSearchService webSearchService;
    private final ObjectMapper objectMapper;
    private final GameVectorServiceFactory vectorServiceFactory;
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url}")
    private String apiUrl;
    
    @Value("${gemini.api.timeout:30000}")
    private long timeoutMs;
    
    @Autowired
    public GeminiService(WebSearchService webSearchService, GameVectorServiceFactory vectorServiceFactory) {
        this.restTemplate = new RestTemplate();
        this.webSearchService = webSearchService;
        this.objectMapper = new ObjectMapper();
        this.vectorServiceFactory = vectorServiceFactory;
    }
    
    public GameAdviceResponse getGameAdvice(GameAdviceRequest request) {
        try {
            log.info("게임 조언 요청: 게임={}, 상황={}", request.getGameName(), request.getCurrentSituation());
            
            // 1단계: 벡터 DB에서 유사한 상황 검색
            List<VectorSearchResult> vectorResults = searchVectorKnowledge(request);
            
            // 2단계: 벡터 검색 결과와 함께 프롬프트 구성
            String prompt;
            if (!vectorResults.isEmpty()) {
                prompt = buildEnhancedGameAdvicePrompt(request, vectorResults);
                log.info("벡터 DB에서 {} 개의 유사 상황 발견", vectorResults.size());
            } else {
                prompt = buildGameAdvicePrompt(request);
                log.info("벡터 DB에서 유사 상황을 찾지 못함. 기본 프롬프트 사용");
            }
            
            GeminiRequest geminiRequest = buildGeminiRequest(prompt);
            GeminiResponse geminiResponse = callGeminiApi(geminiRequest);
            String advice = extractAdviceFromResponse(geminiResponse);
            
            // 3단계: 사용된 벡터 지식의 사용량 증가
            updateVectorKnowledgeUsage(vectorResults);
            
            return GameAdviceResponse.builder()
                    .advice(advice)
                    .characterName("게임 어드바이저")
                    .gameContext(request.getGameName())
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("게임 조언 생성 중 오류 발생", e);
            
            return GameAdviceResponse.builder()
                    .advice("죄송합니다. 현재 조언을 생성할 수 없습니다.")
                    .characterName("게임 어드바이저")
                    .gameContext(request.getGameName())
                    .timestamp(LocalDateTime.now())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
    
    /**
     * 화면 분석 및 조언 제공 (벡터 DB + 웹 검색 포함)
     */
    public ScreenAnalysisResponse analyzeScreen(ScreenAnalysisRequest request) {
        try {
            log.info("화면 분석 요청: 게임={}", request.getGameName());
            
            // 1단계: 기본 화면 분석으로 현재 상황 파악
            String initialPrompt = buildInitialAnalysisPrompt(request);
            GeminiRequest initialRequest = buildGeminiImageRequest(initialPrompt, request.getImageBase64());
            GeminiResponse initialResponse = callGeminiApi(initialRequest);
            String initialAnalysis = extractAdviceFromResponse(initialResponse);
            
            log.info("화면 분석 완료: {}", initialAnalysis.substring(0, Math.min(100, initialAnalysis.length())));
            
            // 2단계: 화면 분석 결과를 바탕으로 벡터 DB에서 유사한 상황 검색
            List<VectorSearchResult> vectorResults = searchVectorKnowledgeForScreen(request.getGameName(), initialAnalysis);
            
            // 3단계: 화면 분석 결과를 바탕으로 웹 검색 키워드 추출
            List<String> searchQueries = extractSearchQueries(initialAnalysis, request.getGameName());
            
            // 4단계: 웹 검색 수행
            List<WebSearchResponse.SearchResult> searchResults = new ArrayList<>();
            for (String query : searchQueries) {
                WebSearchRequest searchRequest = WebSearchRequest.builder()
                        .query(query)
                        .gameName(request.getGameName())
                        .searchType("strategy")
                        .maxResults(3)
                        .build();
                
                WebSearchResponse searchResponse = webSearchService.searchWeb(searchRequest);
                if (searchResponse.isSuccess()) {
                    searchResults.addAll(searchResponse.getResults());
                }
            }
            
            // 5단계: 화면 분석 + 벡터 검색 결과 + 웹 검색 결과를 조합한 최종 분석
            String enhancedPrompt = buildComprehensiveAnalysisPrompt(request, initialAnalysis, vectorResults, searchResults);
            GeminiRequest enhancedRequest = buildGeminiImageRequest(enhancedPrompt, request.getImageBase64());
            GeminiResponse enhancedResponse = callGeminiApi(enhancedRequest);
            String finalAnalysis = extractAdviceFromResponse(enhancedResponse);
            
            // 6단계: 사용된 벡터 지식의 사용량 증가
            updateVectorKnowledgeUsage(vectorResults);
            
            log.info("최종 분석 완료 - 벡터 검색: {}개, 웹 검색: {}개 결과 활용", 
                    vectorResults.size(), searchResults.size());
            
            return ScreenAnalysisResponse.builder()
                    .analysis(finalAnalysis)
                    .advice(finalAnalysis)
                    .characterName("게임 어드바이저")
                    .gameContext(request.getGameName())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("화면 분석 중 오류 발생", e);
            
            return ScreenAnalysisResponse.builder()
                    .analysis("죄송합니다. 현재 화면을 분석할 수 없습니다.")
                    .advice("다시 시도해 주세요.")
                    .characterName("게임 어드바이저")
                    .gameContext(request.getGameName())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
    
    /**
     * 초기 화면 분석을 위한 프롬프트 생성
     */
    private String buildInitialAnalysisPrompt(ScreenAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("게임 화면을 분석하여 현재 상황을 정확히 파악해주세요.\n\n");
        
        if (request.getGameName() != null && !request.getGameName().isEmpty()) {
            prompt.append("게임: ").append(request.getGameName()).append("\n\n");
        }
        
        prompt.append("다음 요소들을 중심으로 현재 상황을 분석해주세요:\n");
        prompt.append("1. 현재 라운드/레벨/스테이지\n");
        prompt.append("2. 보유 자원(골드, 경험치, 재료 등)\n");
        prompt.append("3. 배치된 유닛/건물/캐릭터 현황\n");
        prompt.append("4. 현재 진행 중인 상황 (전투, 준비 단계 등)\n");
        prompt.append("5. 화면에서 관찰되는 위험 요소나 기회\n\n");
        prompt.append("분석 결과는 150자 이내로 간결하고 객관적으로 작성해주세요. ");
        prompt.append("현재 상황에 대한 사실만 기술하고, 조언이나 의견은 포함하지 마세요.");
        
        return prompt.toString();
    }
    
    private List<String> extractSearchQueries(String analysis, String gameName) {
        List<String> queries = new ArrayList<>();
        
        // 분석 결과에서 검색 키워드 추출 로직
        String lowerAnalysis = analysis.toLowerCase();
        
        // 게임별 특정 키워드 검색
        if (gameName != null) {
            if (gameName.toLowerCase().contains("bloon") || gameName.toLowerCase().contains("td")) {
                if (lowerAnalysis.contains("라운드") || lowerAnalysis.contains("round")) {
                    queries.add("round strategy");
                }
                if (lowerAnalysis.contains("타워") || lowerAnalysis.contains("tower")) {
                    queries.add("tower guide");
                }
                if (lowerAnalysis.contains("업그레이드")) {
                    queries.add("upgrade guide");
                }
            } else if (gameName.toLowerCase().contains("master duel")) {
                if (lowerAnalysis.contains("카드") || lowerAnalysis.contains("card")) {
                    queries.add("card combo guide");
                }
                if (lowerAnalysis.contains("덱") || lowerAnalysis.contains("deck")) {
                    queries.add("deck build guide");
                }
                if (lowerAnalysis.contains("듀얼") || lowerAnalysis.contains("duel")) {
                    queries.add("duel strategy");
                }
            }
        }
        
        // 일반적인 게임 키워드
        if (lowerAnalysis.contains("전략") || lowerAnalysis.contains("strategy")) {
            queries.add("strategy tips");
        }
        if (lowerAnalysis.contains("공략") || lowerAnalysis.contains("guide")) {
            queries.add("walkthrough guide");
        }
        
        // 최소 1개 쿼리 보장
        if (queries.isEmpty()) {
            queries.add("tips tricks");
        }
        
        // 최대 3개로 제한
        return queries.subList(0, Math.min(queries.size(), 3));
    }
    
    private GeminiRequest buildGeminiImageRequest(String prompt, String imageBase64) {
        return GeminiRequest.builder()
                .contents(List.of(
                        GeminiRequest.Content.builder()
                                .parts(List.of(
                                        GeminiRequest.Part.builder()
                                                .text(prompt)
                                                .build(),
                                        GeminiRequest.Part.builder()
                                                .inlineData(GeminiRequest.InlineData.builder()
                                                        .mimeType("image/png")
                                                        .data(imageBase64)
                                                        .build())
                                                .build()
                                ))
                                .build()
                ))
                .generationConfig(
                        GeminiRequest.GenerationConfig.builder()
                                .temperature(0.7)
                                .topK(40)
                                .topP(0.8)
                                .maxOutputTokens(500)
                                .build()
                )
                .build();
    }

    
    private String buildGameAdvicePrompt(GameAdviceRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 친근하고 도움이 되는 게임 어드바이저입니다. ");
        prompt.append("플레이어에게 유용한 게임 조언을 제공해주세요.\n\n");
        
        prompt.append("게임 정보:\n");
        prompt.append("- 게임명: ").append(request.getGameName()).append("\n");
        
        if (request.getGameGenre() != null && !request.getGameGenre().isEmpty()) {
            prompt.append("- 장르: ").append(request.getGameGenre()).append("\n");
        }
        
        if (request.getPlayerLevel() != null && !request.getPlayerLevel().isEmpty()) {
            prompt.append("- 플레이어 레벨/경험: ").append(request.getPlayerLevel()).append("\n");
        }
        
        if (request.getCurrentSituation() != null && !request.getCurrentSituation().isEmpty()) {
            prompt.append("- 현재 상황: ").append(request.getCurrentSituation()).append("\n");
        }
        
        if (request.getSpecificQuestion() != null && !request.getSpecificQuestion().isEmpty()) {
            prompt.append("- 구체적인 질문: ").append(request.getSpecificQuestion()).append("\n");
        }
        
        prompt.append("\n조언 요청:\n");
        prompt.append("위 정보를 바탕으로 플레이어에게 도움이 되는 구체적이고 실용적인 조언을 해주세요. ");
        prompt.append("조언은 친근한 말투로 200자 이내로 간결하게 작성해주세요.");
        
        return prompt.toString();
    }
    

    
    private GeminiRequest buildGeminiRequest(String prompt) {
        return GeminiRequest.builder()
                .contents(List.of(
                        GeminiRequest.Content.builder()
                                .parts(List.of(
                                        GeminiRequest.Part.builder()
                                                .text(prompt)
                                                .build()
                                ))
                                .build()
                ))
                .generationConfig(
                        GeminiRequest.GenerationConfig.builder()
                                .temperature(0.7)
                                .topK(40)
                                .topP(0.8)
                                .maxOutputTokens(300)
                                .build()
                )
                .build();
    }
    
    private GeminiResponse callGeminiApi(GeminiRequest request) {
        String url = apiUrl + "?key=" + apiKey;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.postForObject(url, entity, GeminiResponse.class);
    }
    
    // 벡터 DB 관련 메서드들
    
    /**
     * 벡터 DB에서 유사한 게임 지식 검색
     */
    private List<VectorSearchResult> searchVectorKnowledge(GameAdviceRequest request) {
        try {
            if (!vectorServiceFactory.isSupported(request.getGameName())) {
                log.debug("지원하지 않는 게임: {}", request.getGameName());
                return List.of();
            }
            
            GameVectorService vectorService = vectorServiceFactory.getService(request.getGameName());
            return vectorService.searchSimilar(request.getCurrentSituation(), 3);
            
        } catch (Exception e) {
            log.warn("벡터 검색 중 오류 발생: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 화면 분석 결과를 바탕으로 벡터 DB에서 유사한 상황 검색
     */
    private List<VectorSearchResult> searchVectorKnowledgeForScreen(String gameName, String currentSituation) {
        try {
            if (!vectorServiceFactory.isSupported(gameName)) {
                log.debug("지원하지 않는 게임: {}", gameName);
                return List.of();
            }

            GameVectorService vectorService = vectorServiceFactory.getService(gameName);
            return vectorService.searchSimilar(currentSituation, 3);
        } catch (Exception e) {
            log.warn("벡터 검색 중 오류 발생: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 벡터 검색 결과를 포함한 개선된 프롬프트 생성
     */
    private String buildEnhancedGameAdvicePrompt(GameAdviceRequest request, List<VectorSearchResult> vectorResults) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 게임 전문가이며 친근한 게임 어드바이저입니다.\n\n");
        
        prompt.append("게임: ").append(request.getGameName()).append("\n");
        prompt.append("현재 상황: ").append(request.getCurrentSituation()).append("\n\n");
        
        if (request.getPlayerLevel() != null && !request.getPlayerLevel().isEmpty()) {
            prompt.append("플레이어 레벨: ").append(request.getPlayerLevel()).append("\n");
        }
        
        if (request.getSpecificQuestion() != null && !request.getSpecificQuestion().isEmpty()) {
            prompt.append("구체적 질문: ").append(request.getSpecificQuestion()).append("\n");
        }
        
        // 벡터 검색 결과 추가
        prompt.append("\n참고할 만한 검증된 전략들:\n");
        for (int i = 0; i < vectorResults.size(); i++) {
            VectorSearchResult result = vectorResults.get(i);
            prompt.append(String.format("%d. %s\n", i + 1, result.getKnowledge().getTitle()));
            prompt.append(String.format("   전략: %s\n", result.getKnowledge().getAdvice()));
            prompt.append(String.format("   유사도: %.2f\n", result.getSimilarity()));
            if (result.getKnowledge().getSuccessMetric() != null) {
                prompt.append(String.format("   성공률: %.1f%%\n", result.getKnowledge().getSuccessMetric() * 100));
            }
            prompt.append("\n");
        }
        
        prompt.append("위 검증된 전략들을 참고하여 현재 상황에 최적화된 조언을 제공해주세요. ");
        prompt.append("조언은 구체적이고 실행 가능해야 하며, 초보자도 이해할 수 있도록 친근하게 작성해주세요.");
        
        return prompt.toString();
    }

    /**
     * 화면 분석 + 벡터 검색 + 웹 검색 결과를 포함한 최종 분석 프롬프트 생성
     */
    private String buildComprehensiveAnalysisPrompt(ScreenAnalysisRequest request, String initialAnalysis, List<VectorSearchResult> vectorResults, List<WebSearchResponse.SearchResult> searchResults) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 전문 게임 전략가입니다. ");
        prompt.append("화면 분석과 검증된 전략 정보를 바탕으로 즉시 실행 가능한 게임 내 행동만 제시해주세요.\n\n");
        
        if (request.getGameName() != null && !request.getGameName().isEmpty()) {
            prompt.append("게임: ").append(request.getGameName()).append("\n");
        }
        
        prompt.append("\n현재 화면 상황:\n");
        prompt.append(initialAnalysis).append("\n\n");
        
        if (!vectorResults.isEmpty()) {
            prompt.append("검증된 전략:\n");
            for (int i = 0; i < Math.min(vectorResults.size(), 3); i++) {
                VectorSearchResult result = vectorResults.get(i);
                prompt.append(String.format("• %s: %s\n", 
                    result.getKnowledge().getTitle(), 
                    result.getKnowledge().getAdvice()));
            }
            prompt.append("\n");
        }
        
        if (!searchResults.isEmpty()) {
            prompt.append("추가 전략 정보:\n");
            for (int i = 0; i < Math.min(searchResults.size(), 3); i++) {
                WebSearchResponse.SearchResult result = searchResults.get(i);
                prompt.append("• ").append(result.getSnippet()).append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("다음 규칙을 엄격히 따라 조언해주세요:\n");
        prompt.append("1. 게임 내에서 바로 실행할 수 있는 구체적 행동만 제시\n");
        prompt.append("2. 어떤 유닛/건물/아이템을 어디에 배치/구매/사용할지 명확히 지시\n");
        prompt.append("3. 우선순위가 높은 순서로 번호를 매겨 제시\n");
        prompt.append("4. 커뮤니티, 가이드 확인, 연습 등 게임 외부 행동은 절대 언급 금지\n");
        prompt.append("5. 300자 이내로 간결하게 작성\n\n");
        prompt.append("현재 상황에서 즉시 해야 할 게임 내 행동을 구체적으로 알려주세요.");
        
        return prompt.toString();
    }
    
    /**
     * 사용된 벡터 지식의 사용량 업데이트
     */
    private void updateVectorKnowledgeUsage(List<VectorSearchResult> vectorResults) {
        try {
            for (VectorSearchResult result : vectorResults) {
                if (result.getSimilarity() >= 0.7) {  // 유사도가 높은 경우만 사용량 증가
                    String gameName = result.getKnowledge().getGameName();
                    if (vectorServiceFactory.isSupported(gameName)) {
                        GameVectorService vectorService = vectorServiceFactory.getService(gameName);
                        vectorService.incrementUsage(result.getKnowledge().getId());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("벡터 지식 사용량 업데이트 중 오류 발생: {}", e.getMessage());
        }
    }
    
    private String extractAdviceFromResponse(GeminiResponse response) {
        if (response != null && 
            response.getCandidates() != null && 
            !response.getCandidates().isEmpty()) {
            
            GeminiResponse.Candidate candidate = response.getCandidates().get(0);
            if (candidate.getContent() != null && 
                candidate.getContent().getParts() != null && 
                !candidate.getContent().getParts().isEmpty()) {
                
                return candidate.getContent().getParts().get(0).getText();
            }
        }
        
        return "죄송합니다. 조언을 생성할 수 없습니다.";
    }
} 