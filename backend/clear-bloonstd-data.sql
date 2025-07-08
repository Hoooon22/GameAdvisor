-- BloonsTD 벡터 데이터 완전 초기화
-- 기존 데이터를 모두 삭제하고 깨끗한 상태로 시작

TRUNCATE TABLE vector_knowledge_bloonstd;

-- 벡터 검색 로그도 초기화 (선택사항)
DELETE FROM vector_search_log WHERE game_name = 'BloonsTD';

-- 확인용 쿼리
SELECT COUNT(*) as remaining_count FROM vector_knowledge_bloonstd;

SELECT 'BloonsTD 데이터 초기화 완료!' as status; 