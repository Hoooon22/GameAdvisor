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
    
    @JsonProperty("tools")
    private List<Tool> tools;
    
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
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tool {
        @JsonProperty("function_declarations")
        private List<FunctionDeclaration> functionDeclarations;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionDeclaration {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("parameters")
        private Parameters parameters;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameters {
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("properties")
        private Object properties;
        
        @JsonProperty("required")
        private List<String> required;
    }
} 