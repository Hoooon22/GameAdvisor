package com.gameadvisor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSearchResponse {
    private String query;
    private List<SearchResult> results;
    private boolean success;
    private String errorMessage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResult {
        private String title;
        private String snippet;
        private String url;
        private String displayUrl;
    }
} 