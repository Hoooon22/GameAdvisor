# GameAdvisor Backend

게임 어드바이저의 백엔드 서버 프로젝트입니다. AI 기반 게임 화면 분석과 웹 검색을 통합하여 실시간 게임 조언을 제공합니다.

## 🎯 주요 기능

- **AI 화면 분석**: Google Gemini Vision API를 활용한 실시간 게임 화면 분석
- **웹 검색 통합**: 최신 공략 정보를 웹에서 검색하여 AI 분석과 결합
- **벡터 검색**: 게임별 특화된 지식 베이스 검색 시스템
- **하이브리드 조언**: AI 분석 + 웹 검색 + 벡터 DB를 결합한 종합적인 게임 조언
- **RESTful API**: 클라이언트와의 효율적인 통신을 위한 API 제공

## 기술 스택

- **Java 17**: 최신 LTS 버전의 Java 사용
- **Spring Boot 3.5.0**: 현대적인 웹 애플리케이션 프레임워크
- **Spring Data JPA**: 효율적인 데이터베이스 ORM
- **MySQL 8.0**: 안정적인 관계형 데이터베이스
- **Gradle**: 빌드 자동화 도구
- **Google Gemini API**: 최첨단 AI 비전 모델
- **Jackson**: JSON 처리를 위한 라이브러리

## 프로젝트 구조

```
backend/
├── src/main/java/com/gameadvisor/
│   ├── GameadvisorApplication.java          # Spring Boot 메인 클래스
│   ├── controller/
│   │   ├── GameAdviceController.java        # 게임 조언 API 컨트롤러
│   │   ├── GameController.java              # 게임 관리 API 컨트롤러
│   │   └── GameVectorController.java        # 벡터 검색 API 컨트롤러
│   ├── model/
│   │   ├── Game.java                        # 게임 엔티티
│   │   ├── GameAdviceRequest.java           # 조언 요청 DTO
│   │   ├── GameAdviceResponse.java          # 조언 응답 DTO
│   │   ├── ScreenAnalysisRequest.java       # 화면 분석 요청 DTO
│   │   ├── ScreenAnalysisResponse.java      # 화면 분석 응답 DTO
│   │   ├── GeminiRequest.java               # Gemini API 요청 DTO
│   │   ├── GeminiResponse.java              # Gemini API 응답 DTO
│   │   ├── WebSearchRequest.java            # 웹 검색 요청 DTO
│   │   ├── WebSearchResponse.java           # 웹 검색 응답 DTO
│   │   └── vector/                          # 벡터 검색 관련 모델
│   │       ├── BaseGameKnowledge.java
│   │       ├── BloonsTDKnowledge.java
│   │       ├── MasterDuelKnowledge.java
│   │       └── VectorSearchResult.java
│   ├── service/
│   │   ├── GameService.java                 # 게임 관리 서비스
│   │   ├── GeminiService.java               # Gemini AI 서비스
│   │   ├── WebSearchService.java            # 웹 검색 서비스
│   │   └── vector/                          # 벡터 검색 서비스
│   │       ├── GameVectorService.java
│   │       ├── GameVectorServiceFactory.java
│   │       ├── BloonsTDVectorService.java
│   │       ├── MasterDuelVectorService.java
│   │       └── BloonsTDSampleDataService.java
│   └── repository/
│       ├── GameRepository.java              # 게임 데이터 저장소
│       └── vector/                          # 벡터 검색 저장소
│           ├── GameVectorRepository.java
│           └── BloonsTDVectorRepository.java
└── src/main/resources/
    ├── application.properties               # 기본 설정
    ├── schema.sql                          # 데이터베이스 스키마
    └── static/                             # 정적 리소스
```

## 개발 환경 설정

### 1. JDK 17 설치
Oracle JDK 17 또는 OpenJDK 17 이상을 설치합니다.

### 2. MySQL 설치 및 데이터베이스 생성

```sql
CREATE DATABASE gameadvisor_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 환경 설정 파일 생성

`backend/src/main/resources/application-local.properties` 파일을 생성합니다:

```properties
# 로컬 개발 환경 설정
# 이 파일은 Git에 커밋되지 않습니다.

# Gemini API 설정 - 실제 API 키를 여기에 입력하세요
gemini.api.key=YOUR_ACTUAL_GEMINI_API_KEY_HERE
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent
gemini.api.timeout=30000

