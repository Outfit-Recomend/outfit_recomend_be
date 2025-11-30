package com.example.outfit.application;

import com.example.outfit.domain.FashionAttributes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 속성 → 코디 텍스트 변환 (룰 기반)
 */
@Slf4j
@Component
public class OutfitRuleEngine {

    /**
     * 영어 → 한글 변환 맵
     */
    private static final Map<String, String> ENGLISH_TO_KOREAN = new HashMap<>();
    
    static {
        // 스타일
        ENGLISH_TO_KOREAN.put("casual", "캐주얼");
        ENGLISH_TO_KOREAN.put("formal", "정장");
        ENGLISH_TO_KOREAN.put("sporty", "스포티");
        ENGLISH_TO_KOREAN.put("street", "스트릿");
        ENGLISH_TO_KOREAN.put("minimal", "미니멀");
        ENGLISH_TO_KOREAN.put("vintage", "빈티지");
        ENGLISH_TO_KOREAN.put("classic", "클래식");
        ENGLISH_TO_KOREAN.put("trendy", "트렌디");
        ENGLISH_TO_KOREAN.put("elegant", "엘레강트");
        ENGLISH_TO_KOREAN.put("bohemian", "보헤미안");
        
        // 색상
        ENGLISH_TO_KOREAN.put("black", "블랙");
        ENGLISH_TO_KOREAN.put("white", "화이트");
        ENGLISH_TO_KOREAN.put("gray", "그레이");
        ENGLISH_TO_KOREAN.put("grey", "그레이");
        ENGLISH_TO_KOREAN.put("navy", "네이비");
        ENGLISH_TO_KOREAN.put("beige", "베이지");
        ENGLISH_TO_KOREAN.put("brown", "브라운");
        ENGLISH_TO_KOREAN.put("khaki", "카키");
        ENGLISH_TO_KOREAN.put("olive", "올리브");
        ENGLISH_TO_KOREAN.put("burgundy", "버건디");
        ENGLISH_TO_KOREAN.put("maroon", "마룬");
        ENGLISH_TO_KOREAN.put("red", "레드");
        ENGLISH_TO_KOREAN.put("blue", "블루");
        ENGLISH_TO_KOREAN.put("green", "그린");
        ENGLISH_TO_KOREAN.put("yellow", "옐로우");
        ENGLISH_TO_KOREAN.put("pink", "핑크");
        ENGLISH_TO_KOREAN.put("purple", "퍼플");
        ENGLISH_TO_KOREAN.put("orange", "오렌지");
        
        // 의류 종류
        ENGLISH_TO_KOREAN.put("jacket", "재킷");
        ENGLISH_TO_KOREAN.put("coat", "코트");
        ENGLISH_TO_KOREAN.put("blazer", "블레이저");
        ENGLISH_TO_KOREAN.put("cardigan", "가디건");
        ENGLISH_TO_KOREAN.put("sweater", "스웨터");
        ENGLISH_TO_KOREAN.put("hoodie", "후드");
        ENGLISH_TO_KOREAN.put("shirt", "셔츠");
        ENGLISH_TO_KOREAN.put("t-shirt", "티셔츠");
        ENGLISH_TO_KOREAN.put("pants", "팬츠");
        ENGLISH_TO_KOREAN.put("jeans", "청바지");
        ENGLISH_TO_KOREAN.put("dress", "드레스");
        ENGLISH_TO_KOREAN.put("skirt", "스커트");
        ENGLISH_TO_KOREAN.put("shorts", "반바지");
        ENGLISH_TO_KOREAN.put("fleece", "플리스");
        ENGLISH_TO_KOREAN.put("fleece jacket", "플리스 재킷");
        
        // 패턴
        ENGLISH_TO_KOREAN.put("solid", "솔리드");
        ENGLISH_TO_KOREAN.put("striped", "스트라이프");
        ENGLISH_TO_KOREAN.put("checked", "체크");
        ENGLISH_TO_KOREAN.put("polka dot", "도트");
        ENGLISH_TO_KOREAN.put("floral", "플로럴");
        ENGLISH_TO_KOREAN.put("geometric", "지오메트릭");
    }
    
    /**
     * 영어를 한글로 변환 (없으면 원본 반환)
     */
    private String translateToKorean(String english) {
        if (english == null || english.isEmpty()) {
            return english;
        }
        String lower = english.toLowerCase().trim();
        return ENGLISH_TO_KOREAN.getOrDefault(lower, english);
    }

