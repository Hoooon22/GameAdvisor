# GameAdvisor 도커 실행 가이드

## 📋 개요

GameAdvisor 프로젝트를 도커로 실행하는 방법을 설명합니다.

## 🔧 사전 준비사항

- Docker Desktop 설치 및 실행
- Docker Compose 설치
- Windows PowerShell

## 🚀 실행 방법

### 1. 환경 변수 설정 (선택사항)

Gemini API를 사용하려면 `.env` 파일을 생성하세요:

```bash
# .env 파일 생성
echo "GEMINI_API_KEY=your_actual_api_key_here" > .env
```

### 2. 도커 서비스 빌드 및 실행

```bash
# 이미지 빌드
docker-compose build

# 서비스 시작 (백그라운드)
docker-compose up -d

# 서비스 상태 확인
docker-compose ps
```

### 3. 서비스 접속

- **백엔드 API**: http://localhost:8080
- **MySQL 데이터베이스**: localhost:3307
- **헬스체크**: http://localhost:8080/actuator/health

### 4. 로그 확인

```bash
# 전체 로그 확인
docker-compose logs

# 백엔드 로그만 확인
docker-compose logs backend

# 실시간 로그 확인
docker-compose logs -f backend
```

## 📦 서비스 구성

### MySQL 컨테이너
- **이미지**: mysql:8.0
- **포트**: 3307:3306
- **데이터베이스**: gameadvisor_db
- **사용자**: gameadvisor / gameadvisor123
- **Root 비밀번호**: rootpassword

### 백엔드 컨테이너
- **베이스 이미지**: openjdk:17-jdk-slim
- **포트**: 8080:8080
- **프로필**: docker
- **의존성**: MySQL 컨테이너

## 🛠️ 관리 명령어

### 서비스 중지
```bash
docker-compose down
```

### 볼륨 포함 완전 삭제
```bash
docker-compose down -v
```

### 이미지 재빌드
```bash
docker-compose build --no-cache
```

### MySQL 직접 접속
```bash
docker exec -it gameadvisor-mysql mysql -u gameadvisor -pgameadvisor123 gameadvisor_db
```

## 🔍 문제 해결

### 1. 백엔드가 시작되지 않는 경우
```bash
# 백엔드 로그 확인
docker logs gameadvisor-backend

# MySQL 연결 상태 확인
docker logs gameadvisor-mysql
```

### 2. 포트 충돌 시
기존에 8080이나 3307 포트를 사용하는 서비스가 있다면 docker-compose.yml에서 포트를 변경하세요.

### 3. 데이터베이스 초기화
```bash
# MySQL 볼륨 삭제 후 재시작
docker-compose down -v
docker-compose up -d
```

## 📚 추가 정보

- 백엔드 코드 변경 시 `docker-compose build backend` 후 재시작
- 프로덕션 환경에서는 환경 변수와 비밀번호를 안전하게 관리하세요
- 데이터는 Docker 볼륨에 저장되어 컨테이너 삭제 시에도 유지됩니다

## 🔄 기존 로컬 실행으로 돌아가기

도커 사용을 중단하고 기존 방식으로 돌아가려면:

1. 도커 서비스 중지: `docker-compose down`
2. 로컬 MySQL 서비스 시작
3. 백엔드 실행: `cd backend && ./gradlew bootrun` 