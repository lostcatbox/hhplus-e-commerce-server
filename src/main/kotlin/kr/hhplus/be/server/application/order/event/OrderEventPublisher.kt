package kr.hhplus.be.server.application.order.event

import kr.hhplus.be.server.domain.order.Order
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * 주문 관련 이벤트 발행자
 */
@Component
class OrderEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    /**
     * 주문 완료 이벤트 발행
     */
    fun publishOrderCompleted(order: Order) {
        val event = OrderCompletedEvent.from(order)
        applicationEventPublisher.publishEvent(event)
    }
} 