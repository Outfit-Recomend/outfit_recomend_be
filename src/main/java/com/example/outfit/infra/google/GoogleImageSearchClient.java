package com.example.outfit.infra.google;

import com.example.outfit.domain.ProductCandidate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Google Custom Search API를 사용하여 상품 이미지 및 링크 검색
 */
@Slf4j
@Component
public class GoogleImageSearchClient {

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    @Value("${google.search.api-key}")
    private String apiKey;

    @Value("${google.search.search-engine-id}")
    private String searchEngineId;

    @Value("${google.search.endpoint}")
    private String searchEndpoint;

    public GoogleImageSearchClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl("https://www.googleapis.com")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * 검색 쿼리로 상품 이미지 및 링크 검색
     */
    public List<ProductCandidate> searchProducts(String query, int maxResults) {
        try {
            // @Value 필드 주입 확인
            if (apiKey == null || apiKey.isEmpty()) {
                log.error("❌ Google Search API 키가 설정되지 않았습니다!");
                return new ArrayList<>();
            }
            if (searchEngineId == null || searchEngineId.isEmpty()) {
                log.error("❌ Google Search 엔진 ID가 설정되지 않았습니다!");
                return new ArrayList<>();
            }
            
            log.info("✅ Google Search API 설정 확인 - API 키: {}..., 엔진 ID: {}", 
                    apiKey.substring(0, Math.min(10, apiKey.length())), searchEngineId);
            
            // Google Custom Search API의 num 파라미터는 최대 10까지만 허용됨
            final int actualMaxResults = Math.min(maxResults, 10);
            if (maxResults > 10) {
                log.warn("⚠️  요청된 maxResults({})가 10을 초과하여 10으로 제한합니다.", maxResults);
            }
            
            // 패션/의류 관련 키워드를 명시적으로 포함하여 옷과 무관한 결과 제외
            // 쿼리에 이미 의류 종류가 포함되어 있으면 그대로 사용, 없으면 추가
            final String fashionQuery;
            if (!containsFashionKeyword(query)) {
                fashionQuery = query + " 옷 의류 패션";
            } else {
                fashionQuery = query;
            }

            log.info("Google Search API 검색 쿼리: {}", fashionQuery);

            String response = null;
            try {
                log.info("Google Search API 호출 시작 - 쿼리: {}, 엔진 ID: {}", fashionQuery, searchEngineId);
                
                // WebClient를 사용하여 API 호출 (URI 빌더 사용으로 자동 인코딩)
                // siteSearch 파라미터로 무신사 사이트로 검색 범위 제한
                response = webClient.get()
                        .uri(uriBuilder -> {
                            java.net.URI builtUri = uriBuilder
                                    .path("/customsearch/v1")
                                    .queryParam("key", apiKey)
                                    .queryParam("cx", searchEngineId)
                                    .queryParam("q", fashionQuery)
                                    .queryParam("searchType", "image")
                                    .queryParam("num", actualMaxResults)
                                    .queryParam("siteSearch", "musinsa.com/products")
                                    .build();
                            log.info("Google Search API 최종 URL (키 마스킹): {}", builtUri.toString().replace(apiKey, "***"));
                            return builtUri;
                        })
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                        
            } catch (WebClientResponseException e) {
                log.error("Google Search API HTTP 에러 - 상태 코드: {}, 응답: {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
                log.error("API 키 또는 검색 엔진 ID를 확인하세요. API 키: {}..., 엔진 ID: {}", 
                        apiKey.substring(0, Math.min(10, apiKey.length())), searchEngineId);
                return new ArrayList<>();
            } catch (Exception e) {
                log.error("Google Search API 호출 중 예외 발생", e);
                return new ArrayList<>();
            }

            if (response == null) {
                log.error("Google Search API 응답이 null입니다.");
                return new ArrayList<>();
            }

            log.info("Google Search API 응답 길이: {} bytes", response.length());
            
            // HTML 에러 페이지인지 확인
            if (response.trim().startsWith("<!DOCTYPE") || response.trim().startsWith("<html")) {
                log.error("Google Search API가 HTML 에러 페이지를 반환했습니다. API 키나 검색 엔진 ID를 확인하세요.");
                log.error("응답 내용 (처음 1000자): {}", response.substring(0, Math.min(1000, response.length())));
                return new ArrayList<>();
            }
            
            // 응답의 처음 부분 로깅 (JSON 구조 확인)
            String responsePreview = response.length() < 1000 ? response : response.substring(0, 1000);
            log.info("Google Search API 응답 미리보기: {}", responsePreview);
            
            // JSON 파싱 전에 "items" 키워드가 있는지 확인
            if (!response.contains("\"items\"") && !response.contains("\"error\"")) {
                log.warn("⚠️  응답에 'items' 또는 'error' 키워드가 없습니다. 응답 구조를 확인하세요.");
            }

            // 모든 검색 결과 반환 (필터링 없음)
            List<ProductCandidate> results = parseSearchResults(response, query);
            log.info("✅ 검색 결과: {}개", results.size());
            
            if (results.isEmpty() && response != null) {
                log.warn("⚠️  검색 결과가 0개입니다.");
                // 에러 확인
                try {
                    JsonNode root = objectMapper.readTree(response);
                    if (root.has("error")) {
                        log.error("Google Search API 에러: {}", root.path("error").toPrettyString());
                    } else if (!root.has("items")) {
                        log.warn("응답에 'items' 필드가 없습니다.");
                    }
                } catch (Exception e) {
                    log.warn("응답 파싱 실패 (처음 500자): {}", response.substring(0, Math.min(500, response.length())));
                }
            }
            
            return results;

        } catch (Exception e) {
            log.error("Google Search API 호출 실패", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 패션/의류 관련 키워드가 포함되어 있는지 확인
     */
    private boolean containsFashionKeyword(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String lowerText = text.toLowerCase();
        String[] fashionKeywords = {
            "옷", "의류", "패션", "상의", "하의", "아우터", "원피스", "드레스",
            "재킷", "코트", "블레이저", "가디건", "스웨터", "후드", "셔츠", "티셔츠",
            "팬츠", "청바지", "바지", "스커트", "반바지", "신발", "운동화", "스니커즈",
            "가방", "액세서리", "fashion", "clothing", "apparel", "outfit", "wear"
        };
        for (String keyword : fashionKeywords) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private List<ProductCandidate> parseSearchResults(String response, String query) {
        List<ProductCandidate> products = new ArrayList<>();

        try {
            if (response == null || response.trim().isEmpty()) {
                log.error("응답이 null이거나 비어있습니다.");
                return products;
            }
            
            JsonNode root = objectMapper.readTree(response);
            
            // 에러 확인
            if (root.has("error")) {
                JsonNode error = root.path("error");
                log.error("Google Search API 에러: {}", error.toPrettyString());
                return products;
            }
            
            JsonNode items = root.path("items");
            
            log.info("응답 파싱 - items 존재: {}, isArray: {}, size: {}", 
                    !items.isMissingNode(), items.isArray(), items.isArray() ? items.size() : 0);

            if (items.isArray() && items.size() > 0) {
                log.info("✅ items 배열 크기: {}", items.size());
                for (int i = 0; i < items.size(); i++) {
                    try {
                        JsonNode item = items.get(i);
                        
                        String link = item.path("image").path("contextLink").asText("");
                        String title = item.path("title").asText("");
                        String snippet = item.path("snippet").asText("");
                        String imageUrl = item.path("link").asText("");
                        
                        log.debug("Item {} - title: {}, imageUrl: {}", i, 
                                title.length() > 50 ? title.substring(0, 50) + "..." : title,
                                imageUrl.length() > 50 ? imageUrl.substring(0, 50) + "..." : imageUrl);
                        
                        ProductCandidate product = ProductCandidate.builder()
                                .title(title)
                                .imageUrl(imageUrl)
                                .link(link.isEmpty() ? imageUrl : link)
                                .snippet(snippet)
                                .searchQuery(query)
                                .build();

                        products.add(product);
                    } catch (Exception e) {
                        log.warn("Item {} 파싱 실패: {}", i, e.getMessage());
                    }
                }
                log.info("✅ 파싱 완료: {}개 상품 생성", products.size());
            } else {
                log.warn("⚠️  검색 결과에 items가 없거나 비어있습니다.");
                if (!items.isMissingNode()) {
                    log.warn("items 타입: {}, 값: {}", items.getNodeType(), items.toString().substring(0, Math.min(200, items.toString().length())));
                }
            }

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("JSON 파싱 실패: {}", e.getMessage());
            log.error("응답 내용 (처음 500자): {}", response.substring(0, Math.min(500, response.length())));
        } catch (Exception e) {
            log.error("검색 결과 파싱 실패: {}", e.getMessage(), e);
            log.error("응답 내용 (처음 500자): {}", response != null ? response.substring(0, Math.min(500, response.length())) : "null");
        }

        return products;
    }
}

