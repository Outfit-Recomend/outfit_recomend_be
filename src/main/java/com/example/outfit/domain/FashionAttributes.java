package com.example.outfit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 이미지에서 추출된 패션 속성
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FashionAttributes {
    
    // 색상 정보
    private List<String> colors;
    
    // 스타일 (캐주얼, 포멀, 스포츠 등)
    private String style;
    
    // 의류 종류 (상의, 하의, 아우터 등)
    private String clothingType;
    
    // 패턴 (스트라이프, 체크, 플레인 등)
    private String pattern;
    
    // 계절성 (봄, 여름, 가을, 겨울)
    private String season;
    
    // 재질 (면, 폴리에스터, 니트 등)
    private String material;
    
    // 추가 속성
    private List<String> additionalAttributes;
}


