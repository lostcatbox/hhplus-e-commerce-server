package kr.hhplus.be.server.infrastructure.kafka.event

import com.fasterxml.jackson.annotation.JsonProperty
import kr.hhplus.be.server.domain.order.Order
import java.time.LocalDateTime

/**
 * Kafka로 전송되는 주문 완료 이벤트
 */
data class OrderCompletedKafkaEvent(
    @JsonProperty("orderId")
    val orderId: Long,

    @JsonProperty("userId")
    val userId: Long,

    @JsonProperty("orderLines")
    val orderLines: List<OrderLineKafkaInfo>,

    @JsonProperty("totalAmount")
    val totalAmount: Long,

    @JsonProperty("orderDateTime")
    val orderDateTime: String,

    @JsonProperty("eventType")
    val eventType: String = "ORDER_COMPLETED",

    @JsonProperty("timestamp")
    val timestamp: String = LocalDateTime.now().toString()
) {
    companion object {
        fun from(order: Order): OrderCompletedKafkaEvent {
            return OrderCompletedKafkaEvent(
                orderId = order.id,
                userId = order.userId,
                orderLines = order.orderLines.map {
                    OrderLineKafkaInfo(
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
 * 주문 라인 정보 (Kafka용)
 */
data class OrderLineKafkaInfo(
    @JsonProperty("productId")
    val productId: Long,

    @JsonProperty("quantity")
    val quantity: Long,

    @JsonProperty("price")
    val price: Long
) 