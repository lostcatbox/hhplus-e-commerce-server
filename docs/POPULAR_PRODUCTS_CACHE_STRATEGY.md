# 인기 상품 판매량 캐싱 전략

## 1. 개요

인기 상품 판매량 기반 조회는 실시간 집계 과정에서 높은 DB 부하를 발생시키는 대표적인 작업입니다. 이 문서에서는 Redis를 활용한 TTL 캐싱 전략을 통해 인기 상품 조회 성능을 개선하는 방안을 제시합니다.

## 2. 현재 문제점

### 성능 병목 현황
- 판매량 집계를 위한 JOIN 및 GROUP BY 쿼리로 인한 DB 부하
- 인기 상품 조회 API 호출 빈도가 높아 중복 계산 발생
- 특정 시간대(출근 시간, 점심 시간)에 트래픽 집중으로 지연 발생

### 현재 쿼리 분석
```sql
SELECT p.*, COUNT(o.id) as order_count
FROM products p
JOIN order_items oi ON p.id = oi.product_id
JOIN orders o ON oi.order_id = o.id
WHERE o.created_at > DATE_SUB(NOW(), INTERVAL 7 DAY)
  AND o.status = 'COMPLETED'
GROUP BY p.id
ORDER BY order_count DESC
LIMIT 10;
```
- 이 쿼리는 3개 테이블을 조인하며 집계 연산 실행
- 실행 비용: 약 300~500ms (부하 상황에 따라 1초 이상까지 증가)

## 3. 캐싱 전략 설계

### TTL 캐싱 구현 방법

1. **캐시 키 설계**
   - 기본 키: `popular:products:sales:{기간}:{카테고리}:{페이지}:{사이즈}`
   - 기간별 분리: 실시간(1시간), 일간, 주간 등 다양한 기간 지원
   - 카테고리별 분리: 전체 또는 특정 카테고리별 인기 상품 캐싱

2. **TTL 설정**
   - 실시간(1시간): 5분 캐싱
   - 일간 인기상품: 30분 캐싱
   - 주간 인기상품: 1시간 캐싱

3. **데이터 직렬화 방식**
   - Jackson 라이브러리 활용 JSON 직렬화
   - 캐시 저장 시 필요한 필드만 선택적으로 직렬화하여 용량 절약

## 4. 구현 예시

```kotlin
@Service
class PopularProductService(
    private val productRepository: ProductRepository,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {
    // 주간 인기 상품 조회 (판매량 기준)
    fun getWeeklyPopularProducts(categoryId: Long?, page: Int, size: Int): List<ProductDto> {
        val category = if (categoryId != null) "cat:$categoryId" else "all"
        val cacheKey = "popular:products:sales:weekly:$category:$page:$size"
        
        // 1. 캐시 조회
        val cachedValue = redisTemplate.opsForValue().get(cacheKey)
        
        if (cachedValue != null) {
            try {
                // 캐시된 JSON 문자열을 객체로 변환
                return objectMapper.readValue(
                    cachedValue as String,
                    object : TypeReference<List<ProductDto>>() {}
                )
            } catch (e: Exception) {
                // 역직렬화 오류 발생 시 로깅 후 캐시 무시
                log.warn("캐시 역직렬화 오류: $cacheKey", e)
            }
        }
        
        // 2. DB 조회 (캐시 미스)
        val products = productRepository.findPopularProductsBySales(
            categoryId = categoryId,
            startDate = LocalDateTime.now().minusDays(7),
            page = page,
            size = size
        )
        
        // 3. 캐시 저장 (1시간 TTL)
        try {
            val jsonValue = objectMapper.writeValueAsString(products)
            redisTemplate.opsForValue().set(cacheKey, jsonValue, Duration.ofHours(1))
        } catch (e: Exception) {
            log.error("캐시 직렬화 오류", e)
        }
        
        return products
    }
    
    // 캐시 수동 갱신 (스케줄러에서 호출)
    @Scheduled(cron = "0 0/30 * * * *") // 30분마다 실행
    fun refreshPopularProductsCache() {
        // 전체 카테고리 인기 상품 캐시 갱신
        refreshCategoryPopularProducts(null)
        
        // 주요 카테고리별 캐시 갱신
        categoryRepository.findMainCategories().forEach { category ->
            refreshCategoryPopularProducts(category.id)
        }
    }
    
    private fun refreshCategoryPopularProducts(categoryId: Long?) {
        // 페이지당 20개씩, 첫 3페이지를 미리 캐싱
        for (page in 0..2) {
            getWeeklyPopularProducts(categoryId, page, 20)
        }
    }
}
```

## 5. 성능 개선 효과

### 예상 성능 개선
- 응답 시간: 평균 400ms → 10ms 이하 (캐시 히트 시)
- DB 부하 감소: 피크 시간대 DB 연결 수 약 30% 감소 예상
- API 처리량: 초당 최대 처리 요청 3배 이상 증가 예상

### 추가 고려사항
- 캐시 워밍업: 서버 시작 시 또는 주기적으로 인기 상품 캐시 미리 생성
- 어드민 기능: 상품 정보 수정 시 관련 캐시 명시적 갱신 기능 제공
- 모니터링: 캐시 적중률, 응답 시간 등 주요 지표 모니터링

## 6. 장애 대응 방안

1. **캐시 장애 시 Fallback 전략**
   - Redis 연결 오류 발생 시 DB 직접 조회로 전환
   - 응답 지연 시 간소화된 응답으로 대체 (필수 필드만 포함)

2. **캐시 일관성 보장**
   - 판매 완료 시 관련 인기 상품 캐시 선택적 무효화
   - 정기적인 캐시 갱신 스케줄러 운영
   - 캐시 버전 관리를 통한 롤백 방안 마련 