# GameAdvisor Backend

게임 어드바이저의 백엔드 서버 프로젝트입니다.

## 기술 스택

- Java 17
- Spring Boot 3.5.0
- Spring Data JPA
- PostgreSQL
- Gradle

## 개발 환경 설정

1. JDK 17 설치
2. PostgreSQL 설치 및 데이터베이스 생성
3. application.yml 설정

### 데이터베이스 설정

```sql
CREATE DATABASE gameadvisor;
```

### 애플리케이션 설정

`src/main/resources/application.yml` 파일을 생성하고 다음 내용을 추가:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/gameadvisor
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

## API 문서

API 문서는 Swagger UI를 통해 확인할 수 있습니다:
- 개발 환경: http://localhost:8080/swagger-ui.html

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "com.gameadvisor.backend.YourTest"
```