# GameAdvisor Backend

게임 어드바이저의 백엔드 서버 프로젝트입니다. 제미나이 API를 사용하여 게임 조언을 제공합니다.

## 기술 스택

- Java 17
- Spring Boot 3.5.0
- Spring Data JPA
- MySQL 8.0
- Gradle
- Google Gemini API

## 개발 환경 설정

### 1. JDK 17 설치

### 2. MySQL 설치 및 데이터베이스 생성

```sql
CREATE DATABASE gameadvisor_db;
```

### 3. 제미나이 API 키 설정 (중요!)

1. [Google AI Studio](https://makersuite.google.com/app/apikey)에서 제미나이 API 키를 발급받습니다.

2. `backend/src/main/resources/application-local.properties` 파일을 생성합니다:

```properties
# 로컬 개발 환경 설정
# 이 파일은 Git에 커밋되지 않습니다.

# Gemini API 설정 - 실제 API 키를 여기에 입력하세요
gemini.api.key=여기에_실제_제미나이_API_키_입력
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
gemini.api.timeout=30000
```

3. 실제 제미나이 API 키를 `YOUR_ACTUAL_GEMINI_API_KEY_HERE` 부분에 입력합니다.

### 4. 데이터베이스 설정

`application.properties`에서 데이터베이스 연결 정보를 확인하고 필요시 수정:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gameadvisor_db
spring.datasource.username=root
spring.datasource.password=password
```

## 빌드 및 실행

```bash
# 로컬 환경으로 실행 (API 키 포함)
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 일반 실행
./gradlew bootRun
```

## API 엔드포인트

### 게임 조언 API

- **POST** `/api/advice/game` - 게임 조언 요청
- **GET** `/api/advice/test` - 서버 상태 확인

#### 요청 예시:

```json
{
  "gameName": "리그 오브 레전드",
  "currentSituation": "랭크 게임에서 계속 지고 있어요",
  "playerLevel": "골드 티어",
  "gameGenre": "MOBA",
  "specificQuestion": "승률을 올리려면 어떻게 해야 할까요?"
}
```

#### 응답 예시:

```json
{
  "advice": "골드 티어에서 승률을 올리려면...",
  "characterName": "게임 어드바이저",
  "gameContext": "리그 오브 레전드",
  "timestamp": "2024-01-01T12:00:00",
  "success": true
}
```

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "com.gameadvisor.YourTest"
```

## API 테스트

서버가 실행된 후 다음 URL로 테스트할 수 있습니다:

- 상태 확인: http://localhost:8080/api/advice/test
- 게임 조언: POST http://localhost:8080/api/advice/game

## 보안 주의사항

- `application-local.properties` 파일은 Git에 커밋되지 않습니다.
- 제미나이 API 키는 절대 공개 저장소에 올리지 마세요.
- 프로덕션 환경에서는 환경 변수를 사용하는 것을 권장합니다.

## 문제 해결

### API 키 관련 오류
- `application-local.properties` 파일이 올바른 위치에 있는지 확인
- API 키가 유효한지 Google AI Studio에서 확인

### 데이터베이스 연결 오류
- MySQL 서버가 실행 중인지 확인
- 데이터베이스 연결 정보가 올바른지 확인