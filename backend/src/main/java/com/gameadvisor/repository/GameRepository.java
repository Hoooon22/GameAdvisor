package com.gameadvisor.repository;

import com.gameadvisor.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    /**
     * 게임 이름으로 게임 조회
     */
    Optional<Game> findByName(String name);
    
    /**
     * 게임 이름으로 게임 조회 (대소문자 무시)
     */
    Optional<Game> findByNameIgnoreCase(String name);
    
    /**
     * 프로세스 이름으로 게임 조회
     */
    Optional<Game> findByProcessName(String processName);
    
    /**
     * 활성화된 게임들만 조회
     */
    List<Game> findByIsActiveTrue();
    
    /**
     * 비활성화된 게임들 조회
     */
    List<Game> findByIsActiveFalse();
    
    /**
     * 게임 이름에 특정 문자열이 포함된 게임들 조회
     */
    List<Game> findByNameContainingIgnoreCase(String name);
    
    /**
     * 표시 이름에 특정 문자열이 포함된 게임들 조회
     */
    List<Game> findByDisplayNameContainingIgnoreCase(String displayName);
    
    /**
     * 벡터 테이블 이름으로 게임 조회
     */
    Optional<Game> findByVectorTableName(String vectorTableName);
    
    /**
     * 활성화된 게임 수 조회
     */
    @Query("SELECT COUNT(g) FROM Game g WHERE g.isActive = true")
    long countActiveGames();
    
    /**
     * 지원되는 게임인지 확인
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Game g WHERE g.name = :name AND g.isActive = true")
    boolean isGameSupported(String name);
    
    /**
     * 프로세스 이름으로 지원되는 게임인지 확인
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Game g WHERE g.processName = :processName AND g.isActive = true")
    boolean isProcessSupported(String processName);
    
    /**
     * 모든 활성화된 게임의 이름 목록 조회
     */
    @Query("SELECT g.name FROM Game g WHERE g.isActive = true ORDER BY g.name")
    List<String> findAllActiveGameNames();
    
    /**
     * 모든 활성화된 게임의 표시 이름 목록 조회
     */
    @Query("SELECT g.displayName FROM Game g WHERE g.isActive = true ORDER BY g.displayName")
    List<String> findAllActiveDisplayNames();
    
    /**
     * 게임 이름으로 벡터 테이블 이름 조회
     */
    @Query("SELECT g.vectorTableName FROM Game g WHERE g.name = :name AND g.isActive = true")
    Optional<String> findVectorTableNameByGameName(String name);
} 