package com.gameadvisor.service;

import com.gameadvisor.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class GeminiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url}")
    private String apiUrl;
    
    @Value("${gemini.api.timeout:30000}")
    private long timeoutMs;
    
    public GeminiService() {
        this.restTemplate = new RestTemplate();
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