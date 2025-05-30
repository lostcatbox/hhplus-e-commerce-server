package kr.hhplus.be.server.support.event

import kr.hhplus.be.server.application.order.event.OrderCompletedEvent
import kr.hhplus.be.server.infrastructure.dataplatform.DataPlatformService
import kr.hhplus.be.server.infrastructure.kafka.producer.OrderKafkaProducer
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 주문 이벤트 리스너
 * 주문 관련 이벤트를 처리합니다.
 */
@Component
class OrderEventListener(
    private val dataPlatformService: DataPlatformService,
    private val orderKafkaProducer: OrderKafkaProducer
) {
    private val log = LoggerFactory.getLogger(OrderEventListener::class.java)

    /**
     * 주문 완료 이벤트 처리 - Application Event 방식
     * 트랜잭션이 커밋된 후에 비동기로 처리합니다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleOrderCompletedEvent(event: OrderCompletedEvent) {
        try {
            log.info("Application Event 처리 시작: orderId={}", event.orderId)
            dataPlatformService.sendOrderData(event)
            log.info("Application Event 처리 완료: orderId={}", event.orderId)
        } catch (e: Exception) {
            // 데이터 플랫폼 전송 실패 시 핵심 로직에 영향을 주지 않고 로그만 남깁니다.
            log.warn("Application Event 처리 실패: orderId={}, error={}", event.orderId, e.message, e)
        }
    }

    /**
     * 주문 완료 이벤트 처리 - Kafka 방식
     * 트랜잭션이 커밋된 후에 Kafka로 메시지를 발행합니다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleOrderCompletedEventForKafka(event: OrderCompletedEvent) {
        try {
            log.info("Kafka 메시지 발행 시작: orderId={}", event.orderId)

            // OrderCompletedEvent를 Order 객체로 변환하여 Kafka Producer에 전달
            // 실제로는 Order 객체를 직접 전달받는 것이 좋지만, 
            // 현재 구조에서는 이벤트를 통해 필요한 정보를 재구성
            val order = reconstructOrderFromEvent(event)
            orderKafkaProducer.publishOrderCompleted(order)

            log.info("Kafka 메시지 발행 완료: orderId={}", event.orderId)
        } catch (e: Exception) {
            // Kafka 발행 실패 시에도 핵심 로직에 영향을 주지 않고 로그만 남깁니다.
            log.warn("Kafka 메시지 발행 실패: orderId={}, error={}", event.orderId, e.message, e)
        }
    }

    /**
     * OrderCompletedEvent로부터 Order 객체를 재구성
     * 실제 구현에서는 OrderRepository를 통해 조회하는 것이 더 안전합니다.
     */
    private fun reconstructOrderFromEvent(event: OrderCompletedEvent): kr.hhplus.be.server.domain.order.Order {
        // 이는 임시 구현입니다. 실제로는 OrderRepository를 통해 조회해야 합니다.
        return kr.hhplus.be.server.domain.order.Order(
            id = event.orderId,
            userId = event.userId,
            orderLines = event.orderLines.map { lineInfo ->
                kr.hhplus.be.server.domain.order.OrderLine(
                    productId = lineInfo.productId,
                    productPrice = lineInfo.price,
                    quantity = lineInfo.quantity
                )
            }.toMutableList(),
            orderDateTime = java.time.LocalDateTime.parse(event.orderDateTime),
            orderStatus = kr.hhplus.be.server.domain.order.OrderStatus.결제_완료
        )
    }
} 