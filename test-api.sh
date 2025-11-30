#!/bin/bash
# 코디 추천 API 테스트 스크립트

IMAGE_PATH="$1"

if [ -z "$IMAGE_PATH" ]; then
    echo "사용법: ./test-api.sh <이미지파일경로>"
    echo "예시: ./test-api.sh ~/Desktop/test-image.jpg"
    exit 1
fi

if [ ! -f "$IMAGE_PATH" ]; then
    echo "오류: 이미지 파일을 찾을 수 없습니다: $IMAGE_PATH"
    exit 1
fi

echo "이미지 업로드 중: $IMAGE_PATH"
echo ""

# API 호출
curl -X POST http://localhost:8080/api/outfit/recommend \
  -F "image=@$IMAGE_PATH" \
  -H "Accept: application/json" \
  | python3 -m json.tool 2>/dev/null || cat

echo ""
echo ""
echo "테스트 완료!"

