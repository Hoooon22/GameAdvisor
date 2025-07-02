package com.gameadvisor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSearchRequest {
    private String query;
    private String gameName;
    private int maxResults;
    private String searchType; // "strategy", "guide", "tips", "meta" 등
} 