package kr.hhplus.be.server.infrastructure.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.infrastructure.kafka.event.CouponIssuedKafkaEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

/**
 * 쿠폰 관련 Kafka 메시지 소비자
 */
@Service
class CouponKafkaConsumer(
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(CouponKafkaConsumer::class.java)

    /**
     * 쿠폰 발급 이벤트 소비 - 이메일 발송용
     */
    @KafkaListener(
        topics = ["coupon-issued"],
        groupId = "email-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleCouponIssuedForEmail(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_KEY) messageKey: String?,
        acknowledgment: Acknowledgment
    ) {
        try {
            log.info(
                "쿠폰 발급 이벤트 수신 (이메일 서비스): topic={}, partition={}, offset={}, key={}",
                topic, partition, offset, messageKey
            )
            
            val event = objectMapper.readValue(message, CouponIssuedKafkaEvent::class.java)
            
            // 이메일 발송 로직 (Mock)
            sendCouponIssuedEmail(event)
            
            // 메시지 처리 완료 확인
            acknowledgment.acknowledge()
            
            log.info(
                "쿠폰 발급 이메일 발송 완료: couponId={}, userId={}",
                event.couponId, event.userId
            )
            
        } catch (e: Exception) {
            log.error(
                "쿠폰 발급 이메일 처리 실패: topic={}, partition={}, offset={}, error={}",
                topic, partition, offset, e.message, e
            )
            acknowledgment.acknowledge()
        }
    }

    /**
     * 쿠폰 발급 이벤트 소비 - 통계 수집용
     */
    @KafkaListener(
        topics = ["coupon-issued"],
        groupId = "analytics-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleCouponIssuedForAnalytics(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_KEY) messageKey: String?,
        acknowledgment: Acknowledgment
    ) {
        try {
            log.info(
                "쿠폰 발급 이벤트 수신 (통계 서비스): topic={}, partition={}, offset={}, key={}",
                topic, partition, offset, messageKey
            )
            
            val event = objectMapper.readValue(message, CouponIssuedKafkaEvent::class.java)
            
            // 통계 수집 로직 (Mock)
            collectCouponStatistics(event)
            
            // 메시지 처리 완료 확인
            acknowledgment.acknowledge()
            
            log.info(
                "쿠폰 발급 통계 수집 완료: couponId={}, userId={}",
                event.couponId, event.userId
            )
            
        } catch (e: Exception) {
            log.error(
                "쿠폰 발급 통계 처리 실패: topic={}, partition={}, offset={}, error={}",
                topic, partition, offset, e.message, e
            )
            acknowledgment.acknowledge()
        }
    }

    /**
     * 쿠폰 발급 완료 이메일 발송 (Mock)
     */
    private fun sendCouponIssuedEmail(event: CouponIssuedKafkaEvent) {
        log.info(
            "Mock 이메일 발송: 사용자 {}에게 쿠폰 '{}' 발급 완료 알림 (할인금액: {}원)",
            event.userId, event.couponName, event.discountAmount
        )
        
        // 실제 구현에서는 이메일 발송 서비스 호출
        // emailService.sendCouponIssuedNotification(event)
        
        // 처리 시간 시뮬레이션
        Thread.sleep((500..1500).random().toLong())
    }

    /**
     * 쿠폰 발급 통계 수집 (Mock)
     */
    private fun collectCouponStatistics(event: CouponIssuedKafkaEvent) {
        log.info(
            "Mock 통계 수집: 쿠폰 발급 통계 업데이트 - couponId={}, userId={}, discountAmount={}",
            event.couponId, event.userId, event.discountAmount
        )
        
        // 실제 구현에서는 통계 DB 업데이트
        // statisticsService.updateCouponIssuanceStats(event)
        
        // 처리 시간 시뮬레이션
        Thread.sleep((200..800).random().toLong())
    }
} 