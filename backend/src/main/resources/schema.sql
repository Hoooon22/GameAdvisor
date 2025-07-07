-- 게임별 벡터 지식 테이블

-- 블룬스 TD 벡터 지식 테이블
CREATE TABLE IF NOT EXISTS vector_knowledge_bloonstd (
    id VARCHAR(50) PRIMARY KEY,
    situation_type VARCHAR(50) NOT NULL,
    round_range VARCHAR(20),
    difficulty VARCHAR(20),
    tower_types VARCHAR(200),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    advice TEXT NOT NULL,
    tags JSON,
    embedding JSON NOT NULL,
    confidence DECIMAL(3,2) DEFAULT 0.0,
    success_rate DECIMAL(3,2) DEFAULT 0.0,
    usage_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_situation_type (situation_type),
    INDEX idx_round_range (round_range),
    INDEX idx_difficulty (difficulty),
    INDEX idx_confidence (confidence DESC),
    INDEX idx_success_rate (success_rate DESC),
    INDEX idx_usage_count (usage_count DESC)
);

-- 유기오 마스터 듀얼 벡터 지식 테이블
CREATE TABLE IF NOT EXISTS vector_knowledge_masterduel (
    id VARCHAR(50) PRIMARY KEY,
    situation_type VARCHAR(50) NOT NULL,
    format_type VARCHAR(20),
    archetype VARCHAR(50),
    card_types VARCHAR(200),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    advice TEXT NOT NULL,
    tags JSON,
    embedding JSON NOT NULL,
    confidence DECIMAL(3,2) DEFAULT 0.0,
    win_rate DECIMAL(3,2) DEFAULT 0.0,
    usage_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_situation_type (situation_type),
    INDEX idx_format_type (format_type),
    INDEX idx_archetype (archetype),
    INDEX idx_confidence (confidence DESC),
    INDEX idx_win_rate (win_rate DESC),
    INDEX idx_usage_count (usage_count DESC)
);

-- 벡터 검색 로그 테이블 (성능 모니터링용)
CREATE TABLE IF NOT EXISTS vector_search_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    game_name VARCHAR(50) NOT NULL,
    query_text TEXT NOT NULL,
    result_count INT DEFAULT 0,
    max_similarity DECIMAL(5,4) DEFAULT 0.0,
    search_time_ms INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_game_name (game_name),
    INDEX idx_created_at (created_at)
); 