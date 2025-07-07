# BTD 벡터 DB 구축 및 운영 가이드

## 📖 개요

이 가이드는 Bloons Tower Defense (BTD) 게임에 대한 벡터 데이터베이스를 구축하고 운영하는 방법을 제공합니다. 벡터 DB를 통해 게임 상황에 맞는 최적의 전략과 조언을 AI가 제공할 수 있습니다.

## 🎯 벡터 DB의 핵심 기능

### 1. 상황별 지식 검색
- **세라믹 방어**: 라운드 63-76의 세라믹 블룬 대응 전략
- **MOAB 공략**: 고레벨 MOAB류 블룬 처리 방법
- **카모 탐지**: 위장 블룬 탐지 및 처리 전략
- **납 블룬 처리**: 납 블룬의 특성을 활용한 공략법
- **보스 전략**: 특별 이벤트 보스 블룬 공략
- **타워 조합**: 효율적인 타워 시너지 활용
- **경제 구축**: 안정적인 수입원 확보 방법

### 2. 라운드별 맞춤 전략
- **초반 (1-40라운드)**: 경제 구축 및 기본 방어
- **중반 (41-80라운드)**: 카모/세라믹 대응 및 화력 증강
- **후반 (81-100라운드)**: MOAB/BAD 처리 및 최종 방어
- **프리플레이 (100+라운드)**: 무한 진행을 위한 고급 전략

### 3. 난이도별 최적화
- **쉬움**: 기본적인 타워 배치 및 업그레이드
- **보통**: 효율적인 타워 조합 및 경제 운영
- **어려움**: 고급 전략 및 정밀한 타이밍
- **전문가**: 완벽한 최적화와 고난이도 맵 공략

## 🚀 벡터 DB 구축 방법

### 1단계: 환경 설정

```bash
# 백엔드 서버 실행
cd backend
./gradlew bootRun
```

### 2단계: 데이터베이스 초기화

벡터 DB는 MySQL을 사용하며, 애플리케이션 시작 시 자동으로 테이블이 생성됩니다.

**주요 테이블**: `vector_knowledge_bloonstd`
- 벡터 임베딩과 게임 전략 지식을 저장
- JSON 형태의 태그 및 임베딩 벡터 지원
- 성공률 및 사용량 통계 추적

### 3단계: 지식 데이터 생성

PowerShell 스크립트를 사용하여 자동으로 벡터 DB를 구축할 수 있습니다:

```powershell
# 통합 벡터 DB 구축 스크립트 실행
.\setup-btd-vector-db.ps1
```

또는 수동으로 API를 호출할 수 있습니다:

```bash
# 1. 기본 샘플 데이터 생성
curl -X POST http://localhost:8080/api/btd-vector/sample-data

# 2. 고급 전략 데이터 생성
curl -X POST http://localhost:8080/api/btd-vector/advanced-data

# 3. 상황별 맞춤 데이터 생성
curl -X POST http://localhost:8080/api/btd-vector/situation-data
```

## 🔍 벡터 검색 사용법

### API를 통한 검색

```bash
curl -X POST http://localhost:8080/api/btd-vector/search \
  -H "Content-Type: application/json" \
  -d '{
    "situation": "라운드 80에서 BAD를 못 잡겠어요",
    "limit": "5"
  }'
```

### 검색 결과 예시

```json
{
  "success": true,
  "situation": "라운드 80에서 BAD를 못 잡겠어요",
  "resultCount": 3,
  "results": [
    {
      "knowledge": {
        "title": "라운드 80 첫 BAD 공략 전략",
        "advice": "2-0-5 슈퍼 몽키의 다크 챔피언이나 5-2-0 몽키 에이스의 그라운드 제로가 효과적입니다...",
        "situationType": "moab_battle",
        "successRate": 0.76
      },
      "similarity": 0.89
    }
  ]
}
```

## 📊 벡터 DB 아키텍처

### 1. 데이터 모델

```java
// BTD 지식 모델 구조
public class BloonsTDKnowledge {
    private String situationType;    // 상황 유형
    private String roundRange;       // 라운드 범위
    private String difficulty;       // 난이도
    private List<String> towerTypes; // 관련 타워들
    private String title;           // 전략 제목
    private String content;         // 상세 설명
    private String advice;          // 구체적 조언
    private List<String> tags;      // 검색 태그
    private List<Double> embedding; // 벡터 임베딩
    private Double confidence;      // 신뢰도
    private Double successRate;     // 성공률
    private Integer usageCount;     // 사용 횟수
}
```

