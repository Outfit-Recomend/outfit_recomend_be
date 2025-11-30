#!/bin/bash
# 이미지 파일로 코디 추천 API 테스트

echo "=== 코디 추천 API 테스트 ==="
echo ""

# 이미지 파일 경로 확인
if [ -z "$1" ]; then
    echo "사용법: ./test-with-image.sh <이미지파일경로>"
    echo ""
    echo "예시:"
    echo "  ./test-with-image.sh ~/Desktop/test.jpg"
    echo "  ./test-with-image.sh ./test-image.png"
    exit 1
fi

IMAGE_PATH="$1"

if [ ! -f "$IMAGE_PATH" ]; then
    echo "❌ 오류: 이미지 파일을 찾을 수 없습니다: $IMAGE_PATH"
    exit 1
fi

echo "📸 이미지 파일: $IMAGE_PATH"
echo "📊 파일 크기: $(ls -lh "$IMAGE_PATH" | awk '{print $5}')"
echo ""

# Health check
echo "🔍 애플리케이션 상태 확인 중..."
HEALTH=$(curl -s http://localhost:8080/api/outfit/health 2>&1)

if [ "$HEALTH" != "OK" ]; then
    echo "❌ 애플리케이션이 실행 중이지 않습니다."
    echo "   다음 명령어로 애플리케이션을 시작하세요:"
    echo "   mvn spring-boot:run -Dspring-boot.run.profiles=local"
    exit 1
fi

echo "✅ 애플리케이션 실행 중"
echo ""

# API 호출
echo "🚀 코디 추천 요청 전송 중..."
echo ""

RESPONSE=$(curl -s -X POST http://localhost:8080/api/outfit/recommend \
  -F "image=@$IMAGE_PATH" \
  -H "Accept: application/json" \
  -w "\nHTTP_STATUS:%{http_code}")

HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS/d')

echo "📥 응답 상태: $HTTP_STATUS"
echo ""

if [ "$HTTP_STATUS" = "200" ]; then
    echo "✅ 성공! 코디 추천 결과:"
    echo ""
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
    
    # HTML 파일로 저장
    echo ""
    echo "💾 HTML 파일로 저장 중..."
    echo "$BODY" | python3 save-to-html.py 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "✅ generated-outfit.html 파일이 업데이트되었습니다."
        echo "   브라우저에서 열기: open generated-outfit.html"
    else
        echo "⚠️  HTML 파일 저장 실패 (JSON 파싱 오류일 수 있음)"
    fi
else
    echo "❌ 오류 발생:"
    echo "$BODY"
fi

echo ""
echo "=== 테스트 완료 ==="

