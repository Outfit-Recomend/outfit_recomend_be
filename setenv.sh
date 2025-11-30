#!/bin/bash
# 환경 변수 설정 스크립트
# 사용법: source setenv.sh

export GEMINI_API_KEY="AIzaSyAFKz2ToKbRbRMplwfkFtMgOtlcOWZuI6g"
export GOOGLE_SEARCH_API_KEY="AIzaSyBlA-jzIXjRHQrEhxdcUB0lTbUmBTw34WM"
export GOOGLE_SEARCH_ENGINE_ID="f3a10922865bf445b"

echo "환경 변수가 설정되었습니다."
echo "GEMINI_API_KEY: ${GEMINI_API_KEY:0:20}..."
echo "GOOGLE_SEARCH_API_KEY: ${GOOGLE_SEARCH_API_KEY:0:20}..."
echo "GOOGLE_SEARCH_ENGINE_ID: ${GOOGLE_SEARCH_ENGINE_ID}"