    /**
     * 패션 속성을 기반으로 코디 텍스트 생성 (한글)
     */
    public String generateOutfitText(FashionAttributes attributes) {
        StringJoiner outfitText = new StringJoiner(", ");

        // 스타일 기반 코디 (한글로 변환)
        if (attributes.getStyle() != null && !attributes.getStyle().isEmpty()) {
            String koreanStyle = translateToKorean(attributes.getStyle());
            outfitText.add(koreanStyle + " 스타일");
        }

        // 색상 조합 (한글로 변환)
        if (attributes.getColors() != null && !attributes.getColors().isEmpty()) {
            List<String> koreanColors = new ArrayList<>();
            for (String color : attributes.getColors()) {
                koreanColors.add(translateToKorean(color));
            }
            String colorText = String.join("와 ", koreanColors);
            outfitText.add(colorText + " 컬러 조합");
        }

        // 의류 종류 기반 (한글로 변환)
        if (attributes.getClothingType() != null && !attributes.getClothingType().isEmpty()) {
            String koreanClothingType = translateToKorean(attributes.getClothingType());
            outfitText.add(koreanClothingType + " 중심 코디");
        }

        // 패턴 추가 (한글로 변환)
        if (attributes.getPattern() != null && !attributes.getPattern().isEmpty() 
                && !attributes.getPattern().equals("플레인")) {
            String koreanPattern = translateToKorean(attributes.getPattern());
            outfitText.add(koreanPattern + " 패턴");
        }

        // 계절성 고려
        if (attributes.getSeason() != null && !attributes.getSeason().isEmpty()) {
            outfitText.add(attributes.getSeason() + "에 어울리는");
        }

        // 재질 정보 (한글로 변환)
        if (attributes.getMaterial() != null && !attributes.getMaterial().isEmpty()) {
            String koreanMaterial = translateToKorean(attributes.getMaterial());
            outfitText.add(koreanMaterial + " 소재");
        }

        // 추가 속성 (한글로 변환)
        if (attributes.getAdditionalAttributes() != null 
                && !attributes.getAdditionalAttributes().isEmpty()) {
            List<String> koreanAdditional = new ArrayList<>();
            for (String attr : attributes.getAdditionalAttributes()) {
                koreanAdditional.add(translateToKorean(attr));
            }
            String additionalText = String.join(", ", koreanAdditional);
            outfitText.add(additionalText);
        }

        String result = outfitText.toString();
        
        // 기본값 처리
        if (result.isEmpty()) {
            result = "심플하고 깔끔한 캐주얼 코디";
        }

        log.debug("생성된 코디 텍스트 (한글): {}", result);
        return result;
    }

    /**
     * 추천된 아이템으로 하나의 구체적인 제품명 생성 (한글만 사용)
     * 생성된 코디 이미지에서 추출한 속성으로 하나의 구체적인 제품만 검색
     */
    public String generateRecommendedProductName(FashionAttributes recommendedAttributes) {
        // 추천된 아이템의 의류 종류와 색상으로 하나의 구체적인 제품명 생성
        String koreanClothingType = recommendedAttributes.getClothingType() != null 
                ? translateToKorean(recommendedAttributes.getClothingType()) : null;
        List<String> koreanColors = new ArrayList<>();
        if (recommendedAttributes.getColors() != null && !recommendedAttributes.getColors().isEmpty()) {
            for (String color : recommendedAttributes.getColors()) {
                koreanColors.add(translateToKorean(color));
            }
        }
        String koreanStyle = recommendedAttributes.getStyle() != null 
                ? translateToKorean(recommendedAttributes.getStyle()) : null;
        
        // 하나의 구체적인 제품명 생성 (우선순위: 스타일 + 색상 + 의류 종류)
        StringBuilder productName = new StringBuilder();
        
        if (koreanStyle != null && !koreanStyle.isEmpty()) {
            productName.append(koreanStyle).append(" ");
        }
        
        if (!koreanColors.isEmpty()) {
            // 첫 번째 색상만 사용 (가장 주요한 색상)
            productName.append(koreanColors.get(0)).append(" ");
        }
        
        if (koreanClothingType != null && !koreanClothingType.isEmpty()) {
            productName.append(koreanClothingType);
        } else {
            // 의류 종류가 없으면 "옷" 추가
            productName.append("옷");
        }
        
        String result = productName.toString().trim();
        
        // 빈 문자열이면 기본값
        if (result.isEmpty()) {
            result = "패션 의류";
        }
        
        log.info("추천된 제품명 (하나): {}", result);
        return result;
    }
    
    /**
     * 추천된 아이템으로 검색 쿼리 생성 (한글만 사용)
     * @deprecated 하나의 제품명만 생성하는 generateRecommendedProductName 사용
     */
    @Deprecated
    public List<String> generateSearchQueriesFromRecommendedItems(FashionAttributes recommendedAttributes) {
        String productName = generateRecommendedProductName(recommendedAttributes);
        return List.of(productName);
    }

