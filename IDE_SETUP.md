# IDE 설정 가이드

## IntelliJ IDEA 설정

### 방법 1: Active Profiles 설정 (권장)

1. Run → Edit Configurations...
2. OutfitApplication 선택 (또는 새로 생성)
3. **Active profiles** 필드에 `local` 입력
4. Apply → OK
5. 실행

### 방법 2: VM Options 설정

1. Run → Edit Configurations...
2. OutfitApplication 선택
3. **VM options** 필드에 다음 추가:
   ```
   -Dspring.profiles.active=local
   ```
4. Apply → OK
5. 실행

### 방법 3: Environment Variables 설정

1. Run → Edit Configurations...
2. OutfitApplication 선택
3. **Environment variables** 섹션에 추가:
   ```
   GEMINI_API_KEY=AIzaSyAFKz2ToKbRbRMplwfkFtMgOtlcOWZuI6g
   GOOGLE_SEARCH_API_KEY=AIzaSyBlA-jzIXjRHQrEhxdcUB0lTbUmBTw34WM
   GOOGLE_SEARCH_ENGINE_ID=f3a10922865bf445b
   ```
4. Apply → OK
5. 실행

## VS Code 설정

`.vscode/launch.json` 파일 생성:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "OutfitApplication",
      "request": "launch",
      "mainClass": "com.example.outfit.OutfitApplication",
      "projectName": "outfit",
      "args": "--spring.profiles.active=local",
      "vmArgs": "",
      "env": {
        "GEMINI_API_KEY": "AIzaSyAFKz2ToKbRbRMplwfkFtMgOtlcOWZuI6g",
        "GOOGLE_SEARCH_API_KEY": "AIzaSyBlA-jzIXjRHQrEhxdcUB0lTbUmBTw34WM",
        "GOOGLE_SEARCH_ENGINE_ID": "f3a10922865bf445b"
      }
    }
  ]
}
```

