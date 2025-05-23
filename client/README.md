# GameAdvisor Client

게임 어드바이저의 데스크톱 클라이언트 프로그램입니다.

## 기술 스택

- Java 17
- JavaFX (예정)
- Gradle

## 개발 환경 설정

1. JDK 17 설치
2. (선택) Scene Builder 설치 - JavaFX UI 개발용

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew run
```

## 주요 기능

1. 게임 프로세스 감지
2. 실시간 게임 데이터 수집
3. 서버와 실시간 통신
4. 전략 추천 UI 제공

## 개발 가이드

### 프로젝트 구조

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── gameadvisor/
│   │           └── client/
│   │               ├── GameAdvisorClient.java
│   │               ├── controller/
│   │               ├── model/
│   │               └── view/
│   └── resources/
│       ├── fxml/
│       └── css/
└── test/
    └── java/
```

### 코딩 컨벤션

- Google Java Style Guide를 따릅니다
- JavaFX FXML 파일은 `resources/fxml` 디렉토리에 위치
- CSS 파일은 `resources/css` 디렉토리에 위치