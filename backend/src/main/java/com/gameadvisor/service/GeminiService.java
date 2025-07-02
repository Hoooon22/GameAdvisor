package com.gameadvisor.service;

import com.gameadvisor.model.*;
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
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url}")
    private String apiUrl;
    
    @Value("${gemini.api.timeout:30000}")
    private long timeoutMs;
    
    @Autowired
    public GeminiService(WebSearchService webSearchService) {
        this.restTemplate = new RestTemplate();
        this.webSearchService = webSearchService;
        this.objectMapper = new ObjectMapper();
    }
    
    public GameAdviceResponse getGameAdvice(GameAdviceRequest request) {
        try {
            log.info("게임 조언 요청: 게임={}, 상황={}", request.getGameName(), request.getCurrentSituation());
            
            String prompt = buildGameAdvicePrompt(request);
            GeminiRequest geminiRequest = buildGeminiRequest(prompt);
            
            GeminiResponse geminiResponse = callGeminiApi(geminiRequest);
            
            String advice = extractAdviceFromResponse(geminiResponse);
            
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
     * 화면 분석 및 조언 제공 (웹 검색 포함)
     */
    public ScreenAnalysisResponse analyzeScreen(ScreenAnalysisRequest request) {
        try {
            log.info("화면 분석 요청: 게임={}", request.getGameName());
            
            // 1단계: 기본 화면 분석
            String initialPrompt = buildInitialAnalysisPrompt(request);
            GeminiRequest initialRequest = buildGeminiImageRequest(initialPrompt, request.getImageBase64());
            GeminiResponse initialResponse = callGeminiApi(initialRequest);
            String initialAnalysis = extractAdviceFromResponse(initialResponse);
            
            // 2단계: 화면 분석 결과를 바탕으로 웹 검색 키워드 추출
            List<String> searchQueries = extractSearchQueries(initialAnalysis, request.getGameName());
            
            // 3단계: 웹 검색 수행
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
            
            // 4단계: 화면 분석 + 웹 검색 결과를 조합한 최종 분석
            String enhancedPrompt = buildEnhancedAnalysisPrompt(request, initialAnalysis, searchResults);
            GeminiRequest enhancedRequest = buildGeminiImageRequest(enhancedPrompt, request.getImageBase64());
            GeminiResponse enhancedResponse = callGeminiApi(enhancedRequest);
            String finalAnalysis = extractAdviceFromResponse(enhancedResponse);
            
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
    
    private String buildInitialAnalysisPrompt(ScreenAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 게임 전문가이며 친근한 게임 어드바이저입니다. ");
        prompt.append("제공된 게임 화면을 분석하고 현재 상황을 파악해주세요.\n\n");
        
        if (request.getGameName() != null && !request.getGameName().isEmpty()) {
            prompt.append("게임: ").append(request.getGameName()).append("\n");
        }
        
        if (request.getAdditionalContext() != null && !request.getAdditionalContext().isEmpty()) {
            prompt.append("추가 정보: ").append(request.getAdditionalContext()).append("\n");
        }
        
        prompt.append("\n분석 요청:\n");
        prompt.append("1. 현재 화면에서 무엇이 일어나고 있는지 설명해주세요.\n");
        prompt.append("2. 게임 상황, 레벨, 라운드, 캐릭터, 아이템 등을 구체적으로 식별해주세요.\n");
        prompt.append("3. 플레이어가 직면한 문제나 상황을 파악해주세요.\n\n");
        prompt.append("응답은 간결하고 구체적으로 작성해주세요.");
        
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
    
    private String buildEnhancedAnalysisPrompt(ScreenAnalysisRequest request, String initialAnalysis, List<WebSearchResponse.SearchResult> searchResults) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 게임 전문가이며 친근한 게임 어드바이저입니다. ");
        prompt.append("화면 분석과 웹 검색 결과를 종합하여 최고의 공략 조언을 제공해주세요.\n\n");
        
        if (request.getGameName() != null && !request.getGameName().isEmpty()) {
            prompt.append("게임: ").append(request.getGameName()).append("\n");
        }
        
        prompt.append("\n초기 화면 분석:\n");
        prompt.append(initialAnalysis).append("\n\n");
        
        if (!searchResults.isEmpty()) {
            prompt.append("관련 공략 정보:\n");
            for (int i = 0; i < Math.min(searchResults.size(), 5); i++) {
                WebSearchResponse.SearchResult result = searchResults.get(i);
                prompt.append("- ").append(result.getTitle()).append(": ");
                prompt.append(result.getSnippet()).append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("최종 분석 요청:\n");
        prompt.append("1. 현재 상황에 대한 구체적인 분석\n");
        prompt.append("2. 다음에 해야 할 행동의 우선순위\n");
        prompt.append("3. 웹 검색 결과를 활용한 전략적 조언\n");
        prompt.append("4. 주의사항 및 팁\n\n");
        prompt.append("친근한 말투로 실용적이고 구체적인 조언을 400자 이내로 작성해주세요.");
        
        return prompt.toString();
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