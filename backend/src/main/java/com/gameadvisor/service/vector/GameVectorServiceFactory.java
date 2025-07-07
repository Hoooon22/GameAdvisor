package com.gameadvisor.service.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class GameVectorServiceFactory {
    
    private final List<GameVectorService> vectorServices;
    
    @Autowired
    public GameVectorServiceFactory(List<GameVectorService> vectorServices) {
        this.vectorServices = vectorServices;
        log.info("벡터 서비스 팩토리 초기화: {} 개 서비스 등록", vectorServices.size());
        
        for (GameVectorService service : vectorServices) {
            log.info("등록된 벡터 서비스: {}", service.getGameName());
        }
    }
    
    /**
     * 게임 이름으로 적절한 벡터 서비스 조회
     * @param gameName 게임 이름
     * @return 해당 게임의 벡터 서비스
     * @throws IllegalArgumentException 지원하지 않는 게임인 경우
     */
    public GameVectorService getService(String gameName) {
        if (gameName == null || gameName.trim().isEmpty()) {
            throw new IllegalArgumentException("게임 이름이 없습니다");
        }
        
        Optional<GameVectorService> service = vectorServices.stream()
                .filter(s -> s.supports(gameName))
                .findFirst();
        
        if (service.isPresent()) {
            log.debug("게임 '{}' 에 대한 벡터 서비스 선택: {}", gameName, service.get().getGameName());
            return service.get();
        }
        
        log.warn("지원하지 않는 게임: {}", gameName);
        throw new IllegalArgumentException("지원하지 않는 게임입니다: " + gameName);
    }
    
    /**
     * 지원하는 게임인지 확인
     * @param gameName 게임 이름
     * @return 지원 여부
     */
    public boolean isSupported(String gameName) {
        if (gameName == null || gameName.trim().isEmpty()) {
            return false;
        }
        
        return vectorServices.stream()
                .anyMatch(s -> s.supports(gameName));
    }
    
    /**
     * 지원하는 모든 게임 목록 조회
     * @return 지원하는 게임 이름 목록
     */
    public List<String> getSupportedGames() {
        return vectorServices.stream()
                .map(GameVectorService::getGameName)
                .toList();
    }
    
    /**
     * 서비스별 통계 정보 조회
     * @return 서비스별 상태 정보
     */
    public String getServiceStats() {
        StringBuilder stats = new StringBuilder("벡터 서비스 현황:\n");
        
        for (GameVectorService service : vectorServices) {
            stats.append("- ").append(service.getGameName()).append(" 서비스: 활성\n");
        }
        
        return stats.toString();
    }
} 