# MSA 도메인 분리 설계 및 트랜잭션 문제 해결 방안

## 1. 도메인 분리 전략

현재 모놀리식 시스템을 MSA로 전환하기 위한 도메인 분리 전략을 수립합니다. OrderFacade를 분석한 결과, 다음과 같은 핵심 도메인으로 분리할 수 있습니다.

### 1.1 도메인 식별 및 분리

| 마이크로서비스     | 핵심 책임                | 주요 엔티티                         |
|-------------|----------------------|--------------------------------|
| **주문 서비스**  | 주문 생성 및 관리, 주문 상태 변경 | Order, OrderLine, OrderHistory |
| **상품 서비스**  | 상품 정보 관리, 재고 관리      | Product, ProductStatistic      |
| **결제 서비스**  | 결제 처리, 트랜잭션 기록       | Payment                        |
| **사용자 서비스** | 사용자 정보 관리, 포인트 관리    | User, Point                    |
| **쿠폰 서비스**  | 쿠폰 발급 및 사용 관리        | Coupon, IssuedCoupon           |
| **데이터 플랫폼** | 데이터 수집 및 분석          | OrderData, ReservationData     |

### 1.2 서비스 간 의존성 분석

![서비스 의존성 다이어그램](https://mermaid.ink/img/pako:eNqNkc9KAzEQxl8lzLlQqK4u9mJ7Eb1ZUUQvkoZpsljTJJtNKYgPsPgQ3noneLAX8QWSdLeu4h_wMDPf7_tmJhNDaYkgOZ2bpzKzOcFYWf2YK-eeSQ8G6I4acpCWxZ-KuXEVFQxCb0FmZEe9D1Zqi5xgEljEuuCvwJLpUKwsGBEchX1f9xYdP5eQ9ByDO9_4CsAp14DVOUOjDWC_GY7NHXHgdxDsNNw1kHcGgGDQgPcQcdfDXjOTdXu97eFR5nZXQ7CzBwQ_W_J_OLl_OJtxFYeDYTi96lCksiYOFXPWnDRSUl7IVTDQWSYnDQyWtG7kUO97sXM9bxfEHtUlrDGjwRdW8yfMGSSRSaxkFRVqcKnL--hn4-4ngjTQGERURDvaDDJIsltD2w2wjJQ_S8mvrJ7jYW3aVpBOrZX3zlyK12h4FIXjOJzE0XgcDKJwGIV7gVeQdVPVpYjD-ZjDcTgZRcn5ZJhEk3GwnzQ_YsqzvA?type=png)

## 2. 트랜잭션 분리에 따른 문제점

### 2.1 분산 트랜잭션 문제

모놀리식 시스템에서 `OrderFacade`는 하나의 트랜잭션으로 다음 작업을 수행합니다:

```kotlin
@Transactional
fun processOrder(orderCriteria: OrderCriteria): Order {
    // 1. 유저 검증
    // 2. 주문 생성
    // 3. 상품 준비중 상태 변경
    // 4. 상품 재고 확인 및 차감
    // 5. 발급된 쿠폰 사용
    // 6. 결제 대기 상태로 변경
    // 7. 결제 처리
    // 8. 결제 성공 상태로 변경
    // 9. 결제 완료 이벤트 발행
}
```

MSA로 분리 시 발생하는 주요 문제:

1. **원자성(Atomicity) 상실**: 여러 서비스에 걸친 작업이 부분적으로만 성공하는 상황 발생
2. **데이터 일관성(Consistency) 유지 어려움**: 각 서비스가 독립적인 데이터베이스를 가지므로 일관성 보장 어려움
3. **격리성(Isolation) 확보 어려움**: 여러 서비스에 걸친 동시 작업 제어 불가
4. **즉시 일관성 vs 최종 일관성**: 분산 환경에서 즉시 일관성 확보가 어려움

### 2.2 구체적 시나리오 분석

| 시나리오            | 문제점                                          |
|-----------------|----------------------------------------------|
| **상품 재고 부족 롤백** | 주문 생성 후 상품 서비스에서 재고 부족 발견 시, 이미 생성된 주문 처리 필요 |
| **결제 실패**       | 재고 차감, 쿠폰 사용 후 결제 실패 시, 변경된 데이터 복구 필요        |
| **네트워크 장애**     | 서비스 간 통신 중 일시적 장애 발생 시, 트랜잭션 일관성 보장 어려움      |
| **서비스 다운**      | 특정 서비스 다운 시, 전체 주문 흐름 중단 및 복구 전략 필요          |

## 3. 트랜잭션 문제 해결 방안

### 3.1 SAGA 패턴

분산 환경에서 트랜잭션 일관성을 보장하기 위한 SAGA 패턴을 도입합니다.

#### 3.1.1 코레오그래피(Choreography) 방식 SAGA

각 서비스가 이벤트를 발행하고 다른 서비스가 구독하는 방식으로 트랜잭션 조율:

![코레오그래피 SAGA](https://mermaid.ink/img/pako:eNqNk9FuwjAMRX8lyhvTpMHWwZ5gwFPFxCaQeEGTmTaxIKmSFKRp_Pss0G5MgrW8xLl3ju0kB5CpQBAMX-lLRtkCU8yMfsxQmDdMZpjBLVVUoTBMnyZMTJpTwcjsLAhGydTaYI01whKLwFCW87fAHCo0XQqzJC6ZvY-dRcsOOSS3LPr2RDcAVOgaNDtHNGgQ9hpqalXPTrILhI22OkrIJo1AUOuorQTpUg-7zbjudjrj-nQk-mwYhdq_QfBzS_4vJ_ePg3fWRcNCVSY0r_IEtE6xyBn0KTpKaM89jMUiYRfKbOyYCXkBBDz5mDANYbfX7_cfi2hPIUfnTUGGKmj8K9b-IRYMkojpRKMsaXAhiqf1j02_n2qkjtoAU5lsQ5OBRpDNJMz-BZ1O5aeM_D7UfbZYPZ1JkE5ZI-_KvajXqHiQJHsseS_ZHSVpN0la6x6VXLBCSI4U0UZeUGqVpO2e3XtJ2k7uJzPfT2OcAA?type=png)

#### 3.1.2 오케스트레이션(Orchestration) 방식 SAGA

중앙 오케스트레이터가 트랜잭션 단계를 조율하는 방식:

![오케스트레이션 SAGA](https://mermaid.ink/img/pako:eNqNkk9PAjEQxb9KM-eCIdlFwZsejCcTEzAkXJpuZ7cNbbftdDfGhA9g9CO49U7iwYvxC5R2QYkS9TLT9_vNn8wWlFKokYnibVsJmXoSShHm-9SbzMdCr0fqUBqYLRnhgVzLWu6hV-yIxvT2TLIRXoEHvBxiXRtaYAQmmB_KXEIoLzLHCtVq4TIlE4eGlGJAXtgQ1hY0hh2BMxydWKjRo4WuQXFyBsYdGBqB1eBc5L-xfqsBwgPBFIBiOIDQoEPPGkDUKXDarOh219Vk_i-WOY4E0PJ3AH4eP_9i4fnkYa0MnN_iO96VD-Aey3LimSldyG-gZOI-EkpWTBdCPzkZ9_UfVVboDN_cP6VwqJZs4e2Z-K0tjN0IpBRBZmQ9DU1uVXWfnF12-x4NLKEt0aaRW-gtGBTt1mj6Dpp6ln-y8ntUj71iuemLjd83JvCE81a8RuH9OL4R5W0Ub0Xxbj_uxvFOvL3sYUVXtDbaUYI_zYOtXmcnjodjcRWH3a34NLvYFCqV-Yj6UToYX_X2x-nhOB0M080vdZQvwA?type=png)

### 3.2 이벤트 기반 아키텍처

[이미 구현한 이벤트 기반 아키텍처]를 확장하여 서비스 간 통신에 활용합니다.

```kotlin
// 서비스 A: 이벤트 발행
@Transactional
fun processOrder(orderCriteria: OrderCriteria): Order {
    // 로직 수행
    eventPublisher.publishOrderCreatedEvent(order)
    return order
}

// 서비스 B: 이벤트 구독
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Async
fun handleOrderCreatedEvent(event: OrderCreatedEvent) {
    // 후속 작업 수행
}
```

### 3.3 보상 트랜잭션(Compensating Transaction)

실패 시 이전 상태로 복원하기 위한 보상 트랜잭션 도입:

```kotlin
try {
    // 주문 생성
    val order = orderService.createOrder(orderCriteria)

    try {
        // 재고 차감
        productService.reduceStock(orderCriteria.orderLines)
    } catch (e: Exception) {
        // 보상 트랜잭션: 주문 취소
        orderService.cancelOrder(order.id)
        throw e
    }

    // 이후 단계...
} catch (e: Exception) {
    // 전체 실패 처리
}
```

### 3.4 분산 락(Distributed Lock)

동시성 제어를 위한 분산 락 도입:

```kotlin
@DistributedLock(key = "stock_#productId")
fun reduceStock(productId: Long, quantity: Long) {
    // 안전하게 재고 차감
}
```

## 4. 구체적인 서비스 구현 방안

### 4.1 주문 흐름 설계

![주문 흐름 설계](https://mermaid.ink/img/pako:eNqVVE1v2zAM_SuCTgaClDWbtNigpofthqLYoUHHHRSZsYVaoke5AYLkv4-282FnmYseFD2SfHyPsmYwVxoBKjhXt-beaDlNQ1RG3Z8oa_9QGxr3DRkIx9Q_FCqpJrFjQ-QDcGVYjKoDeEW0LEOEwHYMCz79DtgxL4iuxixO4jOzf9f3wfFTDskzCz7cqTqAnFwl0-iEGklo4PVp8L5KnE_6DMF-F_1EyNcHQHDQC--cZv3n8c-0OMXFRQmxdrcY7J9C8Pfi_GlcPkXFzfSQnVv7gfQtCpT8zzUmGITKklsOw0cVXdCOsimOF4bKkoOmwXQgYdB5D6EzcX-xZ1fQz7OyuuHGGhxNNVGfqC5JsyFnZV29rEMYVZMJCPvN4aDnOi_HSNK7KwT7vTzvCXLQdQe1B4-iRJRe8_r8oNu-7vb7X1vJ_6zsvLxuvnS7V5JezZKWYy2Xn0B4cI1YDcPqDWWiqqrcrCilUu-NvADgD-cgQ2cwhBwejRwJw9nF1WLxK06eMpuQzZxBiRFM_hxr_h0rAUmUKlEWWsqGB19Vfy1_dvpuxUgVHQjZSs6GYQYGU41YdkOzv0DTPvOvLI9L1S7Kq-dTAtIppavcB5UOPm4E51myYMlLlrxmyds8yfJkP9mJ8MpFLTxHxO_mQWj7LN_Kku082c2SbZ7s5Zs_ORo7Xw?type=png)

### 4.2 각 서비스별 구현 전략

#### 4.2.1 주문 서비스

```kotlin
// OrderService
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderEventPublisher: OrderEventPublisher
) {
    @Transactional
    fun createOrder(orderCriteria: OrderCriteria): Order {
        val order = Order(...)
        val savedOrder = orderRepository.save(order)
        orderEventPublisher.publishOrderCreatedEvent(savedOrder)
        return savedOrder
    }

    @Transactional
    fun cancelOrder(orderId: Long) {
        val order = orderRepository.findById(orderId)
        order.changeStatus(OrderStatus.주문_취소)
        orderRepository.save(order)
        orderEventPublisher.publishOrderCancelledEvent(order)
    }
}
```

#### 4.2.2 상품 서비스

```kotlin
// ProductService
@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productEventPublisher: ProductEventPublisher
) {
    @Transactional
    @DistributedLock(key = "product_#productId")
    fun reduceStock(productId: Long, quantity: Long): Boolean {
        val product = productRepository.findById(productId)
        if (product.stock < quantity) {
            productEventPublisher.publishStockInsufficientEvent(
                StockInsufficientEvent(productId, quantity)
            )
            return false
        }

        product.reduceStock(quantity)
        productRepository.save(product)
        productEventPublisher.publishStockReducedEvent(
            StockReducedEvent(productId, quantity)
        )
        return true
    }

    @Transactional
    fun restoreStock(productId: Long, quantity: Long) {
        val product = productRepository.findById(productId)
        product.increaseStock(quantity)
        productRepository.save(product)
    }
}
```

#### 4.2.3 결제 서비스

```kotlin
// PaymentService
@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentEventPublisher: PaymentEventPublisher
) {
    @Transactional
    fun processPayment(orderId: Long, userId: Long, amount: Long): Payment {
        try {
            // 외부 결제 API 호출
            val payment = Payment(...)
            val savedPayment = paymentRepository.save(payment)
            paymentEventPublisher.publishPaymentSuccessEvent(savedPayment)
            return savedPayment
        } catch (e: Exception) {
            paymentEventPublisher.publishPaymentFailedEvent(
                PaymentFailedEvent(orderId, userId, amount, e.message)
            )
            throw e
        }
    }
}
```

### 4.3 SAGA 오케스트레이터 구현

```kotlin
// OrderSagaOrchestrator
@Service
class OrderSagaOrchestrator(
    private val orderClient: OrderServiceClient,
    private val productClient: ProductServiceClient,
    private val paymentClient: PaymentServiceClient,
    private val couponClient: CouponServiceClient
) {
    fun processOrder(orderRequest: OrderRequest) {
        val sagaState = OrderSagaState()

        try {
            // 1단계: 주문 생성
            val order = orderClient.createOrder(orderRequest)
            sagaState.setOrderCreated(order)

            // 2단계: 재고 확인 및 차감
            val stockResult = productClient.reduceStock(order.orderLines)
            if (!stockResult.success) {
                // 보상 트랜잭션 - 주문 취소
                orderClient.cancelOrder(order.id)
                throw RuntimeException("재고 부족")
            }
            sagaState.setStockReduced(stockResult)

            // 3단계: 쿠폰 사용
            if (orderRequest.issuedCouponId != null) {
                val couponResult = couponClient.useCoupon(orderRequest.issuedCouponId)
                sagaState.setCouponUsed(couponResult)
            }

            // 4단계: 결제 처리
            val paymentResult = paymentClient.processPayment(
                order.id, order.userId, order.calculateTotalPrice()
            )
            sagaState.setPaymentCompleted(paymentResult)

            // 5단계: 주문 완료
            orderClient.completeOrder(order.id)

        } catch (e: Exception) {
            // 보상 트랜잭션 실행
            executeCompensatingTransactions(sagaState)
            throw e
        }
    }

    private fun executeCompensatingTransactions(sagaState: OrderSagaState) {
        // 각 단계별 보상 트랜잭션 실행
        if (sagaState.isPaymentCompleted()) {
            paymentClient.refundPayment(sagaState.getPaymentId())
        }

        if (sagaState.isCouponUsed()) {
            couponClient.restoreCoupon(sagaState.getIssuedCouponId())
        }

        if (sagaState.isStockReduced()) {
            productClient.restoreStock(sagaState.getReducedStocks())
        }

        if (sagaState.isOrderCreated()) {
            orderClient.cancelOrder(sagaState.getOrderId())
        }
    }
}
```

## 5. 결론 및 권장사항

### 5.1. 채택 전략 요약

1. **도메인 분리**: 5개의 핵심 도메인(주문, 상품, 결제, 사용자, 쿠폰)으로 분리
2. **트랜잭션 처리 전략**: SAGA 패턴(오케스트레이션 방식) 도입
3. **이벤트 기반 통신**: 서비스 간 느슨한 결합을 위한 이벤트 기반 통신
4. **분산 락**: 동시성 제어를 위한, Redisson 기반 분산 락 활용
5. **보상 트랜잭션**: 실패 상황에서의 데이터 일관성을 위한 보상 트랜잭션 패턴 도입

### 5.2 마이그레이션 로드맵

1. **1단계**: 도메인별 API 경계 정의 및 서비스 분리
2. **2단계**: 이벤트 기반 통신 구조 구현
3. **3단계**: SAGA 오케스트레이터 구현
4. **4단계**: 서비스별 독립 배포 및 모니터링 구축
5. **5단계**: 점진적 마이그레이션 및 검증

### 5.3 고려사항

- **복잡성 증가**: MSA 전환에 따른 복잡성 및 운영 부담 증가
- **테스트 전략**: 분산 환경에서의 통합 테스트 어려움
- **모니터링**: 분산 추적, 로깅, 모니터링 구축 필요
- **네트워크 신뢰성**: 서비스 간 통신의 안정성 확보 방안 필요
- **데이터 일관성 vs 가용성**: 최종 일관성(eventual consistency)과 가용성 간의 균형

최종적으로 이 설계는 시스템의 확장성과 유연성을 높이면서도, 분산 트랜잭션으로 인한 문제를 효과적으로 해결할 수 있는 방안을 제시합니다.
