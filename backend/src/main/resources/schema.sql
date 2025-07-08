-- 게임 정보 테이블 (게임 메타데이터 관리)
CREATE TABLE IF NOT EXISTS games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    process_name VARCHAR(100),
    vector_table_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_name (name),
    INDEX idx_is_active (is_active)
);

-- 게임별 벡터 지식 테이블

-- 블룬스 TD 벡터 지식 테이블
CREATE TABLE IF NOT EXISTS vector_knowledge_bloonstd (
    id VARCHAR(50) PRIMARY KEY,
    situation_type VARCHAR(50) NOT NULL,
    round_range VARCHAR(20),
    difficulty VARCHAR(20),
    tower_types TEXT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    advice TEXT NOT NULL,
    tags JSON,
    embedding JSON NOT NULL,
    confidence DECIMAL(3,2) DEFAULT 0.0,
    success_rate DECIMAL(3,2) DEFAULT 0.0,
    usage_count INT DEFAULT 0,
    source_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_situation_type (situation_type),
    INDEX idx_round_range (round_range),
    INDEX idx_difficulty (difficulty),
    INDEX idx_confidence (confidence DESC),
    INDEX idx_success_rate (success_rate DESC),
    INDEX idx_usage_count (usage_count DESC),
    INDEX idx_created_at (created_at DESC)
);

-- 유기오 마스터 듀얼 벡터 지식 테이블
CREATE TABLE IF NOT EXISTS vector_knowledge_masterduel (
    id VARCHAR(50) PRIMARY KEY,
    situation_type VARCHAR(50) NOT NULL,
    format_type VARCHAR(20),
    archetype VARCHAR(50),
    card_types TEXT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    advice TEXT NOT NULL,
    tags JSON,
    embedding JSON NOT NULL,
    confidence DECIMAL(3,2) DEFAULT 0.0,
    win_rate DECIMAL(3,2) DEFAULT 0.0,
    usage_count INT DEFAULT 0,
    source_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_situation_type (situation_type),
    INDEX idx_format_type (format_type),
    INDEX idx_archetype (archetype),
    INDEX idx_confidence (confidence DESC),
    INDEX idx_win_rate (win_rate DESC),
    INDEX idx_usage_count (usage_count DESC),
    INDEX idx_created_at (created_at DESC)
);

-- 벡터 검색 로그 테이블 (성능 모니터링용)
CREATE TABLE IF NOT EXISTS vector_search_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    game_name VARCHAR(50) NOT NULL,
    query_text TEXT NOT NULL,
    result_count INT DEFAULT 0,
    max_similarity DECIMAL(5,4) DEFAULT 0.0,
    search_time_ms INT DEFAULT 0,
    user_feedback INT DEFAULT 0, -- -1: 부정, 0: 중립, 1: 긍정
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_game_name (game_name),
    INDEX idx_created_at (created_at),
    INDEX idx_search_time_ms (search_time_ms),
    INDEX idx_user_feedback (user_feedback)
);

-- 게임별 설정 테이블
CREATE TABLE IF NOT EXISTS game_settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    game_name VARCHAR(50) NOT NULL,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_game_setting (game_name, setting_key),
    INDEX idx_game_name (game_name)
);

-- 초기 게임 데이터 삽입
INSERT IGNORE INTO games (name, display_name, process_name, vector_table_name, description) VALUES 
('BloonsTD', 'Bloons Tower Defense', 'BloonsTD6', 'vector_knowledge_bloonstd', 'Bloons Tower Defense 6 게임 공략 및 전략 정보'),
('MasterDuel', 'Yu-Gi-Oh! Master Duel', 'YuGiOh', 'vector_knowledge_masterduel', 'Yu-Gi-Oh! Master Duel 덱 구성 및 전략 정보');

-- 기본 게임 설정 삽입
INSERT IGNORE INTO game_settings (game_name, setting_key, setting_value, description) VALUES 
('BloonsTD', 'max_embedding_dimension', '384', '임베딩 벡터 최대 차원 수'),
('BloonsTD', 'similarity_threshold', '0.7', '유사도 검색 최소 임계값'),
('BloonsTD', 'max_search_results', '10', '최대 검색 결과 수'),
('MasterDuel', 'max_embedding_dimension', '384', '임베딩 벡터 최대 차원 수'),
('MasterDuel', 'similarity_threshold', '0.7', '유사도 검색 최소 임계값'),
('MasterDuel', 'max_search_results', '10', '최대 검색 결과 수');

-- 새로운 게임 추가를 위한 템플릿 (참고용 주석)
-- 새 게임을 추가할 때 다음 단계를 따르세요:
-- 1. games 테이블에 게임 정보 추가
-- 2. vector_knowledge_{게임명} 테이블 생성
-- 3. 해당 게임의 GameVectorService 구현
-- 4. 해당 게임의 Repository 구현
-- 5. 샘플 데이터 서비스 구현

/*
예시: 새 게임 'NewGame' 추가시

-- 1. 게임 등록
INSERT INTO games (name, display_name, process_name, vector_table_name, description) VALUES 
('NewGame', 'New Game Title', 'NewGameProcess', 'vector_knowledge_newgame', 'New Game 설명');

-- 2. 벡터 지식 테이블 생성
CREATE TABLE IF NOT EXISTS vector_knowledge_newgame (
    id VARCHAR(50) PRIMARY KEY,
    situation_type VARCHAR(50) NOT NULL,
    -- 게임별 특화 필드들...
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    advice TEXT NOT NULL,
    tags JSON,
    embedding JSON NOT NULL,
    confidence DECIMAL(3,2) DEFAULT 0.0,
    success_metric DECIMAL(3,2) DEFAULT 0.0,
    usage_count INT DEFAULT 0,
    source_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_situation_type (situation_type),
    INDEX idx_confidence (confidence DESC),
    INDEX idx_success_metric (success_metric DESC),
    INDEX idx_usage_count (usage_count DESC)
);

-- 3. 기본 설정 추가
INSERT INTO game_settings (game_name, setting_key, setting_value, description) VALUES 
('NewGame', 'max_embedding_dimension', '384', '임베딩 벡터 최대 차원 수'),
('NewGame', 'similarity_threshold', '0.7', '유사도 검색 최소 임계값'),
('NewGame', 'max_search_results', '10', '최대 검색 결과 수');
*/ 