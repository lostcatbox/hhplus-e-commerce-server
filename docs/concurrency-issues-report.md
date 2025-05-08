# 동시성 이슈 분석 및 해결 보고서

## 목차
1. [문제 식별](#문제-식별)
2. [분석](#분석)
3. [해결](#해결)
4. [대안](#대안)

## 문제 식별

본 프로젝트에서 다음과 같은 동시성 이슈가 발생할 수 있는 부분을 식별하였습니다:

| No | 문제 영역 | 동시성 이슈 | 위험성 |
|------|----------|------------|-------|
| 1 | 상품 재고 차감 | 여러 요청이 동시에 같은 상품의 재고를 차감할 때 정합성 훼손 | 상품 오버셀링, 재고 음수 발생 |
| 2 | 유저 포인트 잔액 | 여러 요청이 동시에 같은 유저의 포인트를 사용/충전할 때 정합성 훼손 | 포인트 오버드로잉, 포인트 정합성 훼손 |
| 3 | 선착순 쿠폰 | 여러 사용자가 동시에 한정 수량 쿠폰을 요청할 때 정합성 훼손 | 쿠폰 오버이슈, 재고 음수 발생 |

각 문제를 좀 더 구체적으로 살펴보겠습니다:

### 1. 상품 재고 차감 및 실패시 복원

기존 구현은 다음과 같은 순서로 재고를 차감합니다:
1. 상품 조회 (findById)
2. 재고 차감 (product.sale)
3. 변경된 상품 저장 (save)

여러 요청이 동시에 같은 상품의 재고를 조회하고 차감하려고 할 때, 읽은 시점(Read)에 상품의 재고 값은 동일하기 때문에 동시에 같은 재고에서 차감을 시도하게 됩니다. 이는 Lost Update 문제를 발생시킵니다.

### 2. 유저 포인트 잔액

유저 포인트 역시 비슷한 구조로 구현되어 있습니다:
1. 포인트 조회 (findByUserId)
2. 포인트 차감/충전 (usePoint/chargePoint)
3. 변경된 포인트 저장 (save)

동시에 여러 요청이 같은 사용자의 포인트를 조회하고 수정하려고 할 때 Lost Update 문제가 발생할 수 있습니다.

### 3. 선착순 쿠폰

쿠폰 발급 기능은 다음 단계로 수행됩니다:
1. 쿠폰 조회 (findById)
2. 쿠폰 발급 가능 여부 확인 (isAvailable)
3. 쿠폰 발급 및 재고 차감 (issueTo)
4. 변경된 쿠폰 저장 (save)

여러 사용자가 동시에 같은 쿠폰을 요청하면, 재고가 충분하지 않은 상황에서도 모든 요청이 재고가 충분하다고 판단하여 발급을 시도할 수 있습니다.

## 분석

### AS-IS: 문제점 및 원인

| 문제 영역 | 현재 구현 (AS-IS) | 문제점 |
|----------|-----------------|-------|
| 상품 재고 차감 | 일반 조회 후 업데이트 | 동시 접근 시 가장 마지막에 저장한 값만 적용됨(Lost Update) |
| 유저 포인트 잔액 | 일반 조회 후 업데이트 | 동시 접근 시 포인트 잔액 정합성 훼손 |
| 선착순 쿠폰 | 일반 조회 후 업데이트 | 동시 접근 시 쿠폰 초과 발급 가능 |

각 문제 영역의 동시성 이슈를 테스트한 결과:

1. **상품 재고 차감**: 동시에 여러 요청이 동일 상품을 구매할 경우, 재고 차감이 정확히 이루어지지 않고 일부 트랜잭션의 재고 차감이 무시됨
2. **유저 포인트 잔액**: 동시에 여러 요청이 동일 유저의 포인트를 사용/충전할 경우, 포인트 잔액이 정확히 반영되지 않음
3. **선착순 쿠폰**: 재고보다 많은 수의 쿠폰이 발급될 수 있음

### 기술적 원인

이러한 문제가 발생하는 근본적인 원인은 다음과 같습니다:

1. **경쟁 상태(Race Condition)**: 여러 트랜잭션이 동시에 같은 데이터에 접근할 때 발생
2. **Lost Update 문제**: 한 트랜잭션의 변경사항이 다른 트랜잭션에 의해 덮어씌워짐
3. **Dirty Read**: 커밋되지 않은 데이터를 다른 트랜잭션이 읽는 문제
4. **트랜잭션 격리 수준**: 기본 격리 수준(READ COMMITTED)이 동시성 제어에 충분하지 않음

## 해결

### TO-BE: 해결책 및 적용

| 문제 영역 | 해결책 (TO-BE) | 기대 효과 |
|----------|--------------|---------|
| 상품 재고 차감 | 비관적 락(Pessimistic Lock) 적용 | 동시 접근 시에도 재고 정합성 보장 |
| 유저 포인트 잔액 | 비관적 락(Pessimistic Lock) 적용 | 동시 접근 시에도 포인트 정합성 보장 |
| 선착순 쿠폰 | 비관적 락(Pessimistic Lock) 적용 | 정확히 재고 수량만큼만 쿠폰 발급 보장 |

### 적용 방법

#### 1. 상품 재고 차감

```kotlin
// AS-IS
@Transactional
fun saleOrderProducts(orderLines: List<OrderLineCriteria>) {
    for (orderLine in orderLines) {
        val product = findById(orderLine.productId)
        // 재고 차감
        val updatedProduct = product.sale(orderLine.quantity)
        productRepository.save(updatedProduct)
    }
}

// TO-BE
@Transactional
fun saleOrderProducts(orderLines: List<OrderLineCriteria>) {
    for (orderLine in orderLines) {
        // 비관적 락을 사용하여 상품 조회
        val product = productRepository.findByIdWithPessimisticLock(orderLine.productId)
        // 재고 차감
        val updatedProduct = product.sale(orderLine.quantity)
        productRepository.save(updatedProduct)
    }
}
```

#### 2. 유저 포인트 잔액

```kotlin
// AS-IS
fun usePoint(userId: Long, useAmount: Long) {
    val point = pointRepository.findByUserId(userId) ?: Point.EMPTY(userId)
    val updatedPoint = point.usePoint(useAmount)
    pointRepository.save(updatedPoint)
}

// TO-BE
@Transactional
fun usePoint(userId: Long, useAmount: Long) {
    // 비관적 락을 사용하여 포인트 조회
    val point = pointRepository.findByUserIdWithPessimisticLock(userId) ?: Point.EMPTY(userId)
    val updatedPoint = point.usePoint(useAmount)
    pointRepository.save(updatedPoint)
}
```

#### 3. 선착순 쿠폰

```kotlin
// AS-IS는 이미 비관적 락이 적용되어 있어 그대로 유지
@Transactional
fun issuedCoupon(userId: Long, couponId: Long) {
    // 비관적 락을 사용하여 쿠폰 조회
    val coupon = couponRepository.findByIdWithPessimisticLock(couponId)
    val issuedCouponAndCoupon = coupon.issueTo(userId)
    couponRepository.save(issuedCouponAndCoupon)
}
```

### 테스트 결과

| 문제 영역 | 테스트 시나리오 | AS-IS 결과 | TO-BE 결과 |
|----------|--------------|-----------|-----------|
| 상품 재고 차감 | 50개 동시 요청, 초기 재고 30개 | 오버셀링 발생, 재고 음수 | 정확히 30개 요청 성공, 20개 실패 |
| 유저 포인트 잔액 | 50개 동시 포인트 사용/충전 요청 | 잘못된 잔액 계산 | 정확한 잔액 유지 |
| 선착순 쿠폰 | 50명 동시 요청, 쿠폰 재고 10개 | TO-BE와 동일 (이미 비관적 락 적용) | 정확히 10명만 쿠폰 발급 성공 |

## 대안

비관적 락 외에도 다음과 같은 대안적 해결책을 고려할 수 있습니다:

| 해결책 | 장점 | 단점 | 적합한 상황 |
|-------|------|------|-----------|
| 낙관적 락(Optimistic Lock) | 동시 요청이 적을 때 성능 우수, 데드락 위험 없음 | 충돌 시 재시도 로직 필요, 높은 충돌률시 성능 저하 | 동시 수정 확률이 낮은 경우 |
| 분산 락(Distributed Lock) | 서버 간 동시성 제어 가능, 확장성 우수 | 구현 복잡도 증가, 외부 의존성 추가 | 다중 서버 환경, MSA 환경 |
| 메시지 큐 | 비동기 처리로 부하 분산, 시스템 복원력 향상 | 실시간성 감소, 추가 인프라 필요 | 높은 처리량이 필요한 경우 |
| MVCC(다중 버전 동시성 제어) | 읽기 작업 병렬 처리 가능, 잠금 없이 처리 | DB 종속적, 구현 복잡도 높음 | 읽기 작업이 많은 경우 |

### 결론 및 권장사항

본 프로젝트의 동시성 이슈에 대해 비관적 락 방식을 채택한 이유:

1. **안전성**: 비관적 락은 동시성 문제를 가장 확실하게 방지할 수 있음
2. **구현 용이성**: JPA와 Spring의 내장 기능으로 쉽게 구현 가능
3. **성능**: 현재 시스템 부하 수준에서는 비관적 락으로도 충분한 성능 제공

향후 시스템 확장 시 고려사항:
1. 비관적 락으로 인한 성능 저하가 심각해질 경우 낙관적 락이나 분산 락으로 전환 검토
2. 높은 트래픽에 대비한 샤딩(Sharding) 전략 구상
3. 실시간성이 덜 중요한 기능은 메시지 큐 도입 검토

### 테스트 코드

동시성 이슈를 재현하고 해결책의 효과를 검증하기 위한 테스트 코드를 작성하였습니다:
- [ProductInventoryConcurrencyTest.kt](../src/test/kotlin/kr/hhplus/be/server/concurrency/ProductInventoryConcurrencyTest.kt)
- [PointBalanceConcurrencyTest.kt](../src/test/kotlin/kr/hhplus/be/server/concurrency/PointBalanceConcurrencyTest.kt)
- [FirstComeCouponConcurrencyTest.kt](../src/test/kotlin/kr/hhplus/be/server/concurrency/FirstComeCouponConcurrencyTest.kt) 