### 2. 벡터 임베딩 생성

현재는 간단한 해시 기반 임베딩을 사용하지만, 향후 다음과 같은 방법으로 개선 가능:

- **Gemini API**: Google의 텍스트 임베딩 모델 활용
- **OpenAI Embeddings**: OpenAI의 embedding 모델 사용
- **로컬 모델**: Sentence-BERT 등 로컬 임베딩 모델

### 3. 유사도 계산

코사인 유사도를 사용하여 검색 쿼리와 저장된 지식 간의 유사성을 측정:

```java
private double calculateCosineSimilarity(List<Double> vec1, List<Double> vec2) {
    // 두 벡터 간의 코사인 유사도 계산
    double dotProduct = 0.0;
    double norm1 = 0.0;
    double norm2 = 0.0;
    
    for (int i = 0; i < vec1.size(); i++) {
        dotProduct += vec1.get(i) * vec2.get(i);
        norm1 += vec1.get(i) * vec1.get(i);
        norm2 += vec2.get(i) * vec2.get(i);
    }
    
    return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
}
```

## 🛠️ 벡터 DB 확장 방법

### 1. 새로운 지식 추가

```java
// 새로운 BTD 전략 지식 추가
BloonsTDKnowledge newKnowledge = BloonsTDKnowledge.builder()
    .situationType("custom_strategy")
    .roundRange("mid_game")
    .difficulty("hard")
    .towerTypes(Arrays.asList("Wizard", "Druid"))
    .title("위저드-드루이드 시너지 전략")
    .content("...")
    .advice("...")
    .tags(Arrays.asList("위저드", "드루이드", "시너지"))
    .embedding(generateEmbedding("위저드 드루이드 시너지"))
    .confidence(0.85)
    .successRate(0.80)
    .build();

vectorService.saveKnowledge(newKnowledge);
```

### 2. 피드백 시스템 구현

사용자 피드백을 통해 지식의 품질을 개선할 수 있습니다:

```bash
# 지식 사용량 증가
curl -X POST http://localhost:8080/api/btd-vector/knowledge/{knowledgeId}/use

# 성공률 업데이트
curl -X POST http://localhost:8080/api/btd-vector/knowledge/{knowledgeId}/success \
  -H "Content-Type: application/json" \
  -d '{"successRate": 0.85}'
```

### 3. 실시간 학습

게임 플레이 데이터를 수집하여 실시간으로 벡터 DB를 개선:

1. **스크린 캡처**: 게임 화면 분석으로 상황 인식
2. **결과 추적**: 전략 적용 후 성공/실패 여부 확인
3. **자동 업데이트**: 성공률이 높은 전략의 가중치 증가

### 4. 웹 자료 수집 및 자동 학습 🆕

인터넷에서 최신 BTD 공략 정보를 자동으로 수집하여 벡터 DB를 지속적으로 업데이트할 수 있습니다:

#### 4.1 자동 웹 학습 실행

```powershell
# 모든 BTD 관련 웹 자료 수집 및 학습
.\start-web-learning.ps1 -Mode all

# 특정 주제에 대한 자료 수집
.\start-web-learning.ps1 -Mode topic -Topic "ceramic defense"

# 키워드 기반 자료 수집
.\start-web-learning.ps1 -Mode keyword -Keyword "MOAB strategy"

# 서비스 상태 확인
.\start-web-learning.ps1 -Mode status
```

#### 4.2 수동 API 호출

```bash
# 전체 웹 자료 수집
curl -X POST http://localhost:8080/api/web-learning/collect-all

# 주제별 자료 수집
curl -X POST "http://localhost:8080/api/web-learning/collect-topic?topic=boss%20battle"

# 키워드별 자료 수집
curl -X POST "http://localhost:8080/api/web-learning/collect-keyword?keyword=late%20game"

# 서비스 상태 확인
curl -X GET http://localhost:8080/api/web-learning/status
```

#### 4.3 신뢰할 수 있는 데이터 소스

웹 학습 시스템은 다음과 같은 검증된 사이트에서만 자료를 수집합니다:

- **Reddit** (r/btd6): 커뮤니티 전략 및 팁
- **Bloons Fandom**: 공식 게임 정보 및 가이드
- **GamePress**: 전문적인 게임 분석
- **IGN**: 검증된 게임 가이드
- **Steam Community**: 사용자 가이드 및 토론

