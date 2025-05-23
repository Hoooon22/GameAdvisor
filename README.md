# GameAdvisor

실시간 게임 상황 분석 및 공략 제공 어드바이저 프로그램

## 프로젝트 구조

```
GameAdvisor/
├── backend/                # Spring Boot 백엔드 서버
│   ├── src/               # 소스 코드
│   ├── build.gradle       # Gradle 빌드 설정
│   └── README.md          # 백엔드 문서
│
└── client/                # Java 클라이언트
    ├── src/              # 소스 코드
    ├── build.gradle      # Gradle 빌드 설정
    └── README.md         # 클라이언트 문서
```

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.5.0
- Gradle
- Spring Data JPA
- PostgreSQL

### Client
- Java 17
- Gradle
- JavaFX (예정)

## 시작하기

### 요구사항
- JDK 17 이상
- Gradle 8.x
- PostgreSQL 15 이상

### 백엔드 서버 실행
```bash
cd backend
./gradlew bootRun
```

### 클라이언트 실행
```bash
cd client
./gradlew run
```

## 주요 기능

1. 실시간 게임 상황 감지
2. 게임 데이터 분석
3. 맞춤형 전략 추천
4. 실시간 피드백 제공

## 개발 가이드

자세한 개발 가이드는 각 모듈의 README.md를 참고해주세요:
- [Backend 개발 가이드](backend/README.md)
- [Client 개발 가이드](client/README.md)

## 라이선스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details