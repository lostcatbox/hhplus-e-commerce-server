# 캐시 전략 설계 문서

## 1. 트래픽 병목 지점 분석

### 주요 병목 지점
- **인기 상품 목록 조회**: 인기 상품 목록을 조회할 때 발생하는 복잡한 JOIN 쿼리와 집계 연산
- **상품 상세 정보 조회**: 상세 페이지 로딩 시 상품 정보, 리뷰, 관련 상품 등 여러 테이블 접근 필요
- **사용자 포인트 조회**: 자주 조회되지만 변경은 적은 사용자 포인트 데이터

### 병목 발생 원인
1. **복잡한 집계 쿼리**: 인기 상품 계산을 위한 ORDER BY, GROUP BY 등 집계 연산
2. **다중 테이블 조인**: 여러 테이블을 JOIN하는 쿼리의 실행 비용
3. **반복적인 동일 쿼리**: 같은 데이터를 짧은 시간에 반복 조회하는 패턴

## 2. 캐시 전략 설계

### 메모리 캐시 vs Redis 캐시 선택 기준

| 기준 | 메모리 캐시 | Redis 캐시 |
|------|------------|------------|
| 성능 | 더 빠른 접근 속도 | 약간의 네트워크 지연 발생 |
| 확장성 | 단일 서버 한정 | 분산 환경에서 일관성 유지 |
| 데이터 지속성 | 서버 재시작 시 소멸 | Disk에 저장으로 복구 가능 |
| 메모리 부담 | 애플리케이션 서버에 부담 | 별도 서버로 부담 분산 |

### 적용 전략 1: 인기 상품 판매량 TTL 캐싱

- **캐싱 대상**: 인기 상품 판매량 기반 정렬 목록
- **캐시 저장소**: Redis (분산 환경 고려)
- **TTL 전략**: 5분 주기로 갱신
- **캐싱 패턴**: Cache-Aside 패턴 사용

```kotlin
// 구현 예시
fun getPopularProductsByOrder(page: Int, size: Int): List<ProductDto> {
    val cacheKey = "popular:products:byOrder:$page:$size"
    
    // Redis에서 캐시 조회
    return redisTemplate.opsForValue().get(cacheKey) ?: run {
        // 캐시 미스 시 DB 조회
        val products = productRepository.findPopularProductsByOrder(page, size)
        
        // 캐시 저장 (5분)
        redisTemplate.opsForValue().set(cacheKey, products, Duration.ofMinutes(5))
        
        products
    }
}
```

### 적용 전략 2: 사용자 포인트 조회 캐싱

- **캐싱 대상**: 사용자 포인트 정보
- **캐시 저장소**: Application Memory (변경 빈도가 낮고 사용자별 데이터)
- **TTL 전략**: 1시간 캐싱 + 포인트 변경 시 Eviction
- **캐싱 패턴**: Cache-Aside + Write-Through 패턴

```kotlin
@Cacheable(value = "userPoints", key = "#userId", unless = "#result == null")
fun getUserPoint(userId: Long): PointDto {
    return pointRepository.findByUserId(userId)?.toDto() ?: throw NotFoundException()
}

@CacheEvict(value = "userPoints", key = "#userId")
fun updateUserPoint(userId: Long, amount: Long) {
    // 포인트 업데이트 로직
}
```

## 3. Expiration과 Eviction 전략

### Expiration (만료) 전략
- **시간 기반 만료**: 데이터의 신선도에 따라 TTL 설정 (인기 상품: 5분, 상품 상세: 30분)
- **그룹 만료**: 관련 데이터를 그룹화하여 한꺼번에 만료 처리 가능
- **자동 만료**: Redis의 자동 만료 기능 활용

### Eviction (제거) 전략
- **명시적 무효화**: 데이터 변경 시 관련 캐시 즉시 삭제
- **패턴 기반 제거**: 와일드카드를 사용한 패턴 매칭으로 관련 캐시 일괄 제거
- **LRU (Least Recently Used)**: 메모리 공간 부족 시 가장 오래된 사용 데이터부터 제거

## 4. Cache Stampede 이슈와 대응 전략

### Cache Stampede 문제
- 캐시가 동시에 만료되어 다수의 요청이 DB에 한꺼번에 몰리는 현상
- 특히 계산 비용이 높은 쿼리에서 서버에 큰 부하 발생
- 인기 상품 목록 같은 공통 데이터 조회 시 심각한 성능 저하 초래

### 대응 전략
1. **조기 갱신 (Early Renewal)**: 만료 시간 직전에 백그라운드에서 캐시 미리 갱신
2. **확률적 조기 만료**: TTL의 80% 시점부터 확률적으로 갱신하여 동시 요청 분산
3. **잠금 기반 갱신**: 첫 요청만 DB 조회 권한을 얻고 나머지는 대기하도록 락 사용
4. **폭포 효과 방지**: 실패 시 임시로 만료된 데이터를 재사용하여 가용성 확보

```kotlin
// Cache Stampede 방지 예시
fun getPopularProductsWithStampedeProtection(): List<ProductDto> {
    val cacheKey = "popular:products:list"
    val lockKey = "lock:$cacheKey"
    
    // 1. 캐시에서 데이터 조회
    val cachedData = redisTemplate.opsForValue().get<List<ProductDto>>(cacheKey)
    
    if (cachedData != null) {
        return cachedData
    }
    
    // 2. 캐시 미스 시 락 획득 시도
    val acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(10))
    
    // 3. 락 획득 실패 시 작업 중인 다른 스레드 대기 (짧은 시간 후 재시도)
    if (acquired != true) {
        Thread.sleep(100)
        return getPopularProductsWithStampedeProtection() // 재시도
    }
    
    try {
        // 4. 재확인 (다른 스레드가 이미 갱신했을 수 있음)
        val recheck = redisTemplate.opsForValue().get<List<ProductDto>>(cacheKey)
        if (recheck != null) {
            return recheck
        }
        
        // 5. DB 조회 및 캐시 저장
        val products = productRepository.findPopularProducts()
        redisTemplate.opsForValue().set(cacheKey, products, Duration.ofMinutes(5))
        return products
    } finally {
        // 6. 락 해제
        redisTemplate.delete(lockKey)
    }
}
``` 