    /**
     * 원본 이미지에 어울리는 다른 아이템 검색 쿼리 생성 (한글만 사용)
     * @deprecated 생성된 코디 이미지에서 추천된 아이템으로 검색하는 방식으로 변경됨
     */
    @Deprecated
    public List<String> generateSearchQueries(String outfitText, FashionAttributes attributes) {
        List<String> queries = new ArrayList<>();
        
        String koreanStyle = attributes.getStyle() != null ? translateToKorean(attributes.getStyle()) : null;
        String koreanClothingType = attributes.getClothingType() != null ? translateToKorean(attributes.getClothingType()) : null;
        List<String> koreanColors = new ArrayList<>();
        if (attributes.getColors() != null && !attributes.getColors().isEmpty()) {
            for (String color : attributes.getColors()) {
                koreanColors.add(translateToKorean(color));
            }
        }
        
        // 원본 이미지의 의류 종류에 따라 어울리는 다른 아이템 검색
        if (koreanClothingType != null && !koreanClothingType.isEmpty()) {
            List<String> matchingItems = getMatchingItems(koreanClothingType);
            
            // 스타일과 색상을 유지하면서 다른 아이템 검색
            for (String item : matchingItems) {
                // 스타일 + 아이템
                if (koreanStyle != null && !koreanStyle.isEmpty()) {
                    queries.add(koreanStyle + " " + item);
                }
                
                // 색상 + 아이템
                if (!koreanColors.isEmpty()) {
                    String colorQuery = String.join(" ", koreanColors) + " " + item;
                    queries.add(colorQuery);
                }
                
                // 스타일 + 색상 + 아이템
                if (koreanStyle != null && !koreanStyle.isEmpty() && !koreanColors.isEmpty()) {
                    String fullQuery = koreanStyle + " " + String.join(" ", koreanColors) + " " + item;
                    queries.add(fullQuery);
                }
            }
        } else {
            // 의류 종류가 없으면 일반적인 코디 아이템 검색
            if (koreanStyle != null && !koreanStyle.isEmpty()) {
                queries.add(koreanStyle + " 코디 아이템");
            }
            if (!koreanColors.isEmpty()) {
                queries.add(String.join(" ", koreanColors) + " 코디 아이템");
            }
        }
        
        // 기본 검색 쿼리 (스타일 기반)
        if (koreanStyle != null && !koreanStyle.isEmpty()) {
            queries.add(koreanStyle + " 스타일 코디");
        }
        
        log.debug("생성된 검색 쿼리 (원본 이미지에 어울리는 아이템): {}", queries);
        return queries;
    }
    
    /**
     * 원본 이미지의 의류 종류에 어울리는 다른 아이템 목록 반환
     */
    private List<String> getMatchingItems(String clothingType) {
        List<String> items = new ArrayList<>();
        String lowerType = clothingType.toLowerCase();
        
        // 상의인 경우 → 하의, 신발, 액세서리
        if (lowerType.contains("상의") || lowerType.contains("셔츠") || lowerType.contains("티") || 
            lowerType.contains("재킷") || lowerType.contains("코트") || lowerType.contains("아우터") ||
            lowerType.contains("블레이저") || lowerType.contains("가디건") || lowerType.contains("스웨터") ||
            lowerType.contains("후드") || lowerType.contains("플리스")) {
            items.add("하의");
            items.add("바지");
            items.add("팬츠");
            items.add("청바지");
            items.add("스커트");
            items.add("신발");
            items.add("운동화");
            items.add("스니커즈");
            items.add("가방");
            items.add("액세서리");
        }
        // 하의인 경우 → 상의, 신발, 액세서리
        else if (lowerType.contains("하의") || lowerType.contains("바지") || lowerType.contains("팬츠") ||
                 lowerType.contains("청바지") || lowerType.contains("스커트") || lowerType.contains("반바지")) {
            items.add("상의");
            items.add("티셔츠");
            items.add("셔츠");
            items.add("블라우스");
            items.add("신발");
            items.add("운동화");
            items.add("스니커즈");
            items.add("가방");
            items.add("액세서리");
        }
        // 원피스인 경우 → 신발, 가방, 액세서리
        else if (lowerType.contains("원피스") || lowerType.contains("드레스")) {
            items.add("신발");
            items.add("운동화");
            items.add("스니커즈");
            items.add("가방");
            items.add("액세서리");
            items.add("자켓");
            items.add("카디건");
        }
        // 신발인 경우 → 상의, 하의, 가방, 액세서리
        else if (lowerType.contains("신발") || lowerType.contains("운동화") || lowerType.contains("스니커즈")) {
            items.add("상의");
            items.add("하의");
            items.add("바지");
            items.add("팬츠");
            items.add("가방");
            items.add("액세서리");
        }
        // 기본값: 코디 아이템
        else {
            items.add("상의");
            items.add("하의");
            items.add("신발");
            items.add("가방");
            items.add("액세서리");
        }
        
        return items;
    }
}


