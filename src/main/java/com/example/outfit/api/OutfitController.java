package com.example.outfit.api;

import com.example.outfit.application.OutfitService;
import com.example.outfit.domain.OutfitSuggestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 코디 추천 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/outfit")
@RequiredArgsConstructor
public class OutfitController {

    private final OutfitService outfitService;

    /**
     * 이미지 업로드 및 코디 추천
     * 
     * @param file 업로드된 이미지 파일
     * @return 코디 추천 결과
     */
    @PostMapping(value = "/recommend", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OutfitSuggestion> recommendOutfit(
            @RequestParam("image") MultipartFile file) {
        
        try {
            log.info("이미지 업로드 요청 수신: 파일명={}, 크기={} bytes", 
                    file.getOriginalFilename(), file.getSize());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            byte[] imageBytes = file.getBytes();
            OutfitSuggestion suggestion = outfitService.processOutfitRecommendation(imageBytes);

            return ResponseEntity.ok(suggestion);

        } catch (IOException e) {
            log.error("파일 읽기 실패", e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("코디 추천 처리 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 이미지 업로드 및 제품 목록 추천 (생성된 이미지 포함)
     * 
     * @param file 업로드된 이미지 파일
     * @return 제품 추천 결과 (생성된 이미지 + 제품 목록)
     */
    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OutfitSuggestion> recommendProducts(
            @RequestParam("image") MultipartFile file) {
        
        try {
            log.info("제품 추천 요청 수신: 파일명={}, 크기={} bytes", 
                    file.getOriginalFilename(), file.getSize());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            byte[] imageBytes = file.getBytes();
            OutfitSuggestion suggestion = outfitService.processProductRecommendation(imageBytes);

            return ResponseEntity.ok(suggestion);

        } catch (IOException e) {
            log.error("파일 읽기 실패", e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("제품 추천 처리 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}


