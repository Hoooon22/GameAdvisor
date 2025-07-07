# GameAdvisor Client

게임 어드바이저의 JavaFX 데스크톱 클라이언트입니다. 게임 화면 위에 투명 오버레이로 AI 어드바이저 캐릭터를 표시하고, 실시간 게임 조언을 제공합니다.

## 🎯 주요 기능

- **물리 기반 캐릭터**: 마우스로 드래그하여 날릴 수 있는 인터랙티브 캐릭터
- **투명 오버레이**: 게임 플레이를 방해하지 않는 100% 투명한 오버레이 시스템
- **실시간 게임 감지**: Windows API를 활용한 자동 게임 프로세스 감지
- **AI 화면 분석**: 게임 스크린샷을 캡쳐하여 AI 분석 요청
- **스마트 말풍선**: 다양한 스타일의 말풍선과 최소화/복원 기능
- **클릭 회피 시스템**: 사용자 클릭을 피해 움직이는 똑똑한 캐릭터
- **물리 시뮬레이션**: 중력, 충돌, 마찰력이 적용된 현실적인 물리 효과

## 기술 스택

- **Java 17**: 최신 LTS 버전의 Java
- **JavaFX 17**: 현대적인 데스크톱 UI 프레임워크
- **Gradle**: 빌드 자동화 도구
- **JNA (Java Native Access)**: Windows API 연동을 위한 라이브러리
- **Jackson**: JSON 처리를 위한 라이브러리
- **OkHttp3**: HTTP 클라이언트 라이브러리

## 프로젝트 구조

```
client/
├── src/main/java/com/gameadvisor/client/
│   ├── ui/
│   │   ├── GameAdvisorClient.java           # 메인 클라이언트 애플리케이션
│   │   └── components/
│   │       ├── character/
│   │       │   ├── AdvisorCharacter.java    # 어드바이저 캐릭터 컴포넌트
│   │       │   ├── CharacterOverlay.java    # 캐릭터 오버레이 관리자
│   │       │   └── SpeechBubble.java        # 말풍선 컴포넌트
│   │       └── GameAdvisorClient.java       # 메인 UI 컨트롤러
│   ├── model/
│   │   ├── Game.java                        # 게임 정보 모델
│   │   ├── GameWindowInfo.java              # 게임 창 정보 모델
│   │   ├── ScreenAnalysisRequest.java       # 화면 분석 요청 DTO
│   │   └── ScreenAnalysisResponse.java      # 화면 분석 응답 DTO
│   ├── network/
│   │   └── ApiClient.java                   # 백엔드 서버와의 HTTP 통신
│   ├── service/
│   │   └── ProcessScanService.java          # 게임 프로세스 스캔 서비스
│   └── util/
│       ├── ScreenCaptureUtil.java           # 화면 캡쳐 유틸리티
│       └── WindowUtils.java                 # Windows API 유틸리티
└── src/main/resources/
    ├── css/
    │   └── style.css                        # JavaFX 스타일시트
    └── fxml/                                # FXML 레이아웃 파일들
```

## 개발 환경 설정

### 1. 필수 요구사항
- **JDK 17** 이상
- **Windows 10/11** (현재 Windows만 지원)
- **JavaFX 17** (Gradle이 자동으로 처리)

### 2. IDE 설정 (선택사항)
- **IntelliJ IDEA**: JavaFX 플러그인 설치 권장
- **Eclipse**: e(fx)clipse 플러그인 설치 권장
- **Scene Builder**: FXML UI 디자인용 (선택사항)

### 3. 백엔드 서버 연결
클라이언트는 `http://localhost:8080`에서 실행 중인 백엔드 서버와 통신합니다.

## 빌드 및 실행

### 개발 환경 실행
```bash
# 클라이언트 실행 (백엔드 서버가 먼저 실행되어야 함)
./gradlew run

# Windows PowerShell에서
./gradlew run
```

### 빌드
```bash
# 프로젝트 빌드
./gradlew build

# 실행 가능한 JAR 생성
./gradlew shadowJar
```

### JAR 파일 실행
```bash
# shadowJar로 생성된 실행 파일
java -jar build/libs/gameadvisor-client-*-all.jar
```

