package kr.hhplus.be.server.application.order.event

import kr.hhplus.be.server.infra.client.dataplatform.DataPlatformService
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
    private val dataPlatformService: DataPlatformService
) {
    private val log = LoggerFactory.getLogger(OrderEventListener::class.java)

    /**
     * 주문 완료 이벤트 처리
     * 트랜잭션이 커밋된 후에 비동기로 처리합니다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleOrderCompletedEvent(event: OrderCompletedEvent) {
        try {
            log.info("데이터 플랫폼에 주문 정보 전송 시작: orderId={}", event.orderId)
            dataPlatformService.sendOrderData(event)
            log.info("데이터 플랫폼에 주문 정보 전송 완료: orderId={}", event.orderId)
        } catch (e: Exception) {
            // 데이터 플랫폼 전송 실패 시 핵심 로직에 영향을 주지 않고 로그만 남깁니다.
            log.warn("데이터 플랫폼에 주문 정보 전송 실패: orderId={}, error={}", event.orderId, e.message, e)
        }
    }
} 