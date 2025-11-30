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

## API 사용

### 코디 추천
```bash
curl -X POST http://localhost:8080/api/outfit/recommend \
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

