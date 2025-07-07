package com.gameadvisor.service.vector;

import com.gameadvisor.model.vector.BloonsTDKnowledge;
import com.gameadvisor.model.WebSearchRequest;
import com.gameadvisor.model.WebSearchResponse;
import com.gameadvisor.repository.vector.BloonsTDVectorRepository;
import com.gameadvisor.service.WebSearchService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class WebDataCollectionService {
    
    private final WebSearchService webSearchService;
    private final BloonsTDVectorRepository repository;
    private final RestTemplate restTemplate;
    
    // BTD 관련 검색 키워드
    private static final List<String> BTD_KEYWORDS = Arrays.asList(
        "Bloons TD 6 strategy guide",
        "BTD6 tower combinations",
        "Bloons Tower Defense best strategies", 
        "BTD6 MOAB popping strategy",
        "Bloons TD ceramic defense",
        "BTD6 camo detection guide",
        "Bloons Tower Defense expert tips",
        "BTD6 boss battle strategy",
        "Bloons TD economy guide",
        "BTD6 hero abilities guide"
    );
    
    // 신뢰할 수 있는 BTD 관련 웹사이트
    private static final List<String> TRUSTED_DOMAINS = Arrays.asList(
        "reddit.com/r/btd6",
        "bloons.fandom.com",
        "gamepress.gg",
        "ign.com",
        "steamcommunity.com"
    );
    
    @Autowired
    public WebDataCollectionService(WebSearchService webSearchService, 
                                  BloonsTDVectorRepository repository) {
        this.webSearchService = webSearchService;
        this.repository = repository;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * 인터넷에서 BTD 관련 자료를 수집하고 벡터 DB에 학습시킵니다.
     */
    public void collectAndLearnFromWeb() {
        log.info("웹에서 BTD 자료 수집 및 학습 시작");
        
        int totalCollected = 0;
        int totalLearned = 0;
        
        try {
            for (String keyword : BTD_KEYWORDS) {
                log.info("키워드로 검색 중: {}", keyword);
                
                // 웹 검색 실행
                WebSearchRequest request = WebSearchRequest.builder()
                        .query(keyword)
                        .gameName("BTD6")
                        .searchType("guide")
                        .build();
                
                WebSearchResponse response = webSearchService.searchWeb(request);
                
                if (response.isSuccess() && !response.getResults().isEmpty()) {
                    for (WebSearchResponse.SearchResult result : response.getResults()) {
                        totalCollected++;
                        
                        try {
                            // 신뢰할 수 있는 도메인인지 확인
                            if (isTrustedDomain(result.getUrl())) {
                                // 웹 페이지 내용 수집
                                String content = extractWebContent(result);
                                
                                if (content != null && content.length() > 100) {
                                    // BTD 지식으로 변환하여 저장
                                    BloonsTDKnowledge knowledge = convertToKnowledge(result, content, keyword);
                                    repository.save(knowledge);
                                    totalLearned++;
                                    
                                    log.info("웹 자료 학습 완료: {}", result.getTitle());
                                }
                            }
                        } catch (Exception e) {
                            log.warn("개별 자료 처리 실패: {} - {}", result.getUrl(), e.getMessage());
                        }
                        
                        // API 호출 제한을 위한 대기
                        Thread.sleep(1000);
                    }
                }
                
                // 검색 간 대기
                Thread.sleep(2000);
            }
            
            log.info("웹 자료 수집 완료 - 수집: {} 개, 학습: {} 개", totalCollected, totalLearned);
            
        } catch (Exception e) {
            log.error("웹 자료 수집 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 특정 주제에 대한 맞춤형 자료 수집
     */
    public void collectSpecificTopic(String topic) {
        log.info("특정 주제 자료 수집: {}", topic);
        
        try {
            String enhancedQuery = "BTD6 Bloons Tower Defense " + topic + " strategy guide tips";
            
            WebSearchRequest request = WebSearchRequest.builder()
                    .query(enhancedQuery)
                    .gameName("BTD6")
                    .searchType("guide")
                    .build();
            
            WebSearchResponse response = webSearchService.searchWeb(request);
            
            if (response.isSuccess()) {
                for (WebSearchResponse.SearchResult result : response.getResults()) {
                    try {
                        if (isTrustedDomain(result.getUrl())) {
                            String content = extractWebContent(result);
                            
                            if (content != null && content.length() > 100) {
                                BloonsTDKnowledge knowledge = convertToKnowledge(result, content, topic);
                                repository.save(knowledge);
                                
                                log.info("주제별 자료 학습 완료: {} - {}", topic, result.getTitle());
                            }
                        }
                    } catch (Exception e) {
                        log.warn("주제별 자료 처리 실패: {}", e.getMessage());
                    }
                    
                    Thread.sleep(1000);
                }
            }
            
        } catch (Exception e) {
            log.error("주제별 자료 수집 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 사용자가 직접 지정한 URL에서 웹 자료를 수집합니다.
     */
    public void collectFromSpecificUrl(String url, String category) {
        log.info("사용자 지정 URL에서 자료 수집: {} (카테고리: {})", url, category);
        
        try {
            if (url == null || url.trim().isEmpty()) {
                log.warn("빈 URL이 제공되어 수집을 건너뜁니다.");
                return;
            }
            
            // URL로 직접 접근하여 내용 추출
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();
            
            // 페이지 제목 추출
            String title = doc.title();
            if (title == null || title.isEmpty()) {
                title = "사용자 지정 페이지";
            }
            
            // 메타 설명 추출
            String description = "";
            Element metaDesc = doc.selectFirst("meta[name=description]");
            if (metaDesc != null) {
                description = metaDesc.attr("content");
            }
            
            // 본문 내용 추출
            StringBuilder content = new StringBuilder();
            
            // 제목들 추가
            Elements headings = doc.select("h1, h2, h3, h4, h5, h6");
            for (Element heading : headings) {
                content.append(heading.text()).append("\n");
            }
            
            // 본문 내용 추가
            Elements paragraphs = doc.select("p, div.content, div.post, article, div.main-content, main");
            for (Element p : paragraphs) {
                String text = p.text();
                if (text.length() > 30) { // 의미있는 내용만 수집
                    content.append(text).append("\n");
                }
            }
            
            String fullContent = content.toString().trim();
            
            if (fullContent.length() > 100) {
                // 웹 검색 결과 형태로 변환
                WebSearchResponse.SearchResult result = new WebSearchResponse.SearchResult();
                result.setTitle(title);
                result.setSnippet(description.isEmpty() ? fullContent.substring(0, Math.min(200, fullContent.length())) : description);
                result.setUrl(url);
                
                // BTD 지식으로 변환하여 저장
                BloonsTDKnowledge knowledge = convertToKnowledge(result, fullContent, category);
                knowledge.setSituationType("사용자지정"); // 사용자가 직접 지정한 자료임을 표시
                
                repository.save(knowledge);
                
                log.info("사용자 지정 URL 자료 학습 완료: {} - {}", category, title);
            } else {
                log.warn("수집된 내용이 너무 짧아 건너뜁니다: {}", url);
            }
            
        } catch (Exception e) {
            log.error("사용자 지정 URL 자료 수집 실패: {} - {}", url, e.getMessage(), e);
        }
    }
    
    /**
     * 여러 URL을 한 번에 수집합니다.
     */
    public void collectFromMultipleUrls(List<String> urls, String category) {
        log.info("다중 URL에서 자료 수집 시작: {} 개 URL (카테고리: {})", urls.size(), category);
        
        int successCount = 0;
        int failCount = 0;
        
        for (String url : urls) {
            try {
                collectFromSpecificUrl(url, category + "_" + (successCount + failCount + 1));
                successCount++;
                
                // 각 URL 처리 간 대기 (서버 부하 방지)
                Thread.sleep(2000);
                
            } catch (Exception e) {
                log.warn("다중 URL 중 개별 처리 실패: {} - {}", url, e.getMessage());
                failCount++;
            }
        }
        
        log.info("다중 URL 수집 완료 - 성공: {} 개, 실패: {} 개", successCount, failCount);
    }
    
    /**
     * 사이트를 깊이 크롤링하여 하위 페이지들도 함께 수집합니다.
     */
    public void collectSiteDeep(String baseUrl, String category, int maxDepth, int maxPages) {
        log.info("사이트 깊이 크롤링 시작: {} (카테고리: {}, 최대 깊이: {}, 최대 페이지: {})", 
                baseUrl, category, maxDepth, maxPages);
        
        Set<String> visitedUrls = new HashSet<>();
        Queue<UrlDepthPair> urlQueue = new LinkedList<>();
        int collectedPages = 0;
        
        try {
            // 기본 도메인 추출
            String baseDomain = extractDomain(baseUrl);
            
            // 시작 URL 추가
            urlQueue.offer(new UrlDepthPair(baseUrl, 0));
            
            while (!urlQueue.isEmpty() && collectedPages < maxPages) {
                UrlDepthPair current = urlQueue.poll();
                String currentUrl = current.url;
                int currentDepth = current.depth;
                
                // 이미 방문한 URL이면 건너뛰기
                if (visitedUrls.contains(currentUrl)) {
                    continue;
                }
                
                // 최대 깊이 초과하면 건너뛰기
                if (currentDepth > maxDepth) {
                    continue;
                }
                
                visitedUrls.add(currentUrl);
                
                try {
                    // 현재 페이지 수집
                    log.info("페이지 수집 중 (깊이 {}): {}", currentDepth, currentUrl);
                    collectFromSpecificUrl(currentUrl, category + "_depth" + currentDepth);
                    collectedPages++;
                    
                    // 하위 링크 추출 (깊이가 최대 깊이보다 작을 때만)
                    if (currentDepth < maxDepth) {
                        List<String> subLinks = extractSubLinks(currentUrl, baseDomain);
                        
                        for (String link : subLinks) {
                            if (!visitedUrls.contains(link) && urlQueue.size() < 100) { // 큐 크기 제한
                                urlQueue.offer(new UrlDepthPair(link, currentDepth + 1));
                            }
                        }
                    }
                    
                    // 페이지 간 대기 (서버 부하 방지)
                    Thread.sleep(3000);
                    
                } catch (Exception e) {
                    log.warn("페이지 수집 실패: {} - {}", currentUrl, e.getMessage());
                }
            }
            
            log.info("사이트 깊이 크롤링 완료 - 수집된 페이지: {} 개, 방문한 URL: {} 개", 
                    collectedPages, visitedUrls.size());
            
        } catch (Exception e) {
            log.error("사이트 깊이 크롤링 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * URL에서 도메인을 추출합니다.
     */
    private String extractDomain(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            
            // URL에서 도메인 부분만 추출
            String domain = url.replaceAll("^https?://", "");
            int slashIndex = domain.indexOf('/');
            if (slashIndex != -1) {
                domain = domain.substring(0, slashIndex);
            }
            
            return domain.toLowerCase();
            
        } catch (Exception e) {
            log.warn("도메인 추출 실패: {} - {}", url, e.getMessage());
            return "";
        }
    }
    
    /**
     * 페이지에서 하위 링크들을 추출합니다.
     */
    private List<String> extractSubLinks(String pageUrl, String baseDomain) {
        List<String> links = new ArrayList<>();
        
        try {
            Document doc = Jsoup.connect(pageUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();
            
            // 모든 링크 추출
            Elements linkElements = doc.select("a[href]");
            
            for (Element link : linkElements) {
                String href = link.attr("href");
                
                if (href != null && !href.isEmpty()) {
                    // 상대 경로를 절대 경로로 변환
                    String absoluteUrl = link.attr("abs:href");
                    
                    if (isValidSubLink(absoluteUrl, baseDomain)) {
                        links.add(absoluteUrl);
                    }
                }
            }
            
            log.debug("페이지 {}에서 {} 개의 하위 링크 추출", pageUrl, links.size());
            
        } catch (Exception e) {
            log.warn("하위 링크 추출 실패: {} - {}", pageUrl, e.getMessage());
        }
        
        return links;
    }
    
    /**
     * 유효한 하위 링크인지 확인합니다.
     */
    private boolean isValidSubLink(String url, String baseDomain) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        // 같은 도메인인지 확인
        String linkDomain = extractDomain(url);
        if (!linkDomain.equals(baseDomain)) {
            return false;
        }
        
        // 파일 확장자 필터링 (이미지, 비디오, 문서 등 제외)
        String lowerUrl = url.toLowerCase();
        String[] excludeExtensions = {".jpg", ".jpeg", ".png", ".gif", ".pdf", ".doc", ".docx", 
                                    ".zip", ".mp4", ".mp3", ".css", ".js", ".xml", ".json"};
        
        for (String ext : excludeExtensions) {
            if (lowerUrl.endsWith(ext)) {
                return false;
            }
        }
        
        // 특정 패턴 제외 (로그인, 관리자 페이지 등)
        String[] excludePatterns = {"login", "admin", "register", "signup", "logout", 
                                   "cart", "checkout", "payment", "profile", "settings"};
        
        for (String pattern : excludePatterns) {
            if (lowerUrl.contains(pattern)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * URL과 깊이를 함께 저장하는 내부 클래스
     */
    private static class UrlDepthPair {
        String url;
        int depth;
        
        UrlDepthPair(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }
    }
    
    /**
     * 웹 페이지에서 실제 내용을 추출합니다.
     */
    private String extractWebContent(WebSearchResponse.SearchResult result) {
        try {
            if (result.getUrl() == null || result.getUrl().isEmpty()) {
                return result.getSnippet();
            }
            
            // JSoup을 사용하여 웹 페이지 파싱
            Document doc = Jsoup.connect(result.getUrl())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            // 본문 내용 추출
            StringBuilder content = new StringBuilder();
            
            // 제목 추가
            Elements titles = doc.select("h1, h2, h3");
            for (Element title : titles) {
                content.append(title.text()).append("\n");
            }
            
            // 본문 내용 추가
            Elements paragraphs = doc.select("p, div.content, div.post, article");
            for (Element p : paragraphs) {
                String text = p.text();
                if (text.length() > 50 && isBTDRelated(text)) {
                    content.append(text).append("\n");
                }
            }
            
            String finalContent = content.toString();
            
            // 최소 길이 확인
            if (finalContent.length() < 100) {
                return result.getSnippet();
            }
            
            // 최대 길이 제한 (너무 긴 내용은 잘라냄)
            if (finalContent.length() > 2000) {
                finalContent = finalContent.substring(0, 2000) + "...";
            }
            
            return finalContent;
            
        } catch (Exception e) {
            log.warn("웹 내용 추출 실패: {} - {}", result.getUrl(), e.getMessage());
            return result.getSnippet();
        }
    }
    
    /**
     * 웹 자료를 BloonsTDKnowledge로 변환합니다.
     */
    private BloonsTDKnowledge convertToKnowledge(WebSearchResponse.SearchResult result, 
                                               String content, String keyword) {
        // 내용에서 정보 추출
        String situationType = extractSituationTypeFromContent(content);
        String roundRange = extractRoundRangeFromContent(content);
        String difficulty = extractDifficultyFromContent(content);
        List<String> towerTypes = extractTowerTypesFromContent(content);
        List<String> tags = generateTagsFromContent(content, keyword);
        
        return BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(situationType)
                .roundRange(roundRange)
                .difficulty(difficulty)
                .towerTypes(towerTypes)
                .title(result.getTitle() != null ? result.getTitle() : "웹에서 수집된 전략")
                .content("웹에서 수집된 BTD 전략 정보: " + content.substring(0, Math.min(500, content.length())))
                .advice(extractAdviceFromContent(content))
                .tags(tags)
                .embedding(generateWebContentEmbedding(content))
                .confidence(0.7) // 웹에서 수집된 자료는 중간 신뢰도
                .successRate(0.6)
                .usageCount(0)
                .sourceUrl(result.getUrl())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 신뢰할 수 있는 도메인인지 확인합니다.
     */
    private boolean isTrustedDomain(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        return TRUSTED_DOMAINS.stream()
                .anyMatch(domain -> url.toLowerCase().contains(domain));
    }
    
    /**
     * BTD 관련 내용인지 확인합니다.
     */
    private boolean isBTDRelated(String text) {
        String lower = text.toLowerCase();
        return lower.contains("bloons") || 
               lower.contains("btd") ||
               lower.contains("tower defense") ||
               lower.contains("moab") ||
               lower.contains("ceramic") ||
               lower.contains("monkey");
    }
    
    /**
     * 내용에서 상황 유형을 추출합니다.
     */
    private String extractSituationTypeFromContent(String content) {
        String lower = content.toLowerCase();
        
        if (lower.contains("ceramic") || lower.contains("세라믹")) {
            return BloonsTDKnowledge.SituationType.CERAMIC_DEFENSE;
        }
        if (lower.contains("moab") || lower.contains("bfb") || lower.contains("zomg")) {
            return BloonsTDKnowledge.SituationType.MOAB_BATTLE;
        }
        if (lower.contains("camo") || lower.contains("카모")) {
            return BloonsTDKnowledge.SituationType.CAMO_DETECTION;
        }
        if (lower.contains("lead") || lower.contains("납")) {
            return BloonsTDKnowledge.SituationType.LEAD_POPPING;
        }
        if (lower.contains("boss")) {
            return BloonsTDKnowledge.SituationType.BOSS_BATTLE;
        }
        if (lower.contains("combo") || lower.contains("combination")) {
            return BloonsTDKnowledge.SituationType.TOWER_COMBO;
        }
        if (lower.contains("economy") || lower.contains("farm")) {
            return BloonsTDKnowledge.SituationType.ECONOMY_BUILD;
        }
        
        return BloonsTDKnowledge.SituationType.GENERAL_STRATEGY;
    }
    
    /**
     * 내용에서 라운드 범위를 추출합니다.
     */
    private String extractRoundRangeFromContent(String content) {
        Pattern pattern = Pattern.compile("round\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            int round = Integer.parseInt(matcher.group(1));
            return BloonsTDKnowledge.getRoundRange(round);
        }
        
        if (content.toLowerCase().contains("early")) return "early_game";
        if (content.toLowerCase().contains("late")) return "late_game";
        
        return "mid_game";
    }
    
    /**
     * 내용에서 난이도를 추출합니다.
     */
    private String extractDifficultyFromContent(String content) {
        String lower = content.toLowerCase();
        
        if (lower.contains("expert") || lower.contains("advanced")) return "expert";
        if (lower.contains("hard") || lower.contains("difficult")) return "hard";
        if (lower.contains("easy") || lower.contains("beginner")) return "easy";
        
        return "medium";
    }
    
    /**
     * 내용에서 타워 유형들을 추출합니다.
     */
    private List<String> extractTowerTypesFromContent(String content) {
        List<String> towers = new ArrayList<>();
        String lower = content.toLowerCase();
        
        String[] towerNames = {
            "dart monkey", "boomerang", "bomb shooter", "tack shooter", "ice monkey",
            "glue gunner", "sniper", "monkey sub", "monkey buccaneer", "monkey ace",
            "heli pilot", "mortar", "dartling", "wizard", "super monkey",
            "ninja", "alchemist", "druid", "banana farm", "spike factory",
            "monkey village", "engineer"
        };
        
        for (String tower : towerNames) {
            if (lower.contains(tower)) {
                towers.add(tower);
            }
        }
        
        return towers.isEmpty() ? Arrays.asList("General") : towers;
    }
    
    /**
     * 내용에서 조언을 추출합니다.
     */
    private String extractAdviceFromContent(String content) {
        // 조언 관련 문장 찾기
        String[] sentences = content.split("[.!?]");
        
        for (String sentence : sentences) {
            String lower = sentence.toLowerCase().trim();
            if (lower.contains("should") || lower.contains("recommend") || 
                lower.contains("best") || lower.contains("tip") ||
                lower.contains("strategy") || lower.contains("use")) {
                
                if (sentence.length() > 20 && sentence.length() < 200) {
                    return sentence.trim();
                }
            }
        }
        
        // 조언을 찾지 못하면 첫 번째 유의미한 문장 반환
        for (String sentence : sentences) {
            if (sentence.trim().length() > 50 && sentence.trim().length() < 200) {
                return sentence.trim();
            }
        }
        
        return "웹에서 수집된 전략 정보입니다.";
    }
    
    /**
     * 내용에서 태그를 생성합니다.
     */
    private List<String> generateTagsFromContent(String content, String keyword) {
        List<String> tags = new ArrayList<>();
        
        // 키워드에서 태그 추출
        tags.add(keyword.toLowerCase().replace(" ", "_"));
        
        // 내용에서 중요 단어 추출
        String lower = content.toLowerCase();
        
        if (lower.contains("strategy")) tags.add("전략");
        if (lower.contains("guide")) tags.add("가이드");
        if (lower.contains("tip")) tags.add("팁");
        if (lower.contains("combo")) tags.add("조합");
        if (lower.contains("defense")) tags.add("방어");
        if (lower.contains("economy")) tags.add("경제");
        
        tags.add("웹수집");
        
        return tags;
    }
    
    /**
     * 웹 내용을 벡터로 변환합니다.
     */
    private List<Double> generateWebContentEmbedding(String content) {
        // 현재는 간단한 해시 기반 임베딩 (실제로는 임베딩 API 사용)
        int hash = content.hashCode();
        List<Double> embedding = new ArrayList<>();
        
        for (int i = 0; i < 768; i++) {
            embedding.add((double) ((hash + i) % 1000) / 1000.0);
        }
        
        return embedding;
    }
} 