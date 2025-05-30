package kr.hhplus.be.server.infrastructure.kafka.producer

import kr.hhplus.be.server.infrastructure.kafka.event.CouponIssuedKafkaEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * 쿠폰 관련 Kafka 메시지 발행자
 */
@Service
class CouponKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val log = LoggerFactory.getLogger(CouponKafkaProducer::class.java)
    
    companion object {
        const val COUPON_ISSUED_TOPIC = "coupon-issued"
    }

    /**
     * 쿠폰 발급 완료 이벤트를 Kafka로 발행
     * 메시지 키는 userId로 설정하여 같은 사용자의 쿠폰은 같은 파티션에서 순차 처리
     */
    fun publishCouponIssued(event: CouponIssuedKafkaEvent) {
        val messageKey = "user_${event.userId}"
        
        try {
            val future: CompletableFuture<SendResult<String, Any>> = kafkaTemplate.send(
                COUPON_ISSUED_TOPIC,
                messageKey,
                event
            )
            
            future.whenComplete { result, exception ->
                if (exception == null) {
                    log.info(
                        "쿠폰 발급 이벤트 발행 성공: couponId={}, userId={}, partition={}, offset={}",
                        event.couponId,
                        event.userId,
                        result.recordMetadata.partition(),
                        result.recordMetadata.offset()
                    )
                } else {
                    log.error(
                        "쿠폰 발급 이벤트 발행 실패: couponId={}, userId={}, error={}",
                        event.couponId,
                        event.userId,
                        exception.message,
                        exception
                    )
                }
            }
        } catch (e: Exception) {
            log.error(
                "쿠폰 발급 이벤트 발행 중 예외 발생: couponId={}, userId={}, error={}",
                event.couponId,
                event.userId,
                e.message,
                e
            )
            throw e
        }
    }
} 