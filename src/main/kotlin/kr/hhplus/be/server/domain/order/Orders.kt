package kr.hhplus.be.server.domain.order

import java.time.LocalDateTime


enum class OrderStatus {
    주문_요청됨,
    상품_준비중,
    결제_대기중,
    결제_완료,
    주문_실패
}

data class Order(
    val id: Long = -1L,
    val userId: Long,
    val issuedCouponId: Long? = null, // 주문에 사용될 쿠폰 정보
    val orderLines: List<OrderLine>,
    val orderDateTime: LocalDateTime,
    val orderStatus: OrderStatus = OrderStatus.주문_요청됨  // 주문요청됨, 상품준비중, 결제 대기중, 결제 완료, 주문실패
) {
    val totalPrice: Long = orderLines.sumOf { it.totalPrice }

    fun readyProduct(): Order {
        return this.copy(
            orderStatus = OrderStatus.상품_준비중
        )
    }

    fun readyPay(): Order {
        return this.copy(
            orderStatus = OrderStatus.결제_대기중
        )
    }

    fun finishPay(): Order {
        return this.copy(
            orderStatus = OrderStatus.결제_완료
        )
    }

    fun failOrder(): Order {
        return this.copy(
            orderStatus = OrderStatus.주문_실패
        )
    }
}

data class OrderLine(
    val orderId: Long,
    val productId: Long,
    val productPrice: Long,
    val quantity: Long,
) {
    val totalPrice: Long = productPrice * quantity
}

data class OrderHistory(
    val id: Long = -1L,
    val orderId: Long,
    val userId: Long,
    val issuedCouponId: Long? = null, // 주문에 사용될 쿠폰 정보
    val orderLines: List<OrderLine>,
    val orderDateTime: LocalDateTime,
    val totalPrice: Long,
    val orderStatus: OrderStatus  // 주문요청됨, 상품준비중, 결제 대기중, 결제 완료, 주문실패
)