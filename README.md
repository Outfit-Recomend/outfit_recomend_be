# Outfit Recommendation System

이미지 기반 코디 추천 시스템

## 환경 변수 설정

### 방법 1: Spring Boot 프로파일 사용 (로컬 개발)

```bash
# application-local.yml 파일이 이미 생성되어 있습니다
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

또는 IDE에서:
- Run Configuration → Active profiles에 `local` 추가

### 방법 2: 시스템 환경 변수 설정

#### macOS/Linux:
```bash
# setenv.sh 스크립트 사용
source setenv.sh

# 또는 수동으로
export GEMINI_API_KEY="your-api-key"
export GOOGLE_SEARCH_API_KEY="your-api-key"
export GOOGLE_SEARCH_ENGINE_ID="your-engine-id"
```

#### Windows:
```cmd
# setenv.bat 스크립트 사용
setenv.bat

# 또는 수동으로
set GEMINI_API_KEY=your-api-key
set GOOGLE_SEARCH_API_KEY=your-api-key
set GOOGLE_SEARCH_ENGINE_ID=your-engine-id
```

### 방법 3: IDE에서 환경 변수 설정

#### IntelliJ IDEA:
1. Run → Edit Configurations
2. Environment variables에 추가:
   - `GEMINI_API_KEY=your-api-key`
   - `GOOGLE_SEARCH_API_KEY=your-api-key`
   - `GOOGLE_SEARCH_ENGINE_ID=your-engine-id`

#### VS Code:
`.vscode/launch.json`에 환경 변수 추가:
```json
{
  "env": {
    "GEMINI_API_KEY": "your-api-key",
    "GOOGLE_SEARCH_API_KEY": "your-api-key",
    "GOOGLE_SEARCH_ENGINE_ID": "your-engine-id"
  }
}
```

## 실행

### Maven 사용:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Gradle 사용:
```bash
# Gradle Wrapper 사용 (권장)
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 스크립트 사용
./run-gradle.sh
```

## API 문서 (Swagger)

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

Swagger UI에서 API를 직접 테스트할 수 있습니다.

## 프론트엔드 연동

프론트엔드 개발자를 위한 상세한 연동 가이드는 [FRONTEND_INTEGRATION.md](./FRONTEND_INTEGRATION.md)를 참고하세요.

주요 내용:
- React, Vue, Vanilla JavaScript 예제 코드
- 이미지 업로드 및 처리 흐름
- API 응답 구조
- CORS 설정

## API 사용

### 코디 추천
```bash
curl -X POST http://localhost:8080/api/outfit/recommend \
  -F "image=@/path/to/image.jpg"
```

### 제품 추천 (이미지 + 상품 목록)
```bash
curl -X POST http://localhost:8080/api/outfit/products \
  -F "image=@/path/to/image.jpg"
```

### Health Check
```bash
curl http://localhost:8080/api/outfit/health
```

## 필요한 API 키

1. **GEMINI_API_KEY**: Google Gemini API 키
   - Vision API와 Image Generation API에 사용

2. **GOOGLE_SEARCH_API_KEY**: Google Custom Search API 키
   - 상품 검색에 사용

3. **GOOGLE_SEARCH_ENGINE_ID**: Google Custom Search Engine ID
   - 상품 검색에 사용

## 배포 시 주의사항

### 1. CORS 설정
프로덕션 배포 시 `src/main/java/com/example/outfit/config/CorsConfig.java`에서 실제 프론트엔드 도메인을 추가해야 합니다:

```java
config.setAllowedOrigins(Arrays.asList(
    "https://your-frontend-domain.com"  // 실제 프론트엔드 도메인
));
```

### 2. 환경 변수 설정
배포 환경에서 다음 환경 변수를 설정해야 합니다:
- `GEMINI_API_KEY`
- `GOOGLE_SEARCH_API_KEY`
- `GOOGLE_SEARCH_ENGINE_ID`

### 3. 파일 크기 제한
현재 최대 20MB까지 업로드 가능합니다. 필요에 따라 `application.yml`에서 조정 가능합니다.

### 4. 타임아웃 설정
이미지 처리에 시간이 걸릴 수 있으므로 프론트엔드에서 충분한 타임아웃을 설정하세요 (권장: 60초 이상).

