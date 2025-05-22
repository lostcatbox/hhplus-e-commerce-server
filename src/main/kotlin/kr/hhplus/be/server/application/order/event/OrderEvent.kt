package kr.hhplus.be.server.application.order.event

import kr.hhplus.be.server.domain.order.Order

/**
 * 주문 완료 이벤트
 * 주문이 성공적으로 완료되었을 때 발행되는 이벤트
 */
data class OrderCompletedEvent(
    val orderId: Long,
    val userId: Long,
    val orderLines: List<OrderLineInfo>,
    val totalAmount: Long,
    val orderDateTime: String
) {
    companion object {
        fun from(order: Order): OrderCompletedEvent {
            return OrderCompletedEvent(
                orderId = order.id,
                userId = order.userId,
                orderLines = order.orderLines.map {
                    OrderLineInfo(
                        productId = it.productId,
                        quantity = it.quantity,
                        price = it.productPrice
                    )
                },
                totalAmount = order.totalPrice,
                orderDateTime = order.orderDateTime.toString()
            )
        }
    }
}

/**
 * 주문 라인 정보
 */
data class OrderLineInfo(
    val productId: Long,
    val quantity: Long,
    val price: Long
) 