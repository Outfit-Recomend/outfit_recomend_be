package com.example.outfit.api;

import com.example.outfit.application.OutfitService;
import com.example.outfit.domain.OutfitSuggestion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Outfit Recommendation", description = "이미지 기반 코디 추천 API")
public class OutfitController {

    private final OutfitService outfitService;

    /**
     * 이미지 업로드 및 코디 추천
     * 
     * @param file 업로드된 이미지 파일
     * @return 코디 추천 결과
     */
    @Operation(
            summary = "코디 추천",
            description = "업로드된 이미지를 분석하여 코디를 추천하고, 추천된 코디를 입은 모습의 이미지를 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = OutfitSuggestion.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 파일 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/recommend", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OutfitSuggestion> recommendOutfit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업로드할 이미지 파일 (JPG, PNG 등, 최대 20MB)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
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
    @Operation(
            summary = "제품 추천",
            description = "업로드된 이미지를 분석하여 코디를 추천하고, 추천된 코디를 입은 모습의 이미지를 생성하며, 관련 제품 목록을 검색합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = OutfitSuggestion.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 파일 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OutfitSuggestion> recommendProducts(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업로드할 이미지 파일 (JPG, PNG 등, 최대 20MB)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
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
    @Operation(
            summary = "헬스 체크",
            description = "서버 상태를 확인합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "서버 정상 작동")
    })
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}


