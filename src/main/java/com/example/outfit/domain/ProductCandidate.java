package com.example.outfit.domain;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "추천 상품 정보")
public class ProductCandidate {
    
    @Schema(description = "상품 제목", example = "모던아론 체이스 본딩 기모 데님 팬츠 그레이시진청")
    private String title;
    
    @Schema(description = "상품 이미지 URL", 
            example = "https://image.msscdn.net/thumbnails/images/goods_img/20251104/5683046/5683046_17622420524462_big.jpg")
    private String imageUrl;
    
    @Schema(description = "상품 링크 (무신사 상품 페이지)", 
            example = "https://www.musinsa.com/products/5683046")
    private String link;
    
    @Schema(description = "상품 설명/스니펫", 
            example = "모던아론(FP142) 플라이트 헤비웨이트 집업후드 7종 검정...")
    private String snippet;
    
    @Schema(description = "검색 쿼리 (어떤 검색어로 찾았는지)", 
            example = "진청 데님 팬츠")
    private String searchQuery;
}


