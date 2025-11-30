# 멀티 스테이지 빌드
# Stage 1: 빌드 스테이지
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Gradle 캐시를 활용하기 위해 의존성 파일 먼저 복사
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

# 의존성 다운로드 (캐시 활용)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드
RUN gradle clean build -x test --no-daemon

# Stage 2: 실행 스테이지
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 환경 변수 설정 (기본값)
ENV SPRING_PROFILES_ACTIVE=default
ENV GEMINI_API_KEY=""
ENV GOOGLE_SEARCH_API_KEY=""
ENV GOOGLE_SEARCH_ENGINE_ID=""
ENV NANOBANANA_API_KEY=""

# 헬스체크 (curl 설치 필요)
RUN apk add --no-cache curl
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/outfit/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]

