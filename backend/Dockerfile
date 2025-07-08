# OpenJDK 17 기반 이미지 사용
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper와 빌드 파일들을 먼저 복사 (캐싱 최적화)
COPY gradlew ./
COPY gradle/ gradle/
COPY build.gradle ./
COPY settings.gradle ./

# 권한 설정
RUN chmod +x gradlew

# 의존성 다운로드 (빌드 캐싱을 위해 먼저 실행)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src/ src/

# 애플리케이션 빌드
RUN ./gradlew build --no-daemon -x test

# 8080 포트 노출
EXPOSE 8080

# 애플리케이션 실행
CMD ["java", "-jar", "build/libs/gameadvisor-0.0.1-SNAPSHOT.jar"] 