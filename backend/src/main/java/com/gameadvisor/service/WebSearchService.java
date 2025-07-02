package com.gameadvisor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameadvisor.model.WebSearchRequest;
import com.gameadvisor.model.WebSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WebSearchService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // DuckDuckGo Instant Answer API 사용
    private static final String DUCKDUCKGO_API_URL = "https://api.duckduckgo.com/";
    
    public WebSearchService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    public WebSearchResponse searchWeb(WebSearchRequest request) {
        try {
            log.info("웹 검색 요청: 쿼리={}, 게임={}", request.getQuery(), request.getGameName());
            
            // 게임 이름과 함께 검색 쿼리 생성
            String enhancedQuery = buildEnhancedQuery(request);
            
            // DuckDuckGo API 호출
            String searchUrl = buildSearchUrl(enhancedQuery);
            String response = restTemplate.getForObject(searchUrl, String.class);
            
            // 응답 파싱
            List<WebSearchResponse.SearchResult> results = parseSearchResponse(response);
            
            return WebSearchResponse.builder()
                    .query(enhancedQuery)
                    .results(results)
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("웹 검색 중 오류 발생: {}", e.getMessage(), e);
            
            return WebSearchResponse.builder()
                    .query(request.getQuery())
                    .results(new ArrayList<>())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
    
    private String buildEnhancedQuery(WebSearchRequest request) {
        StringBuilder query = new StringBuilder();
        
        // 게임 이름 추가
        if (request.getGameName() != null && !request.getGameName().isEmpty()) {
            query.append(request.getGameName()).append(" ");
        }
        
        // 검색 유형에 따른 키워드 추가
        if (request.getSearchType() != null) {
            switch (request.getSearchType().toLowerCase()) {
                case "strategy":
                    query.append("strategy guide ");
                    break;
                case "guide":
                    query.append("guide tutorial ");
                    break;
                case "tips":
                    query.append("tips tricks ");
                    break;
                case "meta":
                    query.append("meta build ");
                    break;
                default:
                    query.append("guide ");
                    break;
            }
        }
        
        // 원본 쿼리 추가
        query.append(request.getQuery());
        
        return query.toString().trim();
    }
    
    private String buildSearchUrl(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        return DUCKDUCKGO_API_URL + "?q=" + encodedQuery + "&format=json&no_html=1&skip_disambig=1";
    }
    
    private List<WebSearchResponse.SearchResult> parseSearchResponse(String response) {
        List<WebSearchResponse.SearchResult> results = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(response);
            
            // DuckDuckGo Abstract 결과 처리
            JsonNode abstractNode = root.get("Abstract");
            if (abstractNode != null && !abstractNode.asText().isEmpty()) {
                results.add(WebSearchResponse.SearchResult.builder()
                        .title("요약 정보")
                        .snippet(abstractNode.asText())
                        .url(root.path("AbstractURL").asText())
                        .displayUrl("DuckDuckGo")
                        .build());
            }
            
            // Related Topics 처리
            JsonNode relatedTopics = root.get("RelatedTopics");
            if (relatedTopics != null && relatedTopics.isArray()) {
                for (JsonNode topic : relatedTopics) {
                    if (results.size() >= 5) break; // 최대 5개 결과만
                    
                    String text = topic.path("Text").asText();
                    String url = topic.path("FirstURL").asText();
                    
                    if (!text.isEmpty() && !url.isEmpty()) {
                        results.add(WebSearchResponse.SearchResult.builder()
                                .title(extractTitle(text))
                                .snippet(text)
                                .url(url)
                                .displayUrl(extractDisplayUrl(url))
                                .build());
                    }
                }
            }
            
            // Answer 처리 (즉석 답변)
            JsonNode answer = root.get("Answer");
            if (answer != null && !answer.asText().isEmpty()) {
                results.add(0, WebSearchResponse.SearchResult.builder()
                        .title("즉석 답변")
                        .snippet(answer.asText())
                        .url("")
                        .displayUrl("DuckDuckGo")
                        .build());
            }
            
        } catch (Exception e) {
            log.error("검색 응답 파싱 중 오류 발생: {}", e.getMessage());
        }
        
        return results;
    }
    
    private String extractTitle(String text) {
        // 첫 번째 문장이나 첫 30글자를 제목으로 사용
        if (text.length() > 50) {
            int firstPeriod = text.indexOf('.');
            if (firstPeriod > 0 && firstPeriod < 50) {
                return text.substring(0, firstPeriod);
            }
            return text.substring(0, 47) + "...";
        }
        return text;
    }
    
    private String extractDisplayUrl(String url) {
        try {
            if (url.startsWith("http")) {
                String domain = url.replaceFirst("^https?://", "");
                int slashIndex = domain.indexOf('/');
                if (slashIndex > 0) {
                    domain = domain.substring(0, slashIndex);
                }
                return domain;
            }
            return url;
        } catch (Exception e) {
            return "Unknown";
        }
    }
} 