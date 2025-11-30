package com.example.outfit.application;

import com.example.outfit.domain.FashionAttributes;
import com.example.outfit.domain.OutfitSuggestion;
import com.example.outfit.domain.ProductCandidate;
import com.example.outfit.infra.google.GoogleImageSearchClient;
import com.example.outfit.infra.nanobanana.NanoBananaClient;
import com.example.outfit.infra.vision.VisionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 코디 추천 파이프라인 오케스트레이션
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutfitService {

    private final VisionClient visionClient;
    private final OutfitRuleEngine outfitRuleEngine;
    private final PromptGenerator promptGenerator;
    private final NanoBananaClient nanoBananaClient;
    private final GoogleImageSearchClient googleImageSearchClient;

    /**
     * 이미지 업로드 → 전체 파이프라인 실행
     */
    public OutfitSuggestion processOutfitRecommendation(byte[] imageBytes) {
        log.info("코디 추천 파이프라인 시작");

        // 1. 원본 이미지에서 AI에게 어울리는 옷 하나만 추천받기
        log.info("1단계: 원본 이미지에서 AI에게 어울리는 옷 하나만 추천받는 중...");
        String recommendedProduct = visionClient.extractRecommendedProductName(imageBytes);
        log.info("AI가 추천한 제품: '{}'", recommendedProduct);
        
        // 2. AI 추천 제품명 하나만으로 검색
        log.info("2단계: AI 추천 제품명으로 검색 중...");
        log.info("검색에 사용할 제품명: '{}'", recommendedProduct);
        List<ProductCandidate> products = googleImageSearchClient.searchProducts(recommendedProduct, 20);
        log.info("검색된 상품 수: {}", products.size());

        // 3. 원본 이미지 속성 추출 (코디 이미지 생성용)
        log.info("3단계: 원본 이미지 속성 추출 중...");
        FashionAttributes attributes = visionClient.extractAttributes(imageBytes);
        log.info("추출된 속성: {}", attributes);

        // 4. 원본 옷 + 추천 옷 합쳐진 코디 텍스트 생성
        log.info("4단계: 원본 옷 + 추천 옷 합쳐진 코디 텍스트 생성 중...");
        String outfitText = outfitRuleEngine.generateOutfitText(attributes);
        String combinedOutfitText = outfitText + " + " + recommendedProduct;
        log.info("생성된 코디 텍스트: {}", combinedOutfitText);

        // 5. 원본 옷 + 추천 옷 합쳐진 코디 이미지 생성 프롬프트 생성
        log.info("5단계: 코디 이미지 생성 프롬프트 생성 중...");
        String prompt = promptGenerator.translateToEnglishPrompt(combinedOutfitText);
        log.info("생성된 프롬프트: {}", prompt);

        // 6. Nano Banana로 원본 옷 + 추천 옷 합쳐진 코디 이미지 생성 (원본 이미지 포함)
        log.info("6단계: 원본 이미지를 참고하여 추천 옷을 입은 코디 이미지 생성 중...");
        String outfitImageUrl = nanoBananaClient.generateImage(imageBytes, prompt);
        log.info("생성된 코디 이미지 URL: {}", outfitImageUrl);

        // 결과 조합
        OutfitSuggestion suggestion = OutfitSuggestion.builder()
                .description(combinedOutfitText)
                .outfitImageUrl(outfitImageUrl)
                .prompt(prompt)
                .searchQuery(recommendedProduct)
                .products(products)
                .build();

        log.info("코디 추천 파이프라인 완료");
        return suggestion;
    }


    /**
     * Data URL에서 이미지 바이트 추출
     */
    private byte[] extractImageBytesFromDataUrl(String dataUrl) {
        try {
            // data:image/png;base64,xxxxx 형식에서 base64 부분만 추출
            String base64Data = dataUrl.substring(dataUrl.indexOf(",") + 1);
            return java.util.Base64.getDecoder().decode(base64Data);
        } catch (Exception e) {
            log.error("Data URL에서 이미지 바이트 추출 실패", e);
            throw new RuntimeException("이미지 바이트 추출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지 업로드 → 제품 목록 추천 (이미지 생성 포함)
     */
    public OutfitSuggestion processProductRecommendation(byte[] imageBytes) {
        log.info("제품 추천 파이프라인 시작 (이미지 생성 포함)");

        // 1. Vision API로 속성 추출
        log.info("1단계: 이미지 속성 추출 중...");
        FashionAttributes attributes = visionClient.extractAttributes(imageBytes);
        log.info("추출된 속성: {}", attributes);

        // 2. 속성 → 코디 텍스트 생성
        log.info("2단계: 코디 텍스트 생성 중...");
        String outfitText = outfitRuleEngine.generateOutfitText(attributes);
        log.info("생성된 코디 텍스트: {}", outfitText);

        // 3. 코디 텍스트 → 프롬프트 생성
        log.info("3단계: 이미지 생성 프롬프트 생성 중...");
        String prompt = promptGenerator.translateToEnglishPrompt(outfitText);
        log.info("생성된 프롬프트: {}", prompt);

        // 4. Nano Banana로 코디 이미지 생성
        log.info("4단계: 코디 이미지 생성 중...");
        String outfitImageUrl = nanoBananaClient.generateImage(prompt);
        log.info("생성된 코디 이미지 URL: {}", outfitImageUrl);

        // 5. 생성된 코디 이미지에서 AI에게 옷 하나만 추천받기
        log.info("5단계: 생성된 코디 이미지에서 AI에게 옷 하나만 추천받는 중...");
        byte[] generatedImageBytes = extractImageBytesFromDataUrl(outfitImageUrl);
        String recommendedProduct = visionClient.extractRecommendedProductName(generatedImageBytes);
        log.info("AI가 추천한 제품: {}", recommendedProduct);
        
        // 추천 제품 하나만 검색
        List<ProductCandidate> products = googleImageSearchClient.searchProducts(recommendedProduct, 20);
        log.info("검색된 상품 수: {}", products.size());

        // 결과 조합
        OutfitSuggestion suggestion = OutfitSuggestion.builder()
                .description(outfitText)
                .outfitImageUrl(outfitImageUrl)
                .prompt(prompt)
                .searchQuery(recommendedProduct)
                .products(products)
                .build();

        log.info("제품 추천 파이프라인 완료");
        return suggestion;
    }

}


