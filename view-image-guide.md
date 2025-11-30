# 생성된 이미지 확인 방법

## 방법 1: HTML 파일로 저장하여 확인

API 응답에서 `outfitImageUrl` 필드의 base64 데이터를 HTML 파일로 저장:

```bash
# 테스트 후 생성된 HTML 파일 열기
open generated-outfit.html
```

## 방법 2: 브라우저에서 직접 확인

1. API 응답의 `outfitImageUrl` 값을 복사
2. 브라우저 주소창에 입력하거나 HTML 파일에 포함

## 방법 3: Python 스크립트로 이미지 저장

```python
import json
import base64

# API 응답 JSON 파일 읽기
with open('response.json', 'r') as f:
    data = json.load(f)

# base64 이미지 데이터 추출
img_data = data['outfitImageUrl']
if img_data.startswith('data:image'):
    # data:image/png;base64, 부분 제거
    base64_data = img_data.split(',')[1]
    image_bytes = base64.b64decode(base64_data)
    
    # 이미지 파일로 저장
    with open('outfit-image.png', 'wb') as img_file:
        img_file.write(image_bytes)
    
    print("이미지가 outfit-image.png로 저장되었습니다.")
```

## 방법 4: 온라인 Base64 디코더 사용

1. `outfitImageUrl`의 base64 부분 복사 (`, 이후 부분`)
2. 온라인 Base64 to Image 변환기 사용
3. 이미지 다운로드

