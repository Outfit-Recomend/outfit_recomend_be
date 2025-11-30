#!/bin/bash
# Google Custom Search API 테스트 스크립트

API_KEY="${GOOGLE_SEARCH_API_KEY:-AIzaSyBlA-jzIXjRHQrEhxdcUB0lTbUmBTw34WM}"
ENGINE_ID="${GOOGLE_SEARCH_ENGINE_ID:-f3a10922865bf445b}"
QUERY="검정 청바지"

echo "=== Google Custom Search API 테스트 ==="
echo "API Key: ${API_KEY:0:20}..."
echo "Engine ID: $ENGINE_ID"
echo "Query: $QUERY"
echo ""

# URL 인코딩
ENCODED_QUERY=$(python3 -c "import urllib.parse; print(urllib.parse.quote('$QUERY'))")
URL="https://www.googleapis.com/customsearch/v1?key=${API_KEY}&cx=${ENGINE_ID}&q=${ENCODED_QUERY}&searchType=image&num=5"

echo "요청 URL: ${URL//${API_KEY}/***}"
echo ""

RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" "$URL")
HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS/d')

echo "HTTP 상태 코드: $HTTP_STATUS"
echo ""

if [ "$HTTP_STATUS" = "200" ]; then
    echo "✅ 성공! 응답:"
    echo "$BODY" | python3 -m json.tool 2>/dev/null | head -50
else
    echo "❌ 실패! 응답:"
    echo "$BODY" | head -20
fi

