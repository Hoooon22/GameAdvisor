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

## 주요 변경점 (2024-06)

### 윈도우 환경 프로세스 및 게임 윈도우 탐지 방식 개선
- 클라이언트가 서버에 의존하지 않고, **로컬에서 직접 실행 중인 게임 프로세스(PID)를 탐지**합니다.
- JNA(Java Native Access)와 Windows API를 활용하여, PID로부터 최상위 윈도우 핸들(HWND) 및 윈도우 위치/크기(RECT)를 직접 조회합니다.
- 탐지 성공 시, 오버레이가 즉시 적용되고, 상태 라벨에 `탐지 성공: [게임명] ([프로세스명])` 메시지가 표시됩니다.
- 탐지 성공 후에는 자동으로 탐지 서비스가 중단되어, 불필요한 반복 탐색을 방지합니다.

#### 관련 주요 코드
- `WindowUtils.findMainWindowByPid(int pid)`: PID로 최상위 윈도우 핸들(HWND) 반환
- `WindowUtils.getWindowRectByHwnd(HWND hwnd)`: HWND로 RECT(윈도우 위치/크기) 반환
- `ProcessScanService`: 실행 중인 프로세스 목록에서 PID와 프로세스명을 추출하고, RECT까지 연동하여 게임 윈도우를 식별
- `GameAdvisorClient`: 탐지 성공 시 오버레이 적용 및 메시지 표시, 서비스 중단

#### 예시 동작 흐름
1. 클라이언트가 실행 중인 프로세스 목록과 PID를 추출
2. 각 게임의 프로세스명과 매칭되는 PID를 찾음
3. 해당 PID로부터 HWND, RECT를 얻어 게임 윈도우 위치/크기를 파악
4. 탐지 성공 시 오버레이 적용 및 메시지 표시, 탐지 서비스 중단


---