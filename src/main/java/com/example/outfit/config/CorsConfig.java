package com.example.outfit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 설정
 * 프론트엔드와의 통신을 위한 Cross-Origin Resource Sharing 설정
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 자격 증명 허용 (쿠키, 인증 헤더 등)
        config.setAllowCredentials(true);

        // 허용할 Origin 설정
        // 개발 환경: 로컬호스트 허용
        // 프로덕션: 실제 프론트엔드 도메인으로 변경 필요
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // React 기본 포트
                "http://localhost:5173",  // Vite 기본 포트
                "http://localhost:8080",  // Vue CLI 기본 포트
                "http://localhost:4200",  // Angular 기본 포트
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:8080",
                "http://127.0.0.1:4200"
                // 프로덕션에서는 실제 프론트엔드 도메인 추가
                // 예: "https://your-frontend-domain.com"
        ));

        // 허용할 HTTP 메서드
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 허용할 HTTP 헤더
        config.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // 노출할 응답 헤더
        config.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        // Preflight 요청 캐시 시간 (초)
        config.setMaxAge(3600L);

        // 모든 경로에 CORS 설정 적용
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}

