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

## 개발 진행상황

- 백엔드(Spring Boot)에서 MySQL 데이터베이스 연동 및 게임 목록 조회 기능 구현
- 클라이언트(JavaFX)에서 서버와 연동하여 게임 프로세스 감지 기능 구현
- 클라이언트 화면을 좌측 상단에 항상 최상단(오버레이)으로 표시하도록 개선
- 오버레이 창을 반투명(70%)하게 적용하여 시인성 및 UX 개선
- 오버레이 창 크기를 400x200으로 조정하여 화면 차지 최소화

### [2024-06-30] 윈도우 환경 게임 프로세스 및 윈도우 탐지 구조 개선
- 클라이언트가 서버에 의존하지 않고, **로컬에서 직접 실행 중인 게임 프로세스(PID)를 탐지**하도록 구조를 개선
- JNA(Java Native Access)와 Windows API를 활용하여, PID로부터 최상위 윈도우 핸들(HWND) 및 윈도우 위치/크기(RECT)를 직접 조회
- 탐지 성공 시, 오버레이가 즉시 적용되고, 상태 라벨에 `탐지 성공: [게임명] ([프로세스명])` 메시지가 표시됨
- 탐지 성공 후에는 자동으로 탐지 서비스가 중단되어, 불필요한 반복 탐색을 방지
- 주요 코드: `WindowUtils.findMainWindowByPid`, `WindowUtils.getWindowRectByHwnd`, `ProcessScanService`, `GameAdvisorClient`
- 동작 흐름: 프로세스명+PID 추출 → PID로 HWND/RECT 조회 → 탐지 성공 시 오버레이 및 메시지 표시, 서비스 중단