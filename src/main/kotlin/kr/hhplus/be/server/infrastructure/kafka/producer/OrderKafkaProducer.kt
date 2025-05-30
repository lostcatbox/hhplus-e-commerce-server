package kr.hhplus.be.server.infrastructure.kafka.producer

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.infrastructure.kafka.event.OrderCompletedKafkaEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * 주문 관련 Kafka 메시지 발행자
 */
@Service
class OrderKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val log = LoggerFactory.getLogger(OrderKafkaProducer::class.java)
    
    companion object {
        const val ORDER_COMPLETED_TOPIC = "order-completed"
    }

    /**
     * 주문 완료 이벤트를 Kafka로 발행
     * 메시지 키는 userId로 설정하여 같은 사용자의 주문은 같은 파티션에서 순차 처리
     */
    fun publishOrderCompleted(order: Order) {
        val event = OrderCompletedKafkaEvent.from(order)
        val messageKey = "user_${order.userId}" // 사용자별 파티션 분배
        
        try {
            val future: CompletableFuture<SendResult<String, Any>> = kafkaTemplate.send(
                ORDER_COMPLETED_TOPIC,
                messageKey,
                event
            )
            
            future.whenComplete { result, exception ->
                if (exception == null) {
                    log.info(
                        "주문 완료 이벤트 발행 성공: orderId={}, userId={}, partition={}, offset={}",
                        event.orderId,
                        event.userId,
                        result.recordMetadata.partition(),
                        result.recordMetadata.offset()
                    )
                } else {
                    log.error(
                        "주문 완료 이벤트 발행 실패: orderId={}, userId={}, error={}",
                        event.orderId,
                        event.userId,
                        exception.message,
                        exception
                    )
                }
            }
        } catch (e: Exception) {
            log.error(
                "주문 완료 이벤트 발행 중 예외 발생: orderId={}, userId={}, error={}",
                event.orderId,
                event.userId,
                e.message,
                e
            )
            throw e
        }
    }
} 