-- 기존 테이블 컬럼 타입 업데이트
-- 실행 방법: MySQL에서 이 파일을 실행하거나 각 명령을 개별 실행

-- BloonsTD 테이블의 tower_types 컬럼을 TEXT로 변경
ALTER TABLE vector_knowledge_bloonstd MODIFY COLUMN tower_types TEXT;

-- MasterDuel 테이블의 card_types 컬럼을 TEXT로 변경
ALTER TABLE vector_knowledge_masterduel MODIFY COLUMN card_types TEXT;

-- 변경 확인
DESCRIBE vector_knowledge_bloonstd;
DESCRIBE vector_knowledge_masterduel; 