## 🎮 사용법

### 기본 사용 흐름

1. **백엔드 서버 실행**: 먼저 백엔드 서버가 실행되어야 합니다
2. **클라이언트 실행**: `./gradlew run`으로 클라이언트 실행
3. **게임 감지 대기**: 지원되는 게임을 실행하면 자동으로 감지
4. **캐릭터 표시**: 게임 창 위에 어드바이저 캐릭터가 나타남
5. **인터랙션**: 캐릭터와 상호작용하며 게임 조언 받기

### 캐릭터 인터랙션

#### 물리 기반 조작
- **드래그**: 캐릭터를 마우스로 클릭하고 드래그
- **투사**: 원하는 방향으로 드래그한 후 놓으면 날아감
- **물리 효과**: 중력(400px/s²), 공기 저항(0.995), 바운스(0.75) 적용
- **충돌 반응**: 벽과 바닥 충돌 시 현실적인 반발 효과

#### 애니메이션 상태
- **IDLE**: 평상시 서 있는 상태 (호흡 애니메이션)
- **WALKING**: 게임 창 내에서 걸어다니는 상태
- **TALKING**: 말풍선과 함께 대화하는 상태
- **THINKING**: 머리를 흔들며 생각하는 상태
- **DRAGGING**: 드래그되는 중 (크기 증가, 회전)
- **FLYING**: 공중에서 날아가는 상태
- **STUNNED**: 충돌 후 기절 상태 (X자 눈, 흔들림)

### AI 화면 분석 기능

1. **분석 요청**: 캐릭터 오른쪽 위의 🔍 버튼 클릭
2. **화면 캡쳐**: 현재 게임 창 영역만 정확히 캡쳐
3. **AI 분석**: 백엔드 서버를 통해 Gemini AI가 화면 분석
4. **조언 표시**: 분석 결과를 말풍선으로 표시

### 말풍선 시스템

#### 말풍선 타입
- **일반 대화**: 기본적인 메시지 (연한 파란색)
- **조언**: 도움말과 팁 (연한 노란색)
- **경고**: 주의사항 (연한 빨간색)
- **성공**: 긍정적인 피드백 (연한 초록색)
- **생각**: 사고 과정 표시 (연한 보라색)
- **공략**: AI 분석 결과 (연한 주황색)

#### 말풍선 제어
- **X 버튼**: 빨간색 X 버튼으로 말풍선 닫기
- **최소화**: 초록색 "-" 버튼으로 작업표시줄 스타일 최소화
- **복원**: 최소화된 바를 클릭하여 말풍선 다시 표시
- **자동 시간**: 텍스트 길이에 따라 2-12초 자동 표시

### 클릭 회피 시스템

- **실시간 감지**: 게임 화면 내 사용자 클릭 위치 추적
- **스마트 회피**: 클릭 위치에서 150px 거리만큼 반대 방향 이동
- **시간 제한**: 클릭 후 3초간 회피 효과 지속
- **게임 방해 최소화**: 사용자의 게임 플레이를 능동적으로 피함

## 🔧 주요 컴포넌트 설명

### GameAdvisorClient
- JavaFX 애플리케이션의 메인 클래스
- 게임 감지 전 상태창 표시
- ProcessScanService와 연동하여 게임 프로세스 모니터링

### CharacterOverlay
- 투명 오버레이 창 관리
- 캐릭터 물리 시뮬레이션 처리
- 사용자 인터랙션 이벤트 처리
- AI 분석 요청 및 결과 처리

### AdvisorCharacter
- 캐릭터 비주얼 렌더링
- 다양한 애니메이션 상태 관리
- 물리 효과 적용 (중력, 충돌, 마찰)
- 드래그 앤 드롭 인터랙션

### SpeechBubble
- 다양한 스타일의 말풍선 렌더링
- 최소화/복원 기능
- 텍스트 길이에 따른 자동 크기 조절
- 부드러운 페이드 애니메이션

### ProcessScanService
- Windows API를 통한 게임 프로세스 감지
- 주기적인 프로세스 스캔 (1초 간격)
- 게임 창 정보 수집 (제목, 크기, 위치)

