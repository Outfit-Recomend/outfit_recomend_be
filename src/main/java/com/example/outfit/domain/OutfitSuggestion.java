package com.example.outfit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 코디 제안 한 세트
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutfitSuggestion {
    
    // 코디 텍스트 설명
    private String description;
    
    // 생성된 코디 이미지 URL (Nano Banana에서 생성)
    private String outfitImageUrl;
    
    // 코디 프롬프트 (이미지 생성에 사용된)
    private String prompt;
    
    // 검색 쿼리 (어떤 검색어로 상품을 검색했는지)
    private String searchQuery;
    
    // 추천 상품 목록
    private java.util.List<ProductCandidate> products;
}


