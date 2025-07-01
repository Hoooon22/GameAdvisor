# GameAdvisor

게임 화면 위에서 활동하는 AI 어드바이저 캐릭터가 실시간으로 도움을 제공하는 프로그램

## 🎮 주요 특징

- **캐릭터 기반 UI**: 귀여운 어드바이저 캐릭터가 게임 화면 하단에서 걸어다니며 도움 제공
- **완전 투명 오버레이**: 게임 플레이를 방해하지 않는 100% 투명한 오버레이 시스템
- **말풍선 상호작용**: 다양한 스타일의 말풍선으로 조언과 정보 제공
- **게임별 맞춤 조언**: 각 게임에 특화된 개인화된 조언 시스템
- **실시간 게임 감지**: Windows API를 활용한 자동 게임 감지 및 추적

## 프로젝트 구조

```
GameAdvisor/
├── backend/                # Spring Boot 백엔드 서버
│   ├── src/               # 소스 코드
│   ├── build.gradle       # Gradle 빌드 설정
│   └── README.md          # 백엔드 문서
│
└── client/                # JavaFX 클라이언트
    ├── src/
    │   └── main/java/com/gameadvisor/client/
    │       ├── ui/
    │       │   ├── GameAdvisorClient.java      # 메인 클라이언트
    │       │   └── components/
    │       │       ├── AdvisorCharacter.java   # 캐릭터 컴포넌트
    │       │       ├── SpeechBubble.java       # 말풍선 컴포넌트
    │       │       └── CharacterOverlay.java   # 오버레이 관리
    │       ├── model/                          # 데이터 모델
    │       ├── network/                        # API 통신
    │       ├── service/                        # 게임 감지 서비스
    │       └── util/                           # Windows API 유틸
    ├── build.gradle      # Gradle 빌드 설정
    └── README.md         # 클라이언트 문서
```

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.5.0
- Gradle
- Spring Data JPA
- MySQL 8.0

### Client
- Java 17
- JavaFX 17
- Gradle
- JNA (Java Native Access) - Windows API 연동

## 🚀 시작하기

### 요구사항
- JDK 17 이상
- Gradle 8.x
- MySQL 8.0 이상
- Windows 10/11 (현재 Windows만 지원)

### 실행 방법

1. **백엔드 서버 실행**
```bash
cd backend
./gradlew bootrun
```

2. **클라이언트 실행**
```bash
cd client
./gradlew run
```

**실행 순서**:
1. 먼저 백엔드 서버를 실행하여 API 서버를 구동
2. 백엔드가 완전히 시작된 후 클라이언트를 실행
3. 클라이언트는 게임을 자동으로 탐지하여 캐릭터를 표시

## 🎯 주요 기능

### 1. 캐릭터 기반 상호작용
- 게임 화면 하단에서 활동하는 귀여운 어드바이저 캐릭터
- 서있기, 걷기, 말하기, 생각하기 등 다양한 애니메이션
- 자동으로 좌우로 걸어다니며 생동감 있는 활동

### 2. 스마트 말풍선 시스템
- 일반 대화, 조언, 경고, 성공, 생각 등 5가지 스타일
- 캐릭터를 따라다니는 동적 위치 조정
- 페이드 인/아웃 애니메이션 효과

### 3. 게임별 맞춤 조언
- 각 게임에 특화된 개인화된 조언 시스템
- 이모지를 활용한 친근한 메시지
- 10초마다 자동으로 다양한 활동 수행

### 4. 투명 오버레이 기술
- 완전히 투명한 배경으로 게임 플레이 방해 없음
- 마우스 클릭 이벤트가 게임으로 통과
- 게임 창 위에 항상 표시되는 스마트 레이어링

### 5. 이중 창 시스템
- **상태창**: 게임 탐지 전 표시되는 작은 정보창
- **오버레이창**: 게임 탐지 시 전체 화면을 덮는 투명창
- 자동 전환으로 수동 조작 불필요

## 🔧 시스템 아키텍처

### 게임 감지 시스템
- Windows Process API를 통한 실시간 프로세스 감지
- 게임 창 위치 및 크기 실시간 추적
- 최소화/복원 상태 자동 감지

### 오버레이 렌더링
- JavaFX Stage의 TRANSPARENT 스타일 활용
- 마우스 이벤트 통과를 위한 setPickOnBounds(false) 설정
- 항상 위에 표시를 위한 setAlwaysOnTop(true) 적용

### 캐릭터 애니메이션
- Timeline 기반 부드러운 애니메이션 시스템
- 상태 기반 애니메이션 전환 (IDLE, WALKING, TALKING, THINKING)
- 게임 창 경계 내에서만 활동하는 제한된 이동

## 📊 개발 진행상황

### ✅ 완료된 기능
- **백엔드**: Spring Boot 기반 게임 정보 API 서버
- **게임 감지**: Windows API를 활용한 실시간 게임 프로세스 감지
- **캐릭터 시스템**: 완전한 캐릭터 기반 UI 구현
- **투명 오버레이**: 100% 투명한 게임 위 오버레이 시스템
- **말풍선 시스템**: 5가지 스타일의 동적 말풍선
- **애니메이션**: 부드러운 캐릭터 움직임 및 상태 전환
- **이중 창 관리**: 상태창과 오버레이창의 스마트 전환

### 🚧 개발 중인 기능
- 더 많은 게임 지원 추가
- 캐릭터 커스터마이징 옵션
- 음성 안내 기능
- 게임별 고급 분석 기능

### 📅 최근 업데이트 (2025-01-07)

#### 🎨 UI 대전환: 캐릭터 기반 시스템
- 기존 정적 오버레이에서 **살아있는 캐릭터 기반 UI**로 완전 전환
- 게임 화면 하단에서 걸어다니는 귀여운 어드바이저 캐릭터 구현
- 다양한 애니메이션 상태 (서있기, 걷기, 말하기, 생각하기) 지원

#### 🔍 완전 투명 오버레이 기술
- 하얀 화면 문제 완전 해결
- 캐릭터와 말풍선만 표시되고 나머지는 100% 투명
- 마우스 이벤트가 게임으로 완전히 통과하여 게임 플레이 방해 없음

#### 💬 스마트 상호작용 시스템
- 5가지 스타일의 말풍선 (일반, 조언, 경고, 성공, 생각)
- 게임별 맞춤 조언 메시지 (이모지 포함)
- 10초마다 자동 활동으로 지속적인 상호작용

#### 🏗️ 이중 창 아키텍처
- 게임 탐지 전: 작은 상태창으로 현재 상태 표시
- 게임 탐지 후: 전체 화면 투명 오버레이로 자동 전환
- 게임 종료 시 다시 상태창으로 복귀하는 스마트 전환

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'feat: Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 Git 커밋 메시지 규칙

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅, 세미콜론 누락 등
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드 추가
- `chore`: 빌드 업무 수정, 패키지 매니저 수정

## 📄 라이선스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## 🙏 개발 가이드

자세한 개발 가이드는 각 모듈의 README.md를 참고해주세요:
- [Backend 개발 가이드](backend/README.md)
- [Client 개발 가이드](client/README.md)