### WindowUtils
- JNA를 통한 Windows API 호출
- 게임 창 정보 추출
- 창 상태 확인 (최소화, 활성화 등)

### ScreenCaptureUtil
- 게임 창 영역만 정확히 캡쳐
- Robot 클래스를 활용한 스크린샷
- Base64 인코딩으로 서버 전송

### ApiClient
- OkHttp3 기반 HTTP 클라이언트
- 백엔드 서버와의 비동기 통신
- JSON 직렬화/역직렬화 처리

## 🧪 테스트 및 디버깅

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 특정 테스트
./gradlew test --tests "com.gameadvisor.client.service.ProcessScanServiceTest"
```

### 디버그 모드 실행
```bash
# JVM 디버그 옵션과 함께 실행
./gradlew run --debug-jvm
```

### 로깅 설정
`src/main/resources/logback.xml`에서 로깅 레벨 조정:
```xml
<logger name="com.gameadvisor.client" level="DEBUG"/>
```

## 🛠️ 문제 해결

### 일반적인 문제들

#### JavaFX 실행 오류
```
Error: JavaFX runtime components are missing
```
**해결책**: 
- Gradle이 JavaFX 종속성을 자동으로 처리하므로 `./gradlew run` 사용
- IDE에서 실행 시 VM 옵션에 `--module-path` 설정 필요

#### 게임 감지 실패
```
게임 프로세스를 찾을 수 없습니다
```
**해결책**:
- 지원되는 게임인지 확인
- 게임이 완전히 로드된 후 클라이언트 실행
- 관리자 권한으로 실행 시도

#### 백엔드 서버 연결 실패
```
Connection refused to localhost:8080
```
**해결책**:
- 백엔드 서버가 실행 중인지 확인
- 포트 8080이 사용 가능한지 확인
- 방화벽 설정 확인

#### 화면 캡쳐 실패
```
스크린샷 캡쳐에 실패했습니다
```
**해결책**:
- 게임이 전체화면 모드가 아닌 창 모드인지 확인
- Windows 디스플레이 배율 확인 (100% 권장)
- 보안 소프트웨어의 화면 캡쳐 차단 해제

### 성능 최적화

#### 메모리 사용량 최적화
```bash
# JVM 힙 메모리 조정
./gradlew run -Dorg.gradle.jvmargs="-Xmx1g -Xms512m"
```

#### 애니메이션 성능
- 낮은 사양에서는 애니메이션 프레임율 조정
- 물리 시뮬레이션 정확도 vs 성능 트레이드오프

## 📦 배포

### 실행 파일 생성
```bash
# 모든 의존성을 포함한 JAR 파일 생성
./gradlew shadowJar

# 생성된 파일 위치
build/libs/gameadvisor-client-*-all.jar
```

### Windows 실행 파일 생성 (jpackage)
```bash
# Java 17의 jpackage 사용 (예정)
jpackage --input build/libs \
  --main-jar gameadvisor-client-*-all.jar \
  --main-class com.gameadvisor.client.ui.GameAdvisorClient \
  --name "GameAdvisor" \
  --app-version "1.0" \
  --type msi
```

## 🔄 개발 가이드

### 코딩 컨벤션
- **Google Java Style Guide** 준수
- **JavaFX Property Binding** 적극 활용
- **비동기 처리**: Platform.runLater() 사용
- **리소스 관리**: try-with-resources 구문 사용

### 새로운 게임 지원 추가
1. `ProcessScanService`에 게임 실행 파일명 추가
2. 게임별 특화 조언 로직 구현
3. 필요시 게임별 UI 커스터마이징

### 새로운 애니메이션 추가
1. `AdvisorCharacter.AnimationState` enum에 상태 추가
2. `updateAnimation()` 메서드에 애니메이션 로직 구현
3. 상태 전환 조건 정의

---

**참고사항**:
- 현재 Windows만 지원 (JNA Windows API 사용)
- 게임은 창 모드에서 실행 권장
- 백엔드 서버가 먼저 실행되어야 함
- 화면 배율 100% 권장 (캡쳐 정확도)