#### 4.4 추천 학습 주제

벡터 DB 품질 향상을 위해 다음 주제들을 우선적으로 학습하는 것을 권장합니다:

1. **세라믹 방어** (ceramic defense)
2. **MOAB 전략** (MOAB strategy)
3. **카모 탐지** (camo detection)
4. **납 블룬 처리** (lead popping)
5. **보스 전투** (boss battle)
6. **타워 조합** (tower combinations)
7. **경제 전략** (economy strategy)
8. **후반 게임** (late game strategy)
9. **영웅 능력** (hero abilities)
10. **몽키 지식** (monkey knowledge)

#### 4.5 웹 학습 모니터링

학습 과정을 모니터링하고 결과를 확인할 수 있습니다:

```bash
# 학습된 지식 개수 확인
curl -X GET http://localhost:8080/api/btd-vector/stats

# 최근 학습된 지식 조회
curl -X POST http://localhost:8080/api/btd-vector/search \
  -H "Content-Type: application/json" \
  -d '{
    "situation": "웹에서 수집된",
    "limit": "10"
  }'
```

#### 4.6 자동화된 학습 스케줄

정기적인 웹 학습을 위해 Windows 작업 스케줄러를 활용할 수 있습니다:

```powershell
# 매일 자정에 웹 학습 실행하는 작업 생성
schtasks /create /tn "BTD_WebLearning" /tr "powershell.exe -File C:\Project\GameAdvisor\start-web-learning.ps1 -Mode all" /sc daily /st 00:00
```

## 📈 성능 최적화

### 1. 인덱싱 전략

```sql
-- 주요 검색 필드에 인덱스 생성
CREATE INDEX idx_situation_type ON vector_knowledge_bloonstd(situation_type);
CREATE INDEX idx_round_range ON vector_knowledge_bloonstd(round_range);
CREATE INDEX idx_confidence ON vector_knowledge_bloonstd(confidence DESC);
CREATE INDEX idx_success_rate ON vector_knowledge_bloonstd(success_rate DESC);
```

### 2. 캐싱 전략

자주 검색되는 쿼리 결과를 Redis나 메모리에 캐싱하여 응답 속도 개선.

### 3. 배치 처리

대량의 지식 데이터를 효율적으로 처리하기 위한 배치 작업 구현.

## 🔧 유지보수 가이드

### 1. 정기적인 데이터 정리

```bash
# 사용량이 낮은 지식 데이터 정리
# 성공률이 낮은 전략 재검토
# 중복 데이터 제거
```

### 2. 모니터링

- 검색 응답 시간 모니터링
- 벡터 DB 크기 및 성능 추적
- 사용자 피드백 분석

### 3. 백업 및 복구

```bash
# 데이터베이스 백업
mysqldump -u root -p gameadvisor_db > btd_vector_backup.sql

# 복구
mysql -u root -p gameadvisor_db < btd_vector_backup.sql
```

## 🎮 실제 활용 예시

### 게임 상황별 검색 예시

1. **"라운드 63 세라믹이 너무 많아요"**
   → 세라믹 방어 전략 반환

2. **"물맵에서 어떤 타워를 써야 하나요?"**
   → 물맵 특화 전략 (버커니어, 잠수함) 반환

3. **"초반 경제를 어떻게 구축하나요?"**
   → 바나나 농장 기반 경제 전략 반환

### 통합 게임 어드바이저 연동

클라이언트 애플리케이션에서 벡터 DB를 활용하여:
- 실시간 게임 상황 분석
- 맞춤형 전략 추천
- 시각적 가이드 제공

## 📝 추가 개발 가능 기능

1. **다국어 지원**: 영어, 일본어 등 다양한 언어로 전략 제공
2. **맵별 특화**: 각 맵의 특성에 맞는 전략 데이터베이스
3. **업데이트 추적**: 게임 버전별 메타 변화 반영
4. **커뮤니티 기여**: 사용자가 직접 전략을 추가할 수 있는 시스템
5. **AI 생성**: 대화형 AI로 실시간 전략 생성

---

이 가이드를 통해 BTD 벡터 DB를 성공적으로 구축하고 운영할 수 있습니다. 추가 질문이나 개선 사항이 있다면 언제든지 문의해주세요! 