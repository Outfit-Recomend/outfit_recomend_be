package com.example.outfit.domain;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "코디 추천 결과")
public class OutfitSuggestion {
    
    @Schema(description = "코디 텍스트 설명", example = "캐주얼한 스타일의 검정 청바지와 흰색 티셔츠 조합")
    private String description;
    
    @Schema(description = "생성된 코디 이미지 URL (Base64 데이터 URL 형식)", 
            example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    private String outfitImageUrl;
    
    @Schema(description = "코디 프롬프트 (이미지 생성에 사용된 프롬프트)", 
            example = "A stylish and well-coordinated outfit: casual style, black and white color combination...")
    private String prompt;
    
    @Schema(description = "검색 쿼리 (어떤 검색어로 상품을 검색했는지)", 
            example = "진청 데님 팬츠")
    private String searchQuery;
    
    @Schema(description = "추천 상품 목록")
    private java.util.List<ProductCandidate> products;
}


