package kr.hhplus.be.server.infrastructure.dataplatform

import kr.hhplus.be.server.application.order.event.OrderCompletedEvent
import kr.hhplus.be.server.infrastructure.kafka.event.OrderCompletedKafkaEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 데이터 플랫폼 서비스
 * 실제로는 외부 데이터 플랫폼 API를 호출하는 구현체가 필요합니다.
 */
@Service
class DataPlatformService {
    private val log = LoggerFactory.getLogger(DataPlatformService::class.java)

    /**
     * 주문 데이터 전송 (Application Event용)
     * 실제 구현에서는 외부 API를 호출하게 됩니다.
     */
    fun sendOrderData(event: OrderCompletedEvent) {
        // 실제 API 호출을 모킹합니다.
        log.info("Mock API 호출: 데이터 플랫폼에 주문 정보 전송 (Application Event) - {}", event)

        // 실제 구현시에는 아래와 같이 HTTP 클라이언트를 사용하여 API를 호출합니다.
        // restTemplate.postForEntity("https://dataplatform.example.com/api/orders", event, Void::class.java)

        // 랜덤하게 지연 시간을 추가하여 실제 API 호출처럼 시뮬레이션 (1~3초)
        val delayMillis = (1000..3000).random().toLong()
        Thread.sleep(delayMillis)

        // 약 10%의 확률로 예외를 발생시켜 실패 케이스 테스트
        if (Math.random() < 0.1) {
            throw RuntimeException("데이터 플랫폼 서버 일시적 오류 (시뮬레이션)")
        }
    }

    /**
     * 주문 데이터 전송 (Kafka Event용)
     * Kafka를 통해 수신된 이벤트를 처리합니다.
     */
    fun sendOrderData(event: OrderCompletedKafkaEvent) {
        // 실제 API 호출을 모킹합니다.
        log.info(
            "Mock API 호출: 데이터 플랫폼에 주문 정보 전송 (Kafka Event) - orderId={}, userId={}, totalAmount={}",
            event.orderId, event.userId, event.totalAmount
        )

        // 실제 구현시에는 아래와 같이 HTTP 클라이언트를 사용하여 API를 호출합니다.
        // restTemplate.postForEntity("https://dataplatform.example.com/api/orders", event, Void::class.java)

        // 랜덤하게 지연 시간을 추가하여 실제 API 호출처럼 시뮬레이션 (500ms~2초)
        val delayMillis = (500..2000).random().toLong()
        Thread.sleep(delayMillis)

        // 약 5%의 확률로 예외를 발생시켜 실패 케이스 테스트
        if (Math.random() < 0.05) {
            throw RuntimeException("데이터 플랫폼 서버 일시적 오류 (Kafka 처리 중)")
        }

        log.info("데이터 플랫폼 전송 완료: orderId={}, userId={}", event.orderId, event.userId)
    }
} 