package com.example.outfit.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 코디 텍스트 → 이미지 생성 프롬프트 변환
 */
@Slf4j
@Component
public class PromptGenerator {

    /**
     * 코디 텍스트를 이미지 생성 프롬프트로 변환
     */
    public String generatePrompt(String outfitText) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("A stylish and well-coordinated outfit: ");
        prompt.append(outfitText);
        prompt.append(". ");
        prompt.append("High quality fashion photography, clean background, ");
        prompt.append("professional lighting, full body shot, ");
        prompt.append("modern and trendy style, detailed clothing textures, ");
        prompt.append("realistic proportions, vibrant colors, ");
        prompt.append("fashion magazine quality.");

        String result = prompt.toString();
        log.debug("생성된 프롬프트: {}", result);
        return result;
    }

    /**
     * 한국어 코디 텍스트를 영어 프롬프트로 변환 (간단한 매핑)
     */
    public String translateToEnglishPrompt(String koreanOutfitText) {
        // 간단한 키워드 매핑 (실제로는 번역 API 사용 권장)
        String english = koreanOutfitText
                .replace("캐주얼", "casual")
                .replace("포멀", "formal")
                .replace("스포츠", "sporty")
                .replace("스트릿", "street")
                .replace("상의", "top")
                .replace("하의", "bottom")
                .replace("아우터", "outerwear")
                .replace("원피스", "dress")
                .replace("봄", "spring")
                .replace("여름", "summer")
                .replace("가을", "autumn")
                .replace("겨울", "winter")
                .replace("면", "cotton")
                .replace("니트", "knit")
                .replace("데님", "denim")
                .replace("스트라이프", "striped")
                .replace("체크", "checked")
                .replace("도트", "polka dot")
                .replace("플레인", "plain");

        return generatePrompt(english);
    }
}


