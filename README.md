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
- JavaFX (UI)
- JNA (윈도우 네이티브 연동)

## 시작하기

### 요구사항
- JDK 17 이상
- Gradle 8.x
- PostgreSQL 15 이상 (백엔드)

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

1. 실시간 게임 프로세스 감지 및 매칭
2. 게임 창 위치/크기(RECT) 추적 및 오버레이 동기화
3. 맞춤형 전략 추천(서버 연동)
4. 실시간 피드백 제공

## 실제 사용 경험 및 특징

- 윈도우 환경에서 게임을 실행하면, 클라이언트가 자동으로 실행 중인 프로세스를 탐지해 DB에 등록된 게임과 매칭합니다.
- 게임 창이 감지되면, 오버레이가 게임 창 바깥 영역만 반투명하게 덮어주며, 오버레이는 항상 게임 창 바로 뒤에 위치합니다.
- 게임 창을 이동하거나 크기를 조절, 최소화/복원할 때마다 오버레이가 실시간으로 따라가서 UX가 매우 자연스럽습니다.
- 불필요한 디버그 로그는 모두 제거되어, 실제 매칭된 게임만 간결하게 출력됩니다.
- 맥/리눅스 환경에서는 프로세스 탐지까지만 지원하며, 오버레이 및 창 위치 동기화는 미지원입니다.

## 개발 가이드

자세한 개발 가이드는 각 모듈의 README.md를 참고해주세요:
- [Backend 개발 가이드](backend/README.md)
- [Client 개발 가이드](client/README.md)

## 라이선스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## 최근 주요 개선 내역

- 윈도우 환경에서 게임 창 이동/크기 변경/최소화/복원 시 오버레이가 실시간으로 반응하도록 구조 개선
- 오버레이(Stage)가 항상 게임 창 바로 뒤에 오도록 Z-Order 제어 코드 추가(윈도우 전용)
- 게임 프로세스 매칭 시, 불필요한 전체 프로세스 로그 제거 및 매칭 성공/실패만 요약 출력
- 코드 전체적으로 OS별 분기 및 예외 처리 강화
- 실제 사용 환경에서 여러 번 테스트하며, 게임 UX와 오버레이 반응성을 집중적으로 개선함