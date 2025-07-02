package com.gameadvisor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiResponse {
    
    @JsonProperty("candidates")
    private List<Candidate> candidates;
    
    @JsonProperty("usageMetadata")
    private UsageMetadata usageMetadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        @JsonProperty("content")
        private Content content;
        
        @JsonProperty("finishReason")
        private String finishReason;
        
        @JsonProperty("index")
        private Integer index;
        
        @JsonProperty("safetyRatings")
        private List<SafetyRating> safetyRatings;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        @JsonProperty("parts")
        private List<Part> parts;
        
        @JsonProperty("role")
        private String role;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        @JsonProperty("text")
        private String text;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyRating {
        @JsonProperty("category")
        private String category;
        
        @JsonProperty("probability")
        private String probability;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageMetadata {
        @JsonProperty("promptTokenCount")
        private Integer promptTokenCount;
        
        @JsonProperty("candidatesTokenCount")
        private Integer candidatesTokenCount;
        
        @JsonProperty("totalTokenCount")
        private Integer totalTokenCount;
    }
} 