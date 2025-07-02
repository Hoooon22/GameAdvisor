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
public class GeminiRequest {
    
    @JsonProperty("contents")
    private List<Content> contents;
    
    @JsonProperty("generationConfig")
    private GenerationConfig generationConfig;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        @JsonProperty("parts")
        private List<Part> parts;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        @JsonProperty("text")
        private String text;
        
        @JsonProperty("inline_data")
        private InlineData inlineData;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InlineData {
        @JsonProperty("mime_type")
        private String mimeType;
        
        @JsonProperty("data")
        private String data;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationConfig {
        @JsonProperty("temperature")
        private Double temperature;
        
        @JsonProperty("topK")
        private Integer topK;
        
        @JsonProperty("topP")
        private Double topP;
        
        @JsonProperty("maxOutputTokens")
        private Integer maxOutputTokens;
        
        @JsonProperty("stopSequences")
        private List<String> stopSequences;
    }
} 