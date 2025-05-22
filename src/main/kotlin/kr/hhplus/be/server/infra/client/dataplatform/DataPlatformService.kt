package kr.hhplus.be.server.infra.client.dataplatform

import kr.hhplus.be.server.application.order.event.OrderCompletedEvent
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
     * 주문 데이터 전송
     * 실제 구현에서는 외부 API를 호출하게 됩니다.
     */
    fun sendOrderData(event: OrderCompletedEvent) {
        // 실제 API 호출을 모킹합니다.
        log.info("Mock API 호출: 데이터 플랫폼에 주문 정보 전송 - {}", event)

        // 실제 구현시에는 아래와 같이 HTTP 클라이언트를 사용하여 API를 호출합니다.

    }
} 