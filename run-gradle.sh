#!/bin/bash
# Gradle로 Spring Boot 애플리케이션 실행

echo "🚀 Gradle로 애플리케이션 실행 중..."
echo ""

./gradlew bootRun --args='--spring.profiles.active=local'

