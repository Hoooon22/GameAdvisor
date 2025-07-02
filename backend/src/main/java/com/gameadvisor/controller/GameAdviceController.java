package com.gameadvisor.controller;

import com.gameadvisor.model.GameAdviceRequest;
import com.gameadvisor.model.GameAdviceResponse;
import com.gameadvisor.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/advice")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GameAdviceController {
    
    private final GeminiService geminiService;
    
    @PostMapping("/game")
    public ResponseEntity<GameAdviceResponse> getGameAdvice(@RequestBody GameAdviceRequest request) {
        log.info("게임 조언 요청 받음: {}", request.getGameName());
        
        try {
            GameAdviceResponse response = geminiService.getGameAdvice(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("게임 조언 처리 중 오류 발생", e);
            
            GameAdviceResponse errorResponse = GameAdviceResponse.builder()
                    .advice("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                    .characterName("게임 어드바이저")
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
                    
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("게임 어드바이스 API 서버가 정상 작동 중입니다!");
    }
} 