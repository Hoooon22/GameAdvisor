package com.gameadvisor.service;

import com.gameadvisor.model.Game;
import com.gameadvisor.repository.GameRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class GameService {

    private final GameRepository gameRepository;

    @Autowired
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * 모든 게임 조회
     */
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    /**
     * 활성화된 게임들만 조회
     */
    public List<Game> getActiveGames() {
        return gameRepository.findByIsActiveTrue();
    }

    /**
     * 게임 이름으로 게임 조회
     */
    public Optional<Game> getGameByName(String name) {
        return gameRepository.findByNameIgnoreCase(name);
    }

    /**
     * 프로세스 이름으로 게임 조회
     */
    public Optional<Game> getGameByProcessName(String processName) {
        return gameRepository.findByProcessName(processName);
    }

    /**
     * 게임 ID로 조회
     */
    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id);
    }

    /**
     * 새 게임 등록
     */
    public Game registerGame(Game game) {
        log.info("새 게임 등록: {}", game.getName());
        
        // 중복 체크
        Optional<Game> existingGame = gameRepository.findByNameIgnoreCase(game.getName());
        if (existingGame.isPresent()) {
            throw new IllegalArgumentException("이미 등록된 게임입니다: " + game.getName());
        }
        
        return gameRepository.save(game);
    }

    /**
     * 게임 정보 업데이트
     */
    public Game updateGame(Long gameId, Game updatedGame) {
        Optional<Game> existingGame = gameRepository.findById(gameId);
        if (existingGame.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 게임입니다: " + gameId);
        }

        Game game = existingGame.get();
        game.setDisplayName(updatedGame.getDisplayName());
        game.setProcessName(updatedGame.getProcessName());
        game.setDescription(updatedGame.getDescription());
        game.setIsActive(updatedGame.getIsActive());

        log.info("게임 정보 업데이트: {}", game.getName());
        return gameRepository.save(game);
    }

    /**
     * 게임 활성화/비활성화
     */
    public Game toggleGameActivation(String gameName) {
        Optional<Game> gameOpt = gameRepository.findByNameIgnoreCase(gameName);
        if (gameOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 게임입니다: " + gameName);
        }

        Game game = gameOpt.get();
        game.setIsActive(!game.getIsActive());
        
        log.info("게임 활성화 상태 변경: {} -> {}", gameName, game.getIsActive());
        return gameRepository.save(game);
    }

    /**
     * 게임 삭제
     */
    public void deleteGame(Long gameId) {
        Optional<Game> game = gameRepository.findById(gameId);
        if (game.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 게임입니다: " + gameId);
        }

        log.info("게임 삭제: {}", game.get().getName());
        gameRepository.deleteById(gameId);
    }

    /**
     * 게임 지원 여부 확인
     */
    public boolean isGameSupported(String gameName) {
        return gameRepository.isGameSupported(gameName);
    }

    /**
     * 프로세스 지원 여부 확인
     */
    public boolean isProcessSupported(String processName) {
        return gameRepository.isProcessSupported(processName);
    }

    /**
     * 활성화된 게임 이름 목록 조회
     */
    public List<String> getSupportedGameNames() {
        return gameRepository.findAllActiveGameNames();
    }

    /**
     * 활성화된 게임 표시 이름 목록 조회
     */
    public List<String> getSupportedDisplayNames() {
        return gameRepository.findAllActiveDisplayNames();
    }

    /**
     * 게임 이름으로 벡터 테이블 이름 조회
     */
    public Optional<String> getVectorTableName(String gameName) {
        return gameRepository.findVectorTableNameByGameName(gameName);
    }

    /**
     * 게임 검색 (이름 또는 표시 이름으로)
     */
    public List<Game> searchGames(String keyword) {
        List<Game> nameResults = gameRepository.findByNameContainingIgnoreCase(keyword);
        List<Game> displayNameResults = gameRepository.findByDisplayNameContainingIgnoreCase(keyword);
        
        // 중복 제거를 위해 Stream 사용
        return java.util.stream.Stream.concat(
                nameResults.stream(),
                displayNameResults.stream()
        ).distinct().collect(java.util.stream.Collectors.toList());
    }

    /**
     * 활성화된 게임 수 조회
     */
    public long getActiveGameCount() {
        return gameRepository.countActiveGames();
    }

    /**
     * 게임 통계 정보 조회
     */
    public GameStats getGameStats() {
        long totalGames = gameRepository.count();
        long activeGames = gameRepository.countActiveGames();
        long inactiveGames = totalGames - activeGames;

        return new GameStats(totalGames, activeGames, inactiveGames);
    }

    /**
     * 게임 통계 정보를 담는 내부 클래스
     */
    public static class GameStats {
        private final long totalGames;
        private final long activeGames;
        private final long inactiveGames;

        public GameStats(long totalGames, long activeGames, long inactiveGames) {
            this.totalGames = totalGames;
            this.activeGames = activeGames;
            this.inactiveGames = inactiveGames;
        }

        public long getTotalGames() { return totalGames; }
        public long getActiveGames() { return activeGames; }
        public long getInactiveGames() { return inactiveGames; }
        public double getActivePercentage() { 
            return totalGames > 0 ? (double) activeGames / totalGames * 100 : 0; 
        }
    }
} 