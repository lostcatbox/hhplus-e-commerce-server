package kr.hhplus.be.server.presentation.controller.order.dto

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderStatus

data class OrderResponse(
    val orderId: Long,
    val status: String
) {
    companion object {
        fun of(order: Order): OrderResponse {
            return OrderResponse(
                orderId = order.id,
                status = order.orderStatus.name
            )
        }
    }
} 