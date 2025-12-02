package com.example.outfit.infra.vision;

import com.example.outfit.domain.FashionAttributes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Gemini Vision API를 사용하여 이미지에서 패션 속성 추출
 */
@Slf4j
@Component
public class VisionClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${google.gemini.api-key}")
    private String apiKey;

    @Value("${google.gemini.vision.endpoint}")
    private String visionEndpoint;

    public VisionClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * 속성 기반 추천 제품명 추출
     * 패션 속성을 분석하여 어울리는 옷을 추천
     */
    public String extractRecommendedProductName(byte[] imageBytes, FashionAttributes attributes) {
        try {
            // API 키 검증
            if (apiKey == null || apiKey.isEmpty()) {
                log.error("❌ Vision API 키가 설정되지 않았습니다!");
                throw new RuntimeException("Vision API 키가 설정되지 않았습니다. GEMINI_API_KEY 환경 변수를 확인하세요.");
            }
            
            log.info("✅ Vision API 키 확인 - API 키: {}...", 
                    apiKey.substring(0, Math.min(10, apiKey.length())));
            
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            // 속성 정보를 문자열로 변환
            String colorsStr = attributes.getColors() != null && !attributes.getColors().isEmpty() 
                ? String.join(", ", attributes.getColors()) : "미지정";
            String styleStr = attributes.getStyle() != null && !attributes.getStyle().isEmpty() 
                ? attributes.getStyle() : "미지정";
            String patternStr = attributes.getPattern() != null && !attributes.getPattern().isEmpty() 
                ? attributes.getPattern() : "미지정";
            String seasonStr = attributes.getSeason() != null && !attributes.getSeason().isEmpty() 
                ? attributes.getSeason() : "미지정";
            String materialStr = attributes.getMaterial() != null && !attributes.getMaterial().isEmpty() 
                ? attributes.getMaterial() : "미지정";
            String clothingTypeStr = attributes.getClothingType() != null && !attributes.getClothingType().isEmpty() 
                ? attributes.getClothingType() : "미지정";
            
            String prompt = String.format("""
                다음 패션 속성을 분석하여 어울리는 옷 하나를 추천해주세요:
                - 색상: %s
                - 스타일: %s
                - 의류 종류: %s
                - 패턴: %s
                - 계절: %s
                - 재질: %s
                
                이 속성들에 어울리는 다른 옷 하나만 구체적인 제품명으로 추천해주세요.
                예시: "캐주얼 브라운 재킷", "검정 슬랙스", "베이지 가디건", "화이트 셔츠" 등
                
                추천 제품명만 응답해주세요 (설명 없이 제품명만).
                """, colorsStr, styleStr, clothingTypeStr, patternStr, seasonStr, materialStr);

            String mimeType = "image/png";
            
            java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
            java.util.List<Object> parts = new java.util.ArrayList<>();
            
            java.util.Map<String, String> textPart = new java.util.HashMap<>();
            textPart.put("text", prompt);
            parts.add(textPart);
            
            java.util.Map<String, Object> inlineData = new java.util.HashMap<>();
            inlineData.put("mime_type", mimeType);
            inlineData.put("data", base64Image);
            
            java.util.Map<String, Object> imagePart = new java.util.HashMap<>();
            imagePart.put("inline_data", inlineData);
            parts.add(imagePart);
            
            java.util.Map<String, Object> content = new java.util.HashMap<>();
            content.put("parts", parts);
            
            java.util.List<Object> contentsList = new java.util.ArrayList<>();
            contentsList.add(content);
            
            requestMap.put("contents", contentsList);

            String requestBody = objectMapper.writeValueAsString(requestMap);
            log.debug("Vision API Request (속성 기반 추천 제품명): {}", requestBody);
            log.info("속성 기반 추천 - 색상: {}, 스타일: {}, 의류 종류: {}", colorsStr, styleStr, clothingTypeStr);

            try {
                String response = webClient.post()
                        .uri(visionEndpoint)
                        .header("x-goog-api-key", apiKey)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.debug("Vision API Response (속성 기반 추천 제품명): {}", response);

                return parseRecommendedProductName(response);

            } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
                String errorBody = e.getResponseBodyAsString();
                log.error("Vision API 호출 실패 - Status: {}, Body: {}", e.getStatusCode(), errorBody);
                throw new RuntimeException("속성 기반 추천 제품명 추출 실패: " + e.getStatusCode() + " - " + errorBody, e);
            }

        } catch (Exception e) {
            log.error("Vision API 호출 실패", e);
            throw new RuntimeException("속성 기반 추천 제품명 추출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 생성된 코디 이미지에서 추천 제품명 하나만 추출
     * AI에게 "이 이미지에 어울리는 옷 하나만 추천해줘"라고 요청
     */
    public String extractRecommendedProductName(byte[] imageBytes) {
        try {
            // API 키 검증
            if (apiKey == null || apiKey.isEmpty()) {
                log.error("❌ Vision API 키가 설정되지 않았습니다!");
                throw new RuntimeException("Vision API 키가 설정되지 않았습니다. GEMINI_API_KEY 환경 변수를 확인하세요.");
            }
            
            log.info("✅ Vision API 키 확인 - API 키: {}...", 
                    apiKey.substring(0, Math.min(10, apiKey.length())));
            
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            String prompt = """
                이 옷 이미지를 보고, 이 옷에 어울리는 다른 옷 하나만 추천해주세요.
                구체적인 제품명을 한글로 하나만 추천해주세요.
                예시: "캐주얼 브라운 재킷", "검정 슬랙스", "베이지 가디건", "화이트 셔츠" 등
                
                추천 제품명만 응답해주세요 (설명 없이 제품명만).
                """;

            String mimeType = "image/png";
            
            java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
            java.util.List<Object> parts = new java.util.ArrayList<>();
            
            java.util.Map<String, String> textPart = new java.util.HashMap<>();
            textPart.put("text", prompt);
            parts.add(textPart);
            
            java.util.Map<String, Object> inlineData = new java.util.HashMap<>();
            inlineData.put("mime_type", mimeType);
            inlineData.put("data", base64Image);
            
            java.util.Map<String, Object> imagePart = new java.util.HashMap<>();
            imagePart.put("inline_data", inlineData);
            parts.add(imagePart);
            
            java.util.Map<String, Object> content = new java.util.HashMap<>();
            content.put("parts", parts);
            
            java.util.List<Object> contentsList = new java.util.ArrayList<>();
            contentsList.add(content);
            
            requestMap.put("contents", contentsList);

            String requestBody = objectMapper.writeValueAsString(requestMap);
            log.debug("Vision API Request (추천 제품명): {}", requestBody);

            try {
                String response = webClient.post()
                        .uri(visionEndpoint)
                        .header("x-goog-api-key", apiKey)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.debug("Vision API Response (추천 제품명): {}", response);

                return parseRecommendedProductName(response);

            } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
                String errorBody = e.getResponseBodyAsString();
                log.error("Vision API 호출 실패 - Status: {}, Body: {}", e.getStatusCode(), errorBody);
                throw new RuntimeException("추천 제품명 추출 실패: " + e.getStatusCode() + " - " + errorBody, e);
            }

        } catch (Exception e) {
            log.error("Vision API 호출 실패", e);
            throw new RuntimeException("추천 제품명 추출 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * Vision API 응답에서 추천 제품명 파싱
     */
    private String parseRecommendedProductName(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isEmpty() || !candidates.has(0)) {
                throw new RuntimeException("Vision API 응답에 후보가 없습니다");
            }

            String content = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            
            // 응답에서 제품명만 추출 (설명 제거)
            String productName = content.trim();
            
            // 줄바꿈이나 설명이 있으면 첫 번째 줄만 사용
            if (productName.contains("\n")) {
                productName = productName.split("\n")[0].trim();
            }
            
            // 따옴표 제거
            productName = productName.replace("\"", "").replace("'", "");
            
            log.info("추출된 추천 제품명: {}", productName);
            return productName;

        } catch (Exception e) {
            log.error("추천 제품명 파싱 실패", e);
            throw new RuntimeException("추천 제품명 파싱 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지에서 패션 속성 추출
     */
    public FashionAttributes extractAttributes(byte[] imageBytes) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            String prompt = """
                이 이미지의 패션 아이템을 분석하여 다음 정보를 JSON 형식으로 추출해주세요:
                - colors: 주요 색상 리스트
                - style: 스타일 (캐주얼, 포멀, 스포츠, 스트릿 등)
                - clothingType: 의류 종류 (상의, 하의, 아우터, 원피스 등)
                - pattern: 패턴 (스트라이프, 체크, 플레인, 도트 등)
                - season: 계절성 (봄, 여름, 가을, 겨울)
                - material: 재질 (면, 폴리에스터, 니트, 데님 등)
                - additionalAttributes: 기타 특징 리스트
                
                JSON 형식으로만 응답해주세요.
                """;

            // 이미지 타입 자동 감지 (일반적으로 PNG 또는 JPEG)
            String mimeType = "image/png"; // 기본값
            
            // ObjectMapper를 사용하여 JSON 생성
            java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
            java.util.List<Object> parts = new java.util.ArrayList<>();
            
            java.util.Map<String, String> textPart = new java.util.HashMap<>();
            textPart.put("text", prompt);
            parts.add(textPart);
            
            java.util.Map<String, Object> inlineData = new java.util.HashMap<>();
            inlineData.put("mime_type", mimeType);
            inlineData.put("data", base64Image);
            
            java.util.Map<String, Object> imagePart = new java.util.HashMap<>();
            imagePart.put("inline_data", inlineData);
            parts.add(imagePart);
            
            java.util.Map<String, Object> content = new java.util.HashMap<>();
            content.put("parts", parts);
            
            java.util.List<Object> contentsList = new java.util.ArrayList<>();
            contentsList.add(content);
            
            requestMap.put("contents", contentsList);

            String requestBody = objectMapper.writeValueAsString(requestMap);
            log.debug("Vision API Request Body: {}", requestBody);
            log.debug("Vision API URL: {}", visionEndpoint);

            try {
                String response = webClient.post()
                        .uri(visionEndpoint)
                        .header("x-goog-api-key", apiKey)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.debug("Vision API Response: {}", response);

                return parseResponse(response);
            } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
                String errorBody = e.getResponseBodyAsString();
                log.error("Vision API 호출 실패 - Status: {}, Body: {}", e.getStatusCode(), errorBody);
                throw new RuntimeException("이미지 속성 추출 실패: " + e.getStatusCode() + " - " + errorBody, e);
            }

        } catch (Exception e) {
            log.error("Vision API 호출 실패", e);
            throw new RuntimeException("이미지 속성 추출 실패: " + e.getMessage(), e);
        }
    }

    private FashionAttributes parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isEmpty() || !candidates.has(0)) {
                throw new RuntimeException("Vision API 응답에 후보가 없습니다");
            }

            String content = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            
            // JSON 추출 (응답에서 JSON 부분만 파싱)
            String jsonContent = extractJsonFromText(content);
            JsonNode attributesJson = objectMapper.readTree(jsonContent);

            return FashionAttributes.builder()
                    .colors(parseStringList(attributesJson, "colors"))
                    .style(attributesJson.path("style").asText())
                    .clothingType(attributesJson.path("clothingType").asText())
                    .pattern(attributesJson.path("pattern").asText())
                    .season(attributesJson.path("season").asText())
                    .material(attributesJson.path("material").asText())
                    .additionalAttributes(parseStringList(attributesJson, "additionalAttributes"))
                    .build();

        } catch (Exception e) {
            log.error("응답 파싱 실패", e);
            // 기본값 반환
            return FashionAttributes.builder()
                    .colors(new ArrayList<>())
                    .style("캐주얼")
                    .clothingType("상의")
                    .pattern("플레인")
                    .season("사계절")
                    .material("면")
                    .additionalAttributes(new ArrayList<>())
                    .build();
        }
    }

    private String extractJsonFromText(String text) {
        // JSON 객체 찾기
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "{}";
    }

    private List<String> parseStringList(JsonNode node, String fieldName) {
        List<String> list = new ArrayList<>();
        JsonNode arrayNode = node.path(fieldName);
        if (arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                list.add(item.asText());
            }
        }
        return list;
    }
}