# 데이터베이스 설정
spring.datasource.url=jdbc:mysql://localhost:3306/gameadvisor_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
spring.datasource.username=root
spring.datasource.password=your_password_here
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# 로깅 설정
logging.level.com.gameadvisor=DEBUG
logging.level.org.springframework.web=DEBUG
```

### 4. API 키 발급

1. **Google Gemini API**: [Google AI Studio](https://makersuite.google.com/app/apikey)에서 API 키 발급
2. API 키를 `application-local.properties`의 `gemini.api.key`에 설정

## 빌드 및 실행

### 개발 환경 실행
```bash
# 로컬 프로파일로 실행 (권장)
./gradlew bootRun --args='--spring.profiles.active=local'

# Windows PowerShell에서
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 일반 실행
```bash
./gradlew bootRun
```

### 빌드
```bash
./gradlew build
```

### JAR 파일 실행
```bash
./gradlew bootJar
java -jar build/libs/gameadvisor-backend-*.jar --spring.profiles.active=local
```

## 🚀 API 엔드포인트

### 1. 화면 분석 API
- **POST** `/api/advice/analyze-screen` - AI 기반 게임 화면 분석
  ```json
  {
    "gameTitle": "Bloons TD 6",
    "imageData": "base64_encoded_image_data",
    "analysisType": "STRATEGY_ADVICE"
  }
  ```

### 2. 게임 조언 API
- **POST** `/api/advice/game` - 텍스트 기반 게임 조언 요청
- **GET** `/api/advice/test` - 서버 상태 확인

### 3. 벡터 검색 API
- **POST** `/api/vector/search` - 게임별 지식 베이스 검색
- **GET** `/api/vector/games` - 지원 게임 목록 조회

### 4. 게임 관리 API
- **GET** `/api/games` - 지원 게임 목록
- **POST** `/api/games` - 새 게임 등록

## 📊 주요 서비스 설명

### GeminiService
- Google Gemini Vision API를 활용한 이미지 분석
- Base64 인코딩된 게임 스크린샷을 분석하여 조언 생성
- 비동기 처리로 빠른 응답 시간 보장

### WebSearchService
- 실시간 웹 검색을 통한 최신 공략 정보 수집
- 게임별 맞춤형 검색 쿼리 생성
- 검색 결과 필터링 및 요약

### GameVectorService
- 게임별 특화된 지식 베이스 관리
- 벡터 유사도 검색으로 관련성 높은 정보 제공
- 블룬스 TD 6, 마스터 듀얼 등 게임별 전문 지식 제공

## 🧪 테스트

### 전체 테스트 실행
```bash
./gradlew test
```

### 특정 테스트 실행
```bash
./gradlew test --tests "com.gameadvisor.service.GeminiServiceTest"
```

### API 테스트
서버 실행 후 다음 URL로 테스트:
- 상태 확인: `GET http://localhost:8080/api/advice/test`
- 화면 분석: `POST http://localhost:8080/api/advice/analyze-screen`

## 🔒 보안 및 설정

### 환경별 설정
- **개발**: `application-local.properties` 사용
- **프로덕션**: 환경 변수 또는 외부 설정 파일 사용

### API 키 보안
- `application-local.properties`는 `.gitignore`에 포함
- 프로덕션에서는 환경 변수 사용 권장:
  ```bash
  export GEMINI_API_KEY=your_api_key_here
  ```

### CORS 설정
클라이언트와의 통신을 위해 CORS가 설정되어 있습니다.

## 🛠️ 문제 해결

### API 키 관련 오류
```
Error: Invalid API key
```
- `application-local.properties` 파일 존재 확인
- API 키 유효성 Google AI Studio에서 재확인
- 프로파일 활성화 확인: `--spring.profiles.active=local`

### 데이터베이스 연결 오류
```
Error: Connection refused
```
- MySQL 서버 실행 상태 확인
- 데이터베이스 이름, 사용자명, 비밀번호 확인
- 방화벽 설정 확인

### 메모리 부족 오류
```bash
# JVM 힙 메모리 증가
./gradlew bootRun -Dorg.gradle.jvmargs="-Xmx2g"
```

## 📈 성능 최적화

- **비동기 처리**: AI 분석 및 웹 검색의 비동기 처리
- **커넥션 풀링**: HikariCP를 통한 데이터베이스 연결 관리
- **캐싱**: 자주 사용되는 데이터의 메모리 캐싱
- **이미지 압축**: 클라이언트에서 전송되는 이미지 최적화

## 🔄 배포

### Docker 배포 (예정)
```dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

---

**주의사항**: 
- API 키는 절대 공개 저장소에 커밋하지 마세요
- 개발 환경에서는 반드시 `local` 프로파일을 사용하세요
- 프로덕션 배포 시 보안 설정을 재검토하세요