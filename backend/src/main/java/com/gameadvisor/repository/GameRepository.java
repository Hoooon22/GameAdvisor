package com.gameadvisor.repository;

import com.gameadvisor.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    // 앞으로 필요한 커스텀 쿼리를 여기에 추가할 수 있습니다.
    // 예: List<Game> findByNameContaining(String name);
} 