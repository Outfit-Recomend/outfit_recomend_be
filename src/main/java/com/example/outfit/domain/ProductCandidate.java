package com.example.outfit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 추천 상품 정보 (Google Image Search 결과)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCandidate {
    
    // 상품 제목
    private String title;
    
    // 상품 이미지 URL
    private String imageUrl;
    
    // 상품 링크
    private String link;
    
    // 상품 설명/스니펫
    private String snippet;
    
    // 검색 쿼리 (어떤 검색어로 찾았는지)
    private String searchQuery;
}


