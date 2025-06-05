package kr.hhplus.be.server.infrastructure.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.infrastructure.dataplatform.DataPlatformService
import kr.hhplus.be.server.infrastructure.kafka.event.OrderCompletedKafkaEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

/**
 * 주문 관련 Kafka 메시지 소비자
 */
@Service
class OrderKafkaConsumer(
    private val dataPlatformService: DataPlatformService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(OrderKafkaConsumer::class.java)

    /**
     * 주문 완료 이벤트 소비 - 데이터 플랫폼 전송용
     */
    @KafkaListener(
        topics = ["order-completed"],
        groupId = "data-platform-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleOrderCompletedForDataPlatform(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_KEY) messageKey: String?,
        acknowledgment: Acknowledgment
    ) {
        try {
            log.info(
                "주문 완료 이벤트 수신 (데이터 플랫폼): topic={}, partition={}, offset={}, key={}",
                topic, partition, offset, messageKey
            )
            
            val event = objectMapper.readValue(message, OrderCompletedKafkaEvent::class.java)
            
            // 데이터 플랫폼으로 주문 정보 전송
            dataPlatformService.sendOrderData(event)
            
            // 메시지 처리 완료 확인
            acknowledgment.acknowledge()
            
            log.info(
                "주문 완료 이벤트 처리 완료 (데이터 플랫폼): orderId={}, userId={}",
                event.orderId, event.userId
            )
            
        } catch (e: Exception) {
            log.error(
                "주문 완료 이벤트 처리 실패 (데이터 플랫폼): topic={}, partition={}, offset={}, error={}",
                topic, partition, offset, e.message, e
            )
            // 실패 시에도 acknowledge하여 무한 재시도 방지
            // 실제 운영에서는 DLQ(Dead Letter Queue) 사용 권장
            acknowledgment.acknowledge()
        }
    }

    /**
     * 주문 완료 이벤트 소비 - 알림 서비스용
     */
    @KafkaListener(
        topics = ["order-completed"],
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleOrderCompletedForNotification(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_KEY) messageKey: String?,
        acknowledgment: Acknowledgment
    ) {
        try {
            log.info(
                "주문 완료 이벤트 수신 (알림 서비스): topic={}, partition={}, offset={}, key={}",
                topic, partition, offset, messageKey
            )
            
            val event = objectMapper.readValue(message, OrderCompletedKafkaEvent::class.java)
            
            // 알림 발송 로직 (현재는 로그만 출력)
            log.info(
                "주문 완료 알림 발송: orderId={}, userId={}, totalAmount={}",
                event.orderId, event.userId, event.totalAmount
            )
            
            // 실제 알림 발송 로직이 여기에 들어갈 예정
            // notificationService.sendOrderCompletedNotification(event)
            
            // 메시지 처리 완료 확인
            acknowledgment.acknowledge()
            
            log.info(
                "주문 완료 이벤트 처리 완료 (알림 서비스): orderId={}, userId={}",
                event.orderId, event.userId
            )
            
        } catch (e: Exception) {
            log.error(
                "주문 완료 이벤트 처리 실패 (알림 서비스): topic={}, partition={}, offset={}, error={}",
                topic, partition, offset, e.message, e
            )
            // 실패 시에도 acknowledge하여 무한 재시도 방지
            acknowledgment.acknowledge()
        }
    }
} 