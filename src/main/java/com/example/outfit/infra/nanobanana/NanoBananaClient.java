package com.example.outfit.infra.nanobanana;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Nano Banana (Gemini Image) API를 사용하여 코디 이미지 생성
 */
@Slf4j
@Component
public class NanoBananaClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${nanobanana.api.key}")
    private String apiKey;

    @Value("${nanobanana.api.endpoint}")
    private String endpoint;

    @Value("${nanobanana.api.model}")
    private String model;

    public NanoBananaClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        // 이미지 응답이 크기 때문에 버퍼 크기를 10MB로 증가
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        
        this.webClient = webClientBuilder
                .exchangeStrategies(strategies)
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * 프롬프트를 기반으로 코디 이미지 생성
     */
    public String generateImage(String prompt) {
        return generateImage(null, prompt);
    }

    /**
     * 원본 이미지와 프롬프트를 기반으로 코디 이미지 생성
     * 원본 이미지의 얼굴과 옷을 최대한 유지하면서 추천 옷을 입은 모습으로 생성
     * 
     * @param originalImageBytes 원본 이미지 바이트 (null이면 텍스트만 사용)
     * @param prompt 이미지 생성 프롬프트
     * @return 생성된 이미지의 Data URL
     */
    public String generateImage(byte[] originalImageBytes, String prompt) {
        try {
            // Gemini API 형식으로 요청 본문 생성
            java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
            java.util.List<Object> parts = new java.util.ArrayList<>();
            
            // 원본 이미지가 있으면 먼저 이미지 추가
            if (originalImageBytes != null && originalImageBytes.length > 0) {
                // 이미지 MIME 타입 감지 (간단하게 PNG로 가정, 필요시 확장)
                String mimeType = "image/png";
                if (originalImageBytes.length >= 2) {
                    // JPEG 시그니처 확인
                    if (originalImageBytes[0] == (byte)0xFF && originalImageBytes[1] == (byte)0xD8) {
                        mimeType = "image/jpeg";
                    }
                }
                
                // 이미지를 base64로 인코딩
                String base64Image = java.util.Base64.getEncoder().encodeToString(originalImageBytes);
                
                // 이미지 part 추가
                java.util.Map<String, Object> imagePart = new java.util.HashMap<>();
                java.util.Map<String, Object> inlineData = new java.util.HashMap<>();
                inlineData.put("mimeType", mimeType);
                inlineData.put("data", base64Image);
                imagePart.put("inlineData", inlineData);
                parts.add(imagePart);
                
                log.info("원본 이미지 포함: {} bytes, MIME 타입: {}", originalImageBytes.length, mimeType);
            }
            
            // 프롬프트에 원본 이미지 유지 지시사항 추가
            String enhancedPrompt = prompt;
            if (originalImageBytes != null && originalImageBytes.length > 0) {
                enhancedPrompt = "Based on the provided reference image, maintain the person's face, body shape, and existing clothing as much as possible. " +
                        "Only change the recommended clothing item while keeping everything else identical. " +
                        "The result should look like the same person wearing the new recommended item. " + prompt;
            }
            
            // 텍스트 part 추가
            java.util.Map<String, String> textPart = new java.util.HashMap<>();
            textPart.put("text", enhancedPrompt);
            parts.add(textPart);
            
            java.util.Map<String, Object> content = new java.util.HashMap<>();
            content.put("parts", parts);
            
            java.util.List<Object> contentsList = new java.util.ArrayList<>();
            contentsList.add(content);
            
            requestMap.put("contents", contentsList);

            String requestBody = objectMapper.writeValueAsString(requestMap);
            log.debug("Nano Banana API Request Body (처음 500자): {}", 
                    requestBody.length() > 500 ? requestBody.substring(0, 500) + "..." : requestBody);
            log.debug("Nano Banana API URL: {}", endpoint);

            String response = webClient.post()
                    .uri(endpoint)
                    .header("x-goog-api-key", apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Nano Banana API 응답 길이: {} bytes", response != null ? response.length() : 0);
            if (response != null && response.length() < 2000) {
                log.info("Nano Banana API Response (전체): {}", response);
            } else if (response != null) {
                log.info("Nano Banana API Response (처음 1000자): {}", response.substring(0, Math.min(1000, response.length())));
            }

            return parseImageUrl(response);

        } catch (Exception e) {
            log.error("Nano Banana API 호출 실패", e);
            throw new RuntimeException("코디 이미지 생성 실패: " + e.getMessage(), e);
        }
    }

    private String parseImageUrl(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            
            // 에러 확인
            if (root.has("error")) {
                JsonNode error = root.path("error");
                log.error("Nano Banana API 에러: {}", error.toPrettyString());
                throw new RuntimeException("Nano Banana API 에러: " + error.toPrettyString());
            }
            
            log.info("Nano Banana API 응답 JSON 구조 확인:");
            java.util.Iterator<String> fieldNames = root.fieldNames();
            java.util.List<String> fields = new java.util.ArrayList<>();
            while (fieldNames.hasNext()) {
                fields.add(fieldNames.next());
            }
            log.info("  - root 필드들: {}", fields.isEmpty() ? "없음" : String.join(", ", fields));
            
            // Gemini API 형식
            JsonNode candidates = root.path("candidates");
            log.info("  - candidates 노드 존재: {}, 타입: {}", !candidates.isMissingNode(), candidates.getNodeType());
            
            if (candidates.isArray() && candidates.size() > 0) {
                log.info("  - candidates 배열 크기: {}", candidates.size());
                JsonNode firstCandidate = candidates.get(0);
                
                java.util.Iterator<String> candidateFields = firstCandidate.fieldNames();
                java.util.List<String> candidateFieldList = new java.util.ArrayList<>();
                while (candidateFields.hasNext()) {
                    candidateFieldList.add(candidateFields.next());
                }
                log.info("  - candidate 필드들: {}", candidateFieldList.isEmpty() ? "없음" : String.join(", ", candidateFieldList));
                
                JsonNode content = firstCandidate.path("content");
                log.info("  - content 노드 존재: {}", !content.isMissingNode());
                
                JsonNode parts = content.path("parts");
                log.info("  - parts 노드 존재: {}, 타입: {}, 배열 크기: {}", 
                        !parts.isMissingNode(), parts.getNodeType(), 
                        parts.isArray() ? parts.size() : "배열 아님");
                
                if (parts.isArray() && parts.size() > 0) {
                    // 모든 parts를 순회하면서 inlineData 찾기
                    for (int i = 0; i < parts.size(); i++) {
                        JsonNode part = parts.get(i);
                        java.util.Iterator<String> partFieldNames = part.fieldNames();
                        java.util.List<String> partFields = new java.util.ArrayList<>();
                        while (partFieldNames.hasNext()) {
                            partFields.add(partFieldNames.next());
                        }
                        log.debug("  - part {} 필드들: {}", i, partFields.isEmpty() ? "없음" : String.join(", ", partFields));
                        
                        // inlineData 형식 확인
                        JsonNode inlineData = part.path("inlineData");
                        if (!inlineData.isMissingNode()) {
                            log.info("  - part {}에 inlineData 발견", i);
                            String mimeType = inlineData.path("mimeType").asText("image/png");
                            String data = inlineData.path("data").asText();
                            if (data != null && !data.isEmpty()) {
                                log.info("  - 이미지 데이터 크기: {} bytes", data.length());
                                return "data:" + mimeType + ";base64," + data;
                            } else {
                                log.warn("  - inlineData.data가 비어있음");
                            }
                        }
                        
                        // inline_data 형식도 확인 (snake_case)
                        JsonNode inline_data = part.path("inline_data");
                        if (!inline_data.isMissingNode()) {
                            log.info("  - part {}에 inline_data 발견", i);
                            String mimeType = inline_data.path("mime_type").asText("image/png");
                            String data = inline_data.path("data").asText();
                            if (data != null && !data.isEmpty()) {
                                log.info("  - 이미지 데이터 크기: {} bytes", data.length());
                                return "data:" + mimeType + ";base64," + data;
                            } else {
                                log.warn("  - inline_data.data가 비어있음");
                            }
                        }
                    }
                }
            }

            log.error("이미지 데이터를 찾을 수 없습니다.");
            log.error("응답 전체 구조 (처음 2000자): {}", root.toPrettyString().substring(0, Math.min(2000, root.toPrettyString().length())));
            throw new RuntimeException("이미지 데이터를 찾을 수 없습니다");

        } catch (Exception e) {
            log.error("응답 파싱 실패", e);
            throw new RuntimeException("이미지 URL 파싱 실패: " + e.getMessage(), e);
        }
    }
}

