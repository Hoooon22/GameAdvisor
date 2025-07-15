package com.gameadvisor.service.vector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.gameadvisor.model.WebSearchRequest;
import com.gameadvisor.model.WebSearchResponse;
import com.gameadvisor.model.vector.BloonsTDKnowledge;
import com.gameadvisor.repository.vector.BloonsTDVectorRepository;
import com.gameadvisor.service.WebSearchService;

import lombok.extern.slf4j.Slf4j;

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
        "BTD6 economy guide",
        "BTD6 hero abilities guide"
    );
    
    // 실제 BTD 전략 가이드 URL들 (높은 품질의 전략 정보)
    private static final List<String> STRATEGY_GUIDE_URLS = Arrays.asList(
        // 메인 게임플레이 및 기본 가이드
        "https://bloons.fandom.com/wiki/Bloons_TD_6",
        "https://www.ign.com/wikis/bloons-td-6",
        "https://www.ign.com/wikis/bloons-td-6/Bloons_TD_6_Beginner%27s_Tips_and_Tricks",
        "https://www.ign.com/wikis/bloons-td-6/Bloons_TD_6_Co-op_Play",
        "https://www.ign.com/wikis/bloons-td-6/Bloons_TD_6_Quests",
        "https://www.ign.com/wikis/bloons-td-6/All_Hero_Monkeys",
        "https://www.ign.com/wikis/bloons-td-6/All_Towers_and_Non-Hero_Monkeys_and_Upgrades",
        "https://www.ign.com/wikis/bloons-td-6/All_Bloons",
        "https://www.ign.com/wikis/bloons-td-6/All_Difficulties",
        "https://www.ign.com/wikis/bloons-td-6/All_Game_Modes/Level_Types",
        "https://www.ign.com/wikis/bloons-td-6/All_Powers",
        "https://www.ign.com/wikis/bloons-td-6/Best_Hero",
        "https://www.ign.com/wikis/bloons-td-6/Best_Towers",
        "https://www.ign.com/wikis/bloons-td-6/Best_Upgrades",
        "https://www.ign.com/wikis/bloons-td-6/Best_Strategies",
        "https://www.ign.com/wikis/bloons-td-6/How_to_Use_Insta_Monkeys",
        "https://www.ign.com/wikis/bloons-td-6/How_to_Upgrade"
    );
    
    // 신뢰할 수 있는 BTD 관련 웹사이트
    private static final List<String> TRUSTED_DOMAINS = Arrays.asList(
        "bloons.fandom.com",
        "namu.wiki",
        "steamcommunity.com",
        "gamepress.gg"
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
                                    List<BloonsTDKnowledge> knowledgeList = convertToMultipleKnowledge(result, content, keyword);
                                    for (BloonsTDKnowledge knowledge : knowledgeList) {
                                    repository.save(knowledge);
                                    totalLearned++;
                                    }
                                    
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
                                List<BloonsTDKnowledge> knowledgeList = convertToMultipleKnowledge(result, content, topic);
                                for (BloonsTDKnowledge knowledge : knowledgeList) {
                                repository.save(knowledge);
                                }
                                
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
     * 정의된 전략 가이드 URL들을 모두 수집하고 벡터 DB에 학습시킵니다.
     */
    public void collectAllStrategyGuides() {
        log.info("전략 가이드 URL 전체 수집 시작 - 총 {} 개 URL", STRATEGY_GUIDE_URLS.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (String url : STRATEGY_GUIDE_URLS) {
            try {
                // URL에서 카테고리 추출
                String category = extractCategoryFromUrl(url);
                
                log.info("전략 가이드 수집 중: {} (카테고리: {})", url, category);
                
                collectFromSpecificUrl(url, category);
                successCount++;
                
                // 각 URL 처리 간 대기 (서버 부하 방지)
                Thread.sleep(2000);
                
            } catch (Exception e) {
                log.warn("전략 가이드 수집 실패: {} - {}", url, e.getMessage());
                failCount++;
            }
        }
        
        log.info("전략 가이드 수집 완료 - 성공: {} 개, 실패: {} 개", successCount, failCount);
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
                List<BloonsTDKnowledge> knowledgeList = convertToMultipleKnowledge(result, fullContent, category);
                for (BloonsTDKnowledge knowledge : knowledgeList) {
                knowledge.setSituationType("사용자지정"); // 사용자가 직접 지정한 자료임을 표시
                repository.save(knowledge);
                }
                
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
            String lastTitle = "";
            for (Element title : titles) {
                lastTitle = title.text();
                content.append(lastTitle).append("\n");
            }
            
            // 본문 내용 추가
            Elements paragraphs = doc.select("p, div.content, div.post, article");
            for (Element p : paragraphs) {
                String text = p.text();
                if (text.length() > 50 && isBTDRelated(lastTitle, text)) {
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
     * 웹 검색 결과를 BloonsTD 지식으로 변환합니다. (섹션별 분할 방식)
     */
    private List<BloonsTDKnowledge> convertToKnowledge(WebSearchResponse.SearchResult result, 
                                               String content, String keyword) {
        return convertToMultipleKnowledge(result, content, keyword);
    }

    /**
     * 하나의 웹페이지에서 여러 지식을 추출합니다. (개선된 버전)
     */
    private List<BloonsTDKnowledge> convertToMultipleKnowledge(WebSearchResponse.SearchResult result, 
                                                             String content, String keyword) {
        List<BloonsTDKnowledge> knowledgeList = new ArrayList<>();
        
        try {
            // 1. 페이지를 의미 있는 섹션으로 분할
            List<ContentSection> sections = splitContentIntoSections(content, result.getTitle());
            
            log.info("📄 페이지 '{}' 에서 {} 개의 섹션 추출", result.getTitle(), sections.size());
            
            // 2. 각 섹션에서 독립적인 지식 추출
            for (int i = 0; i < sections.size(); i++) {
                ContentSection section = sections.get(i);
                
                // 섹션이 충분히 길고 유의미한지 확인
                if (section.getContent().length() < 150 || !isBTDRelated(section.getTitle(), section.getContent())) {
                    log.debug("🔍 섹션 '{}' 스킵 - 길이: {} 글자", section.getTitle(), section.getContent().length());
                    continue;
                }

                try {
                    // 섹션별 지식 객체 생성
                    BloonsTDKnowledge knowledge = createKnowledgeFromSection(result, section, keyword, i + 1);
                    
                    if (knowledge != null) {
                        knowledgeList.add(knowledge);
                        log.info("✅ 섹션 지식 생성: '{}'", section.getTitle());
                    }
                    
                } catch (Exception e) {
                    log.warn("⚠️ 섹션 '{}' 처리 중 오류: {}", section.getTitle(), e.getMessage());
                }
            }
            
            // 3. 전체 페이지 요약도 하나의 지식으로 추가 (길이가 충분한 경우)
            if (content.length() > 500) {
                BloonsTDKnowledge summaryKnowledge = createSummaryKnowledge(result, content, keyword, sections.size());
                if (summaryKnowledge != null) {
                    knowledgeList.add(summaryKnowledge);
                    log.info("✅ 페이지 전체 요약 지식 생성");
                }
            }
            
        } catch (Exception e) {
            log.error("❌ 페이지 '{}' 섹션 분할 실패: {}", result.getTitle(), e.getMessage(), e);
            
            // 실패 시 기존 방식으로 폴백
            BloonsTDKnowledge fallbackKnowledge = createFallbackKnowledge(result, content, keyword);
            if (fallbackKnowledge != null) {
                knowledgeList.add(fallbackKnowledge);
                log.info("🔄 폴백 지식 생성");
            }
        }
        
        log.info("🎯 페이지 '{}' 에서 총 {} 개의 지식 추출 완료", result.getTitle(), knowledgeList.size());
        return knowledgeList;
    }

    /**
     * 컨텐츠를 의미 있는 섹션으로 분할합니다.
     */
    private List<ContentSection> splitContentIntoSections(String content, String pageTitle) {
        List<ContentSection> sections = new ArrayList<>();
        
        // HTML 태그 제거 후 텍스트만 추출
        String cleanContent = content.replaceAll("<[^>]+>", " ")
                                    .replaceAll("\\s+", " ")
                                    .trim();
        
        // 1. Contents 목차 기반 분할 시도
        sections.addAll(extractContentsSections(cleanContent, pageTitle));
        
        // 2. 헤더 기반 분할 시도 (목차가 없는 경우)
        if (sections.isEmpty()) {
            sections.addAll(extractHeaderSections(cleanContent, pageTitle));
        }
        
        // 3. 길이 기반 분할 (다른 방법이 실패한 경우)
        if (sections.isEmpty()) {
            sections.addAll(extractLengthBasedSections(cleanContent, pageTitle));
        }
        
        return sections;
    }

    /**
     * Contents 목차를 기준으로 섹션을 추출합니다.
     */
    private List<ContentSection> extractContentsSections(String content, String pageTitle) {
        List<ContentSection> sections = new ArrayList<>();
        
        // Contents 패턴 찾기
        String[] contentsPatterns = {
            "Contents\\s*\\n([\\s\\S]*?)(?:\\n\\n|$)",
            "목차\\s*\\n([\\s\\S]*?)(?:\\n\\n|$)",
            "Table of Contents\\s*\\n([\\s\\S]*?)(?:\\n\\n|$)"
        };
        
        String contentsText = null;
        for (String pattern : contentsPatterns) {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(content);
            if (m.find()) {
                contentsText = m.group(1);
                break;
            }
        }
        
        if (contentsText != null) {
            log.debug("📑 Contents 목차 발견: {} 글자", contentsText.length());
            
            // 목차 항목 추출 (예: "1 Gameplay", "1.1 Main gameplay" 등)
            String[] lines = contentsText.split("\\n");
            List<String> sectionTitles = new ArrayList<>();
            
            for (String line : lines) {
                line = line.trim();
                // 숫자로 시작하는 목차 항목 추출
                if (line.matches("^\\d+(\\.\\d+)?\\s+.+$")) {
                    String title = line.replaceFirst("^\\d+(\\.\\d+)?\\s+", "").trim();
                    if (title.length() > 2) {
                        sectionTitles.add(title);
                    }
                }
            }
            
            log.debug("📋 추출된 섹션 제목: {}", sectionTitles);
            
            // 각 섹션별로 컨텐츠 추출
            for (String sectionTitle : sectionTitles) {
                String sectionContent = extractSectionContent(content, sectionTitle);
                if (sectionContent != null && sectionContent.length() > 100) {
                    sections.add(new ContentSection(sectionTitle, sectionContent));
                }
            }
        }
        
        return sections;
    }

    /**
     * 특정 섹션의 컨텐츠를 추출합니다.
     */
    private String extractSectionContent(String content, String sectionTitle) {
        try {
            // 섹션 제목을 다양한 형태로 찾기
            String[] titlePatterns = {
                "(?i)" + Pattern.quote(sectionTitle) + "\\s*\\n([\\s\\S]*?)(?=\\n\\d+(\\.\\d+)?\\s+\\w+|$)",
                "(?i)" + Pattern.quote(sectionTitle) + "\\s*([\\s\\S]*?)(?=\\n[A-Z][\\w\\s]+\\s*\\n|$)",
                "(?i)" + Pattern.quote(sectionTitle) + "([\\s\\S]{0,2000}?)(?=\\n\\w+\\s*\\n|$)"
            };
            
            for (String pattern : titlePatterns) {
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(content);
                if (m.find()) {
                    String sectionContent = m.group(1).trim();
                    if (sectionContent.length() > 50) {
                        return sectionContent.substring(0, Math.min(sectionContent.length(), 1500));
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("섹션 '{}' 컨텐츠 추출 실패: {}", sectionTitle, e.getMessage());
        }
        
        return null;
    }

    /**
     * 헤더 기반으로 섹션을 추출합니다.
     */
    private List<ContentSection> extractHeaderSections(String content, String pageTitle) {
        List<ContentSection> sections = new ArrayList<>();
        
        // 대문자로 시작하는 헤더 찾기
        String[] paragraphs = content.split("\\n\\n");
        String currentSection = "";
        String currentTitle = "";
        
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            
            // 헤더로 보이는 텍스트 (짧고 대문자로 시작)
            if (paragraph.length() < 100 && paragraph.matches("^[A-Z][\\w\\s]+$")) {
                // 이전 섹션 저장
                if (!currentSection.isEmpty() && currentSection.length() > 150) {
                    sections.add(new ContentSection(currentTitle, currentSection));
                }
                
                currentTitle = paragraph;
                currentSection = "";
            } else {
                currentSection += paragraph + "\\n\\n";
            }
        }
        
        // 마지막 섹션 저장
        if (!currentSection.isEmpty() && currentSection.length() > 150) {
            sections.add(new ContentSection(currentTitle, currentSection));
        }
        
        return sections;
    }

    /**
     * 길이 기반으로 섹션을 분할합니다.
     */
    private List<ContentSection> extractLengthBasedSections(String content, String pageTitle) {
        List<ContentSection> sections = new ArrayList<>();
        
        int sectionLength = 800; // 섹션당 권장 길이
        int overlap = 100; // 섹션 간 겹치는 부분
        
        for (int i = 0; i < content.length(); i += (sectionLength - overlap)) {
            int endIndex = Math.min(i + sectionLength, content.length());
            String sectionContent = content.substring(i, endIndex);
            
            if (sectionContent.length() > 200) {
                String sectionTitle = pageTitle + " - Part " + (sections.size() + 1);
                sections.add(new ContentSection(sectionTitle, sectionContent));
            }
        }
        
        return sections;
    }

    /**
     * 섹션에서 지식 객체를 생성합니다.
     */
    private BloonsTDKnowledge createKnowledgeFromSection(WebSearchResponse.SearchResult result, 
                                                        ContentSection section, 
                                                        String keyword, int sectionIndex) {
        try {
            // 섹션별 제목과 설명
            String combinedTitle = result.getTitle() + " - " + section.getTitle();
            
            return BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(classifySituationType(section.getTitle(), section.getContent()))
                .title(combinedTitle)
                .content(section.getContent())
                .advice("섹션 " + sectionIndex + ": " + section.getTitle())
                .tags(Arrays.asList(keyword, classifyCategory(section.getTitle(), section.getContent())))
                .embedding(new ArrayList<>()) // 임베딩은 나중에 설정
                .confidence(calculateSectionConfidence(result, section))
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roundRange(getRoundRangeFromContent(section.getContent()))
                .difficulty(classifyDifficulty(section.getContent()))
                .towerTypes(extractTowerTypes(section.getContent()))
                .successRate(0.7) // 기본 성공률
                .sourceUrl(result.getUrl())
                .build();
            
        } catch (Exception e) {
            log.error("섹션 지식 생성 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 페이지 전체 요약 지식을 생성합니다.
     */
    private BloonsTDKnowledge createSummaryKnowledge(WebSearchResponse.SearchResult result, 
                                                   String content, String keyword, int sectionCount) {
        try {
            // 컨텐츠 요약 생성 (처음 1000자 + 마지막 500자)
            String summary = "";
            if (content.length() > 1500) {
                summary = content.substring(0, 1000) + "\\n\\n... (" + sectionCount + "개 섹션 포함) ...\\n\\n" 
                         + content.substring(content.length() - 500);
            } else {
                summary = content;
            }
            
            return BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType("일반")
                .title(result.getTitle() + " (전체 요약)")
                .content(summary)
                .advice("전체 페이지 요약 - " + sectionCount + "개 섹션 포함")
                .tags(Arrays.asList(keyword, "종합정보", "페이지요약"))
                .embedding(new ArrayList<>())
                .confidence(0.8)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roundRange(getRoundRangeFromContent(content))
                .difficulty("중급")
                .towerTypes(extractTowerTypes(content))
                .successRate(0.7)
                .sourceUrl(result.getUrl())
                .build();
            
        } catch (Exception e) {
            log.error("요약 지식 생성 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 폴백 지식을 생성합니다.
     */
    private BloonsTDKnowledge createFallbackKnowledge(WebSearchResponse.SearchResult result, 
                                                    String content, String keyword) {
        try {
            String shortContent = content.length() > 1000 ? content.substring(0, 1000) + "..." : content;
            
            return BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType("일반")
                .title(result.getTitle())
                .content(shortContent)
                .advice("폴백 처리된 지식")
                .tags(Arrays.asList(keyword, classifyCategory(result.getTitle(), content)))
                .embedding(new ArrayList<>())
                .confidence(0.6)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roundRange(getRoundRangeFromContent(content))
                .difficulty("중급")
                .towerTypes(extractTowerTypes(content))
                .successRate(0.6)
                .sourceUrl(result.getUrl())
                .build();
            
        } catch (Exception e) {
            log.error("폴백 지식 생성 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 컨텐츠에서 라운드 범위를 추출합니다.
     */
    private String getRoundRangeFromContent(String content) {
        if (content == null) return "mid_game";
        
        String lower = content.toLowerCase();
        
        if (lower.contains("early") || lower.contains("round 1") || lower.contains("round 2") ||
            lower.contains("round 3") || lower.contains("round 4") || lower.contains("round 5")) {
            return "early_game";
        }
        
        if (lower.contains("late") || lower.contains("round 80") || lower.contains("round 90") ||
            lower.contains("round 100") || lower.contains("freeplay")) {
            return "late_game";
        }
        
        if (lower.contains("boss") || lower.contains("beyond 100")) {
            return "freeplay";
        }
        
        return "mid_game";
    }

    /**
     * 컨텐츠에서 타워 타입들을 추출합니다.
     */
    private List<String> extractTowerTypes(String content) {
        if (content == null) return new ArrayList<>();
        
        List<String> towerTypes = new ArrayList<>();
        String lower = content.toLowerCase();
        
        String[] towers = {
            "dart_monkey", "boomerang_monkey", "bomb_shooter", "tack_shooter", "ice_monkey", "glue_gunner",
            "sniper_monkey", "monkey_sub", "monkey_buccaneer", "monkey_ace", "heli_pilot", "mortar_monkey",
            "dartling_gunner", "wizard_monkey", "super_monkey", "ninja_monkey", "alchemist", "druid",
            "banana_farm", "spike_factory", "monkey_village", "engineer_monkey"
        };
        
        for (String tower : towers) {
            String displayName = tower.replace("_", " ");
            if (lower.contains(displayName) || lower.contains(tower)) {
                towerTypes.add(tower);
            }
        }
        
        // 적어도 하나는 있어야 함
        if (towerTypes.isEmpty()) {
            towerTypes.add("general");
        }
        
        return towerTypes;
    }

    /**
     * 섹션의 신뢰도를 계산합니다.
     */
    private double calculateSectionConfidence(WebSearchResponse.SearchResult result, ContentSection section) {
        double confidence = 0.7; // 기본 신뢰도
        
        // URL 기반 보정
        if (result.getUrl().contains("bloons.fandom.com")) {
            confidence += 0.2;
        } else if (result.getUrl().contains("reddit.com")) {
            confidence += 0.1;
        }
        
        // 섹션 제목 기반 보정
        String title = section.getTitle().toLowerCase();
        if (title.contains("strategy") || title.contains("guide") || title.contains("tip")) {
            confidence += 0.1;
        }
        
        // 컨텐츠 길이 기반 보정
        if (section.getContent().length() > 500) {
            confidence += 0.05;
        }
        
        return Math.min(confidence, 1.0);
    }

    /**
     * 컨텐츠 섹션을 나타내는 내부 클래스
     */
    private static class ContentSection {
        private final String title;
        private final String content;
        
        public ContentSection(String title, String content) {
            this.title = title;
            this.content = content;
        }
        
        public String getTitle() { return title; }
        public String getContent() { return content; }
    }
    
    /**
     * 웹 내용을 벡터로 변환합니다.
     * TODO: 실제 임베딩 API(OpenAI, Google 등) 연동 필요
     */
    private List<Double> generateWebContentEmbedding(String content) {
        // TF-IDF 기반 의미 있는 임베딩 생성
        List<Double> embedding = new ArrayList<>();
        
        // BTD 관련 핵심 키워드들로 특성 벡터 생성
        String[] keywords = {
            "bloons", "btd", "tower", "defense", "monkey", "dart", "bomb", "ice", "sniper",
            "moab", "ceramic", "lead", "camo", "strategy", "round", "upgrade", "path",
            "primary", "military", "magic", "support", "hero", "village", "farm", "spike",
            "quincy", "gwendolin", "striker", "obyn", "churchill", "benjamin", "pat", "ezili",
            "combo", "synergy", "early", "mid", "late", "game", "difficult", "expert",
            "beginner", "easy", "medium", "hard", "ceramic", "fortified", "regrow", "shield"
        };
        
        String lowerContent = content.toLowerCase();
        
        // 각 키워드의 출현 빈도와 위치 기반으로 벡터 생성
        for (String keyword : keywords) {
            double frequency = 0.0;
            int lastIndex = 0;
            int count = 0;
            
            // 키워드 출현 빈도 계산
            while ((lastIndex = lowerContent.indexOf(keyword, lastIndex)) != -1) {
                count++;
                lastIndex += keyword.length();
            }
            
            if (count > 0) {
                frequency = Math.log(1 + count) / Math.log(content.length() + 1);
                // 키워드가 제목이나 앞부분에 있으면 가중치 추가
                if (content.length() > 100 && lowerContent.substring(0, 100).contains(keyword)) {
                    frequency *= 1.5;
                }
            }
            
            embedding.add(Math.min(frequency, 1.0));
        }
        
        // 벡터를 768차원으로 확장 (패딩 또는 반복)
        while (embedding.size() < 768) {
            // 기존 벡터를 변형하여 확장
            int index = embedding.size() % keywords.length;
            double baseValue = embedding.get(index);
            // 약간의 노이즈를 추가하여 다양성 확보
            double noise = (Math.random() - 0.5) * 0.1;
            embedding.add(Math.max(0.0, Math.min(1.0, baseValue + noise)));
        }
        
        // 벡터 정규화 (L2 norm)
        double norm = Math.sqrt(embedding.stream().mapToDouble(x -> x * x).sum());
        if (norm > 0) {
            embedding = embedding.stream().map(x -> x / norm).collect(java.util.stream.Collectors.toList());
        }
        
        return embedding;
    }

    /**
     * URL에서 카테고리를 추출합니다.
     */
    private String extractCategoryFromUrl(String url) {
        try {
            if (url.contains("wiki/")) {
                String wikiPart = url.substring(url.indexOf("wiki/") + 5);
                
                // 특정 패턴에 따라 카테고리 분류
                if (wikiPart.contains("Monkey") && wikiPart.contains("BTD6")) {
                    return "towers";
                } else if (wikiPart.contains("Primary") || wikiPart.contains("Military") || 
                          wikiPart.contains("Magic") || wikiPart.contains("Support")) {
                    return "tower_categories";
                } else if (wikiPart.contains("Paragon") || wikiPart.contains("Apex") || 
                          wikiPart.contains("Ascended") || wikiPart.contains("Navarch") || 
                          wikiPart.contains("Goliath") || wikiPart.contains("Master_Builder")) {
                    return "paragons";
                } else if (wikiPart.contains("Heroes") || wikiPart.contains("Quincy") || 
                          wikiPart.contains("Gwendolin") || wikiPart.contains("Striker") || 
                          wikiPart.contains("Obyn") || wikiPart.contains("Churchill") || 
                          wikiPart.contains("Benjamin") || wikiPart.contains("Ezili") || 
                          wikiPart.contains("Pat_Fusty") || wikiPart.contains("Adora") || 
                          wikiPart.contains("Brickell") || wikiPart.contains("Etienne") || 
                          wikiPart.contains("Sauda") || wikiPart.contains("Psi") || 
                          wikiPart.contains("Geraldo") || wikiPart.contains("Corvus") || 
                          wikiPart.contains("Rosalia")) {
                    return "heroes";
                } else if (wikiPart.contains("Bloons") || wikiPart.contains("MOAB") || 
                          wikiPart.contains("Boss") || wikiPart.contains("Camo") || 
                          wikiPart.contains("Regrow") || wikiPart.contains("Fortified") || 
                          wikiPart.contains("Golden")) {
                    return "bloons";
                } else if (wikiPart.contains("Maps") || wikiPart.contains("Beginner") || 
                          wikiPart.contains("Intermediate") || wikiPart.contains("Advanced") || 
                          wikiPart.contains("Expert")) {
                    return "maps";
                } else if (wikiPart.contains("Mode") || wikiPart.contains("Standard") || 
                          wikiPart.contains("Deflation") || wikiPart.contains("Military_Only") || 
                          wikiPart.contains("Reverse") || wikiPart.contains("Apopalypse") || 
                          wikiPart.contains("Half_Cash") || wikiPart.contains("Double_HP") || 
                          wikiPart.contains("Alternate") || wikiPart.contains("Impoppable") || 
                          wikiPart.contains("CHIMPS")) {
                    return "game_modes";
                } else if (wikiPart.contains("Co-Op") || wikiPart.contains("Odyssey") || 
                          wikiPart.contains("Race") || wikiPart.contains("Boss_Events") || 
                          wikiPart.contains("Contested") || wikiPart.contains("Collection")) {
                    return "events";
                } else if (wikiPart.contains("Challenge") || wikiPart.contains("Daily") || 
                          wikiPart.contains("Editor") || wikiPart.contains("Creator") || 
                          wikiPart.contains("Content") || wikiPart.contains("Quests")) {
                    return "custom";
                } else if (wikiPart.contains("Shop") || wikiPart.contains("Trophy") || 
                          wikiPart.contains("Monkey_Knowledge") || wikiPart.contains("Upgrades") || 
                          wikiPart.contains("Crosspathing")) {
                    return "upgrades";
                } else if (wikiPart.contains("Achievement") || wikiPart.contains("Profile") || 
                          wikiPart.contains("Veteran")) {
                    return "progression";
                } else if (wikiPart.contains("Strategies") || wikiPart.contains("Synergies") || 
                          wikiPart.contains("Farming") || wikiPart.contains("Target") || 
                          wikiPart.contains("Damage") || wikiPart.contains("Status")) {
                    return "strategies";
                } else if (wikiPart.contains("Gameplay") || wikiPart.contains("Tutorial")) {
                    return "basics";
                } else {
                    return "general";
                }
            }
            
            return "wiki_guide";
            
        } catch (Exception e) {
            log.warn("카테고리 추출 실패: {} - {}", url, e.getMessage());
            return "unknown";
        }
    }

    /**
     * 모든 BloonsTD 벡터 데이터를 초기화합니다.
     */
    public void clearAllBloonsTDData() {
        try {
            log.info("🗑️ BloonsTD 벡터 데이터 초기화 시작...");
            
            long deletedCount = repository.count();
            repository.deleteAll();
            
            log.info("✅ BloonsTD 벡터 데이터 초기화 완료! 삭제된 지식 수: {}", deletedCount);
            
        } catch (Exception e) {
            log.error("❌ BloonsTD 벡터 데이터 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("DB 초기화 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 현재 벡터 DB의 지식 통계를 반환합니다.
     */
    public Map<String, Object> getKnowledgeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            log.info("📊 벡터 DB 통계 수집 중...");
            
            // 기본 통계
            long totalCount = repository.count();
            stats.put("총_지식_수", totalCount);
            
            if (totalCount > 0) {
                // 평균 신뢰도
                Double avgConfidence = repository.findAverageConfidence();
                stats.put("평균_신뢰도", avgConfidence != null ? String.format("%.2f", avgConfidence) : "N/A");
                
                // 난이도별 통계
                Map<String, Long> difficultyStats = new HashMap<>();
                difficultyStats.put("초급", repository.countByDifficulty("초급"));
                difficultyStats.put("중급", repository.countByDifficulty("중급"));
                difficultyStats.put("고급", repository.countByDifficulty("고급"));
                stats.put("난이도별_통계", difficultyStats);
                
                // 상황 타입별 통계
                Map<String, Long> situationStats = new HashMap<>();
                situationStats.put("초반", repository.countBySituationType("초반"));
                situationStats.put("중반", repository.countBySituationType("중반"));
                situationStats.put("후반", repository.countBySituationType("후반"));
                situationStats.put("보스", repository.countBySituationType("보스"));
                situationStats.put("일반", repository.countBySituationType("일반"));
                stats.put("상황별_통계", situationStats);
                
                // 라운드별 통계
                Map<String, Long> roundStats = new HashMap<>();
                roundStats.put("1-10라운드", repository.countByRoundRange("1-10"));
                roundStats.put("11-30라운드", repository.countByRoundRange("11-30"));
                roundStats.put("31-60라운드", repository.countByRoundRange("31-60"));
                roundStats.put("61-100라운드", repository.countByRoundRange("61-100"));
                roundStats.put("100+라운드", repository.countByRoundRange("100+"));
                stats.put("라운드별_통계", roundStats);
                
                // 최근 추가된 지식들
                List<BloonsTDKnowledge> recentKnowledge = repository.findTop10ByOrderByCreatedAtDesc();
                List<String> recentTitles = recentKnowledge.stream()
                    .map(k -> k.getTitle())
                    .limit(5)
                    .collect(Collectors.toList());
                stats.put("최근_지식_5개", recentTitles);
                
                // 사용량 높은 지식들
                List<BloonsTDKnowledge> popularKnowledge = repository.findTopByUsageCount(5);
                List<String> popularTitles = popularKnowledge.stream()
                    .map(k -> k.getTitle() + " (사용횟수: " + k.getUsageCount() + ")")
                    .collect(Collectors.toList());
                stats.put("인기_지식_5개", popularTitles);
            }
            
            stats.put("수집_시간", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stats.put("상태", totalCount > 0 ? "정상" : "비어있음");
            
            log.info("📊 벡터 DB 통계 수집 완료: 총 {} 개의 지식", totalCount);
            
        } catch (Exception e) {
            log.error("❌ 벡터 DB 통계 수집 실패: {}", e.getMessage(), e);
            stats.put("오류", e.getMessage());
            stats.put("상태", "오류");
        }
        
        return stats;
    }

    /**
     * 전체 재학습을 위한 완전한 초기화 및 재수집
     */
    public CompletableFuture<Map<String, Object>> resetAndRelearn(boolean enhancedMode) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> result = new HashMap<>();
            
            try {
                log.info("🔄 BloonsTD 벡터 DB 전체 재학습 시작 (개선모드: {})", enhancedMode);
                
                // 1. DB 초기화
                clearAllBloonsTDData();
                result.put("1단계_DB초기화", "완료");
                
                // 2. 향상된 수집 모드 설정
                if (enhancedMode) {
                    log.info("🚀 향상된 수집 모드 활성화 - 섹션별 세분화 처리");
                }
                
                // 3. 전략 가이드 수집
                try {
                    collectAllStrategyGuides();
                    result.put("2단계_전략가이드", "완료");
                } catch (Exception e) {
                    log.warn("전략 가이드 수집 실패: {}", e.getMessage());
                    result.put("2단계_전략가이드", "실패: " + e.getMessage());
                }
                
                // 4. 사이트 깊이 크롤링
                try {
                    collectSiteDeep("https://bloons.fandom.com", "BloonsTD", 2, 30);
                    result.put("3단계_깊이크롤링", "완료");
                } catch (Exception e) {
                    log.warn("깊이 크롤링 실패: {}", e.getMessage());
                    result.put("3단계_깊이크롤링", "실패: " + e.getMessage());
                }
                
                // 5. 추가 수집 작업
                try {
                    log.info("📚 추가 BloonsTD 지식 수집을 위해 더 많은 키워드로 검색합니다...");
                    
                    // 여기서는 기존에 정의된 메서드들만 사용
                    // 향후 추가 수집 로직을 구현할 수 있음
                    
                    result.put("4단계_추가수집", "완료 (기본 수집만)");
                } catch (Exception e) {
                    log.warn("추가 수집 실패: {}", e.getMessage());
                    result.put("4단계_추가수집", "실패: " + e.getMessage());
                }
                
                // 6. 최종 통계
                Map<String, Object> finalStats = getKnowledgeStatistics();
                result.put("최종_통계", finalStats);
                
                log.info("✅ BloonsTD 벡터 DB 전체 재학습 완료!");
                result.put("상태", "성공");
                result.put("완료시간", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
            } catch (Exception e) {
                log.error("❌ 전체 재학습 실패: {}", e.getMessage(), e);
                result.put("상태", "실패");
                result.put("오류", e.getMessage());
            }
            
            return result;
        });
    }

    /**
     * 단일 URL에서 모든 지식을 깊이 있게 추출합니다.
     */
    public CompletableFuture<Map<String, Object>> learnFromSingleUrlDeep(String url, String category) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> result = new HashMap<>();
            int totalKnowledgeCount = 0;
            
            try {
                log.info("🎯 단일 URL 깊이 학습 시작: {}", url);
                result.put("url", url);
                result.put("category", category);
                result.put("시작시간", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                // 1. URL에서 컨텐츠 추출
                String content = fetchContentFromUrl(url);
                if (content == null || content.length() < 100) {
                    throw new RuntimeException("페이지 컨텐츠를 가져올 수 없습니다: " + url);
                }
                
                log.info("📄 페이지 컨텐츠 추출 완료: {} 문자", content.length());
                result.put("컨텐츠_크기", content.length() + " 문자");
                
                // 2. 컨텐츠를 의미 있는 섹션들로 분할
                List<DetailedSection> sections = extractDetailedSections(content, url);
                log.info("📑 추출된 섹션 수: {}", sections.size());
                result.put("섹션_수", sections.size());
                
                // 3. 각 섹션을 개별 지식으로 변환 및 저장
                List<String> sectionTitles = new ArrayList<>();
                for (int i = 0; i < sections.size(); i++) {
                    DetailedSection section = sections.get(i);
                    
                    try {
                        if (section.getContent().length() < 30 || !isBTDRelated(section.getTitle(), section.getContent())) {
                            log.debug("섹션 건너뛰기 (내용 부족 또는 BTD 무관): {} / {}", section.getTitle(), section.getContent().substring(0, Math.min(50, section.getContent().length())));
                            continue;
                        }
                        BloonsTDKnowledge knowledge = createKnowledgeFromDetailedSection(section, url, category, i);
                        if (knowledge != null) {
                            repository.save(knowledge);
                            totalKnowledgeCount++;
                            sectionTitles.add(section.getTitle());
                            log.info("✅ 섹션 지식 저장 완료: {} ({}자)", section.getTitle(), section.getContent().length());
                        }
                        Thread.sleep(100);
                    } catch (Exception e) {
                        log.warn("섹션 처리 실패: {} - {}", section.getTitle(), e.getMessage());
                    }
                }
                
                // 4. 전체 페이지 요약 정보도 별도로 저장
                BloonsTDKnowledge summaryKnowledge = createPageSummaryKnowledge(url, content, category, sections.size());
                if (summaryKnowledge != null) {
                    repository.save(summaryKnowledge);
                    totalKnowledgeCount++;
                    log.info("✅ 페이지 요약 지식 저장 완료");
                }
                
                // 5. 최종 결과
                result.put("상태", "성공");
                result.put("저장된_지식_수", totalKnowledgeCount);
                result.put("처리된_섹션들", sectionTitles);
                result.put("완료시간", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                log.info("🎉 단일 URL 깊이 학습 완료! 총 {} 개의 지식 저장", totalKnowledgeCount);
                
            } catch (Exception e) {
                log.error("❌ 단일 URL 학습 실패: {}", e.getMessage(), e);
                result.put("상태", "실패");
                result.put("오류", e.getMessage());
                result.put("저장된_지식_수", totalKnowledgeCount);
            }
            
            return result;
        });
    }

    /**
     * URL에서 컨텐츠를 직접 추출합니다.
     */
    private String fetchContentFromUrl(String url) {
        try {
            log.info("URL에서 컨텐츠 추출 시작: {}", url);
            
            // RestTemplate을 사용한 직접 HTTP 요청
            RestTemplate restTemplate = new RestTemplate();
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            headers.set("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3");
            headers.set("Accept-Encoding", "gzip, deflate");
            headers.set("Connection", "keep-alive");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // HTTP GET 요청
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String content = response.getBody();
                log.info("컨텐츠 추출 성공: {} 문자", content.length());
                return content;
            } else {
                log.warn("HTTP 요청 실패: {} - 상태코드: {}", url, response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            log.error("URL 컨텐츠 추출 실패: {} - {}", url, e.getMessage());
            
            // 대체 방법: 웹 검색 서비스를 통한 간접 접근
            try {
                log.info("대체 방법으로 웹 검색 서비스 사용");
                WebSearchRequest searchRequest = new WebSearchRequest();
                searchRequest.setQuery("site:" + url.replace("https://", "").replace("http://", ""));
                searchRequest.setGameName("BTD6");
                
                WebSearchResponse searchResponse = webSearchService.searchWeb(searchRequest);
                if (searchResponse != null && searchResponse.isSuccess() && 
                    searchResponse.getResults() != null && !searchResponse.getResults().isEmpty()) {
                    
                    // 검색 결과의 스니펫을 조합하여 컨텐츠 생성
                    StringBuilder content = new StringBuilder();
                    for (WebSearchResponse.SearchResult result : searchResponse.getResults()) {
                        if (result.getTitle() != null) {
                            content.append(result.getTitle()).append("\\n");
                        }
                        if (result.getSnippet() != null) {
                            content.append(result.getSnippet()).append("\\n\\n");
                        }
                    }
                    
                    return content.toString();
                }
                
            } catch (Exception e2) {
                log.error("대체 방법도 실패: {}", e2.getMessage());
            }
            
            return null;
        }
    }

    /**
     * 컨텐츠를 더 세밀한 섹션들로 분할합니다.
     */
    private List<DetailedSection> extractDetailedSections(String content, String url) {
        List<DetailedSection> sections = new ArrayList<>();
        try {
            // fandom 위키는 HTML 구조가 명확하므로 Jsoup로 파싱
            Document doc = Jsoup.parse(content);

            // 1. 목차(toc) 추출
            List<String> tocTitles = new ArrayList<>();
            Element toc = doc.getElementById("toc");
            if (toc != null) {
                Elements tocLinks = toc.select("li a");
                for (Element link : tocLinks) {
                    String title = link.text().trim();
                    if (!title.isEmpty()) tocTitles.add(title);
                }
            }

            // 2. heading(h2, h3, h4) 기반 섹션 분리
            Elements headings = doc.select("h2, h3, h4");
            for (int i = 0; i < headings.size(); i++) {
                Element heading = headings.get(i);
                String sectionTitle = heading.text().trim();

                // 다음 heading 전까지의 내용 추출
                StringBuilder sectionContent = new StringBuilder();
                Element sibling = heading.nextElementSibling();
                while (sibling != null && !sibling.tagName().matches("h2|h3|h4")) {
                    // 불필요한 영역 필터링(광고, 푸터, 네비게이션 등)
                    String cls = sibling.className();
                    if (!cls.contains("footer") && !cls.contains("navbox") && !cls.contains("header") && !cls.contains("sidebar") && !cls.contains("mw-editsection")) {
                        sectionContent.append(sibling.text()).append("\n");
                    }
                    sibling = sibling.nextElementSibling();
                }

                // 목차에 있는 제목만 저장(또는 모두 저장)
                if (sectionContent.length() > 100) {
                    sections.add(new DetailedSection(sectionTitle, sectionContent.toString().trim(), i, "heading_based"));
                }
            }

            // 3. 만약 섹션이 하나도 없으면 fallback(기존 방식)
            if (sections.isEmpty()) {
                // 기존 텍스트 기반 분할 방식 사용
                String[] htmlSections = content.split("(?i)<h[1-6][^>]*>|<div[^>]*class[^>]*section|<article|<section");
                for (int i = 0; i < htmlSections.length; i++) {
                    String section = htmlSections[i];
                    if (section.length() > 100) {
                        sections.add(new DetailedSection("페이지 부분 " + (i + 1), section, i, "chunk_based"));
                    }
                }
            }
        } catch (Exception e) {
            log.error("섹션 분할 실패: {}", e.getMessage(), e);
        }
        return sections;
    }

    /**
     * 상세 섹션에서 지식 객체를 생성합니다.
     */
    private BloonsTDKnowledge createKnowledgeFromDetailedSection(DetailedSection section, String sourceUrl, 
                                                               String category, int index) {
        try {
            return BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(classifySituationType(section.getTitle(), section.getContent()))
                .title(section.getTitle())
                .content(section.getContent())
                .advice("섹션 " + (index + 1) + " - " + section.getExtractionMethod() + " 방식으로 추출")
                .tags(Arrays.asList(category, 
                                  classifyCategory(section.getTitle(), section.getContent()), 
                                  section.getExtractionMethod()))
                .embedding(new ArrayList<>())
                .confidence(calculateDetailedSectionConfidence(section, sourceUrl))
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roundRange(getRoundRangeFromContent(section.getContent()))
                .difficulty(classifyDifficulty(section.getContent()))
                .towerTypes(extractTowerTypes(section.getContent()))
                .successRate(0.8) // 단일 페이지 깊이 분석이므로 높은 신뢰도
                .sourceUrl(sourceUrl)
                .build();
                
        } catch (Exception e) {
            log.error("상세 섹션 지식 생성 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 페이지 전체 요약 지식을 생성합니다.
     */
    private BloonsTDKnowledge createPageSummaryKnowledge(String url, String content, String category, int sectionCount) {
        try {
            String summary = content.length() > 2000 ? 
                            content.substring(0, 1500) + "\\n\\n[전체 " + sectionCount + "개 섹션으로 구성]\\n\\n" 
                         + content.substring(Math.max(0, content.length() - 500)) : 
                            content;
            
            return BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType("종합정보")
                .title("Bloons TD 6 - 전체 가이드 (메인 페이지)")
                .content(summary)
                .advice("BTD6 메인 위키 페이지 전체 요약 - " + sectionCount + "개 세부 섹션 포함")
                .tags(Arrays.asList(category, "메인페이지", "종합가이드", "전체요약"))
                .embedding(new ArrayList<>())
                .confidence(0.95) // 공식 위키이므로 매우 높은 신뢰도
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roundRange("mid_game") // 전체 범위
                .difficulty("중급")
                .towerTypes(Arrays.asList("general", "all_towers"))
                .successRate(0.9)
                .sourceUrl(url)
                .build();
                
        } catch (Exception e) {
            log.error("페이지 요약 지식 생성 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 상세 섹션의 신뢰도를 계산합니다.
     */
    private Double calculateDetailedSectionConfidence(DetailedSection section, String sourceUrl) {
        double baseConfidence = 0.7;
        
        // 공식 위키는 신뢰도 높음
        if (sourceUrl.contains("bloons.fandom.com")) {
            baseConfidence += 0.2;
        }
        
        // 컨텐츠 길이에 따른 보정
        if (section.getContent().length() > 500) {
            baseConfidence += 0.05;
        }
        if (section.getContent().length() > 1000) {
            baseConfidence += 0.05;
        }
        
        // 추출 방식에 따른 보정
        switch (section.getExtractionMethod()) {
            case "content_based": baseConfidence += 0.1; break;
            case "html_based": baseConfidence += 0.05; break;
            case "chunk_based": baseConfidence -= 0.1; break;
        }
        
        return Math.min(1.0, Math.max(0.1, baseConfidence));
    }

    /**
     * 상세 섹션 정보를 담는 내부 클래스
     */
    private static class DetailedSection {
        private final String title;
        private final String content;
        private final int index;
        private final String extractionMethod;
        
        public DetailedSection(String title, String content, int index, String extractionMethod) {
            this.title = title;
            this.content = content;
            this.index = index;
            this.extractionMethod = extractionMethod;
        }
        
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public int getIndex() { return index; }
        public String getExtractionMethod() { return extractionMethod; }
    }

    // =============================================================================
    // 유틸리티 메서드들
    // =============================================================================

    /**
     * 신뢰할 수 있는 도메인인지 확인합니다.
     */
    private boolean isTrustedDomain(String url) {
        if (url == null) return false;
        
        String[] trustedDomains = {
            "bloons.fandom.com",
            "reddit.com",
            "namu.wiki",
            "steampowered.com",
            "youtube.com",
            "wikia.com"
        };
        
        for (String domain : trustedDomains) {
            if (url.contains(domain)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * BloonsTD 관련 컨텐츠인지 확인합니다.
     */
    private boolean isBTDRelated(String title, String content) {
        if ((content == null || content.length() < 30) && (title == null || title.length() < 5)) return false;
        String lowerContent = (content == null ? "" : content.toLowerCase());
        String lowerTitle = (title == null ? "" : title.toLowerCase());
        String[] btdKeywords = {
            "bloons", "btd", "tower defense", "monkey", "dart monkey", "boomerang", 
            "tack shooter", "bomb shooter", "ice monkey", "glue gunner", "sniper",
            "monkey sub", "buccaneer", "ace", "heli", "mortar", "dartling", 
            "wizard", "super monkey", "ninja", "alchemist", "druid", "banana farm",
            "spike factory", "village", "engineer", "hero", "quincy", "gwendolin",
            "striker jones", "obyn", "captain churchill", "benjamin", "ezili",
            "pat fusty", "adora", "brickell", "etienne", "sauda", "psi", "geraldo",
            "moab", "ceramic", "lead", "camo", "regrow", "fortified", "ddt", "bad",
            "round", "pop", "pierce", "damage", "upgrade", "tier", "paragon"
        };
        int matchCount = 0;
        for (String keyword : btdKeywords) {
            if (lowerContent.contains(keyword) || lowerTitle.contains(keyword)) {
                matchCount++;
            }
        }
        // 1개 이상 키워드 포함 시 true
        return matchCount >= 1;
    }

    /**
     * 카테고리를 분류합니다.
     */
    private String classifyCategory(String title, String content) {
        if (title == null) title = "";
        if (content == null) content = "";
        
        String combined = (title + " " + content).toLowerCase();
        
        // 카테고리 분류 규칙
        if (combined.contains("hero") || combined.contains("quincy") || combined.contains("gwendolin") ||
            combined.contains("striker") || combined.contains("obyn") || combined.contains("churchill") ||
            combined.contains("benjamin") || combined.contains("ezili") || combined.contains("pat") ||
            combined.contains("adora") || combined.contains("brickell") || combined.contains("etienne") ||
            combined.contains("sauda") || combined.contains("psi") || combined.contains("geraldo")) {
            return "영웅";
        }
        
        if (combined.contains("tower") || combined.contains("monkey") || combined.contains("dart") ||
            combined.contains("boomerang") || combined.contains("tack") || combined.contains("bomb") ||
            combined.contains("ice") || combined.contains("glue") || combined.contains("sniper") ||
            combined.contains("sub") || combined.contains("buccaneer") || combined.contains("ace") ||
            combined.contains("heli") || combined.contains("mortar") || combined.contains("dartling") ||
            combined.contains("wizard") || combined.contains("super") || combined.contains("ninja") ||
            combined.contains("alchemist") || combined.contains("druid") || combined.contains("spike") ||
            combined.contains("village") || combined.contains("engineer")) {
            return "타워";
        }
        
        if (combined.contains("strategy") || combined.contains("guide") || combined.contains("tip") ||
            combined.contains("combo") || combined.contains("build")) {
            return "전략";
        }
        
        if (combined.contains("map") || combined.contains("beginner") || combined.contains("intermediate") ||
            combined.contains("advanced") || combined.contains("expert")) {
            return "맵";
        }
        
        if (combined.contains("bloon") || combined.contains("moab") || combined.contains("ceramic") ||
            combined.contains("lead") || combined.contains("camo") || combined.contains("regrow") ||
            combined.contains("fortified") || combined.contains("ddt") || combined.contains("bad")) {
            return "블룬";
        }
        
        if (combined.contains("upgrade") || combined.contains("tier") || combined.contains("paragon")) {
            return "업그레이드";
        }
        
        if (combined.contains("economy") || combined.contains("banana") || combined.contains("farm") ||
            combined.contains("money") || combined.contains("income")) {
            return "경제";
        }
        
        return "일반";
    }

    /**
     * 상황 타입을 분류합니다.
     */
    private String classifySituationType(String title, String content) {
        if (title == null) title = "";
        if (content == null) content = "";
        
        String combined = (title + " " + content).toLowerCase();
        
        // 라운드 정보로 판단
        if (combined.contains("round 1") || combined.contains("round 2") || combined.contains("round 3") ||
            combined.contains("round 4") || combined.contains("round 5") || combined.contains("early") ||
            combined.contains("beginner") || combined.contains("start")) {
            return "초반";
        }
        
        if (combined.contains("round 6") || combined.contains("round 7") || combined.contains("round 8") ||
            combined.contains("round 9") || combined.contains("round 10") || combined.contains("ceramic") ||
            combined.contains("middle") || combined.contains("mid")) {
            return "중반";
        }
        
        if (combined.contains("round 80") || combined.contains("round 90") || combined.contains("round 100") ||
            combined.contains("late") || combined.contains("endgame") || combined.contains("freeplay")) {
            return "후반";
        }
        
        if (combined.contains("boss") || combined.contains("bloonarius") || combined.contains("lych") ||
            combined.contains("vortex") || combined.contains("dreadbloon") || combined.contains("phayze")) {
            return "보스";
        }
        
        if (combined.contains("moab") || combined.contains("bfb") || combined.contains("zomg") ||
            combined.contains("bad") || combined.contains("ddt")) {
            return "중반";
        }
        
        return "일반";
    }

    /**
     * 난이도를 분류합니다.
     */
    private String classifyDifficulty(String content) {
        if (content == null) return "중급";
        
        String lower = content.toLowerCase();
        
        if (lower.contains("beginner") || lower.contains("easy") || lower.contains("simple") ||
            lower.contains("기초") || lower.contains("초보") || lower.contains("쉬운")) {
            return "초급";
        }
        
        if (lower.contains("expert") || lower.contains("hard") || lower.contains("difficult") ||
            lower.contains("advanced") || lower.contains("complex") || lower.contains("어려운") ||
            lower.contains("고급") || lower.contains("전문")) {
            return "고급";
        }
        
        return "중급";
    }

    /**
     * 라운드 시작점을 추출합니다.
     */
    private Integer extractRoundStart(String content) {
        if (content == null) return null;
        
        // Round X 패턴 찾기
        Pattern pattern = Pattern.compile("round\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }

    /**
     * 라운드 종료점을 추출합니다.
     */
    private Integer extractRoundEnd(String content) {
        if (content == null) return null;
        
        Integer start = extractRoundStart(content);
        if (start != null) {
            // 보통 시작점에서 10라운드 정도의 범위로 설정
            return start + 10;
        }
        
        return null;
    }

    /**
     * 섹션에서 제목을 추출합니다.
     */
    private String extractSectionTitle(String sectionContent, int index) {
        if (sectionContent == null || sectionContent.length() < 10) return null;
        
        try {
            // HTML 헤더 태그에서 제목 추출
            Pattern headerPattern = Pattern.compile("<h[1-6][^>]*>([^<]+)</h[1-6]>", Pattern.CASE_INSENSITIVE);
            Matcher matcher = headerPattern.matcher(sectionContent);
            if (matcher.find()) {
                return cleanText(matcher.group(1)).trim();
            }
            
            // 첫 번째 줄을 제목으로 사용 (길이 제한)
            String[] lines = sectionContent.split("\\n");
            for (String line : lines) {
                String cleanLine = cleanText(line).trim();
                if (cleanLine.length() > 5 && cleanLine.length() < 100) {
                    // 특수 문자나 숫자로만 구성된 줄은 제외
                    if (cleanLine.matches(".*[가-힣a-zA-Z].*")) {
                        return cleanLine;
                    }
                }
            }
            
            // 기본 제목
            return "섹션 " + (index + 1);
            
        } catch (Exception e) {
            return "섹션 " + (index + 1);
        }
    }

    /**
     * 컨텐츠를 정리합니다.
     */
    private String cleanContent(String content) {
        if (content == null) return "";
        
        try {
            String cleaned = content
                // HTML 태그 제거
                .replaceAll("<[^>]+>", " ")
                // 특수 문자 정리
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                // 연속된 공백 및 줄바꿈 정리
                .replaceAll("\\s+", " ")
                .replaceAll("\\n\\s*\\n", "\\n")
                // 불필요한 문자 제거
                .replaceAll("[\\[\\]\\{\\}]", "")
                .trim();
            
            return cleaned;
            
        } catch (Exception e) {
            log.debug("컨텐츠 정리 실패: {}", e.getMessage());
            return content.trim();
        }
    }

    /**
     * 텍스트를 정리합니다 (제목용).
     */
    private String cleanText(String text) {
        if (text == null) return "";
        
        return text
            .replaceAll("<[^>]+>", "")
            .replaceAll("&[a-zA-Z]+;", " ")
            .replaceAll("[\\[\\]\\{\\}]", "")
            .replaceAll("\\s+", " ")
            .trim();
    }
} 