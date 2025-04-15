package kr.hhplus.be.server.domain.order

import jakarta.persistence.*
import java.time.LocalDateTime


enum class OrderStatus {
    주문_요청됨,
    상품_준비중,
    결제_대기중,
    결제_완료,
    주문_실패
}

@Entity(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1L,
    val userId: Long,
    val issuedCouponId: Long? = null, // 주문에 사용될 쿠폰 정보
    @ElementCollection
    @CollectionTable(
        name = "order_lines",
        joinColumns = [JoinColumn(name = "order_id")]
    )
    val orderLines: List<OrderLine>,
    val orderDateTime: LocalDateTime,
    @Enumerated(EnumType.STRING)
    val orderStatus: OrderStatus = OrderStatus.주문_요청됨  // 주문요청됨, 상품준비중, 결제 대기중, 결제 완료, 주문실패
) {
    val totalPrice: Long = orderLines.sumOf { it.totalPrice }

    fun readyProduct(): Order {
        return Order(
            id = id,
            userId = userId,
            issuedCouponId = issuedCouponId,
            orderLines = orderLines,
            orderDateTime = orderDateTime,
            orderStatus = OrderStatus.상품_준비중
        )
    }

    fun readyPay(): Order {
        return Order(
            id = id,
            userId = userId,
            issuedCouponId = issuedCouponId,
            orderLines = orderLines,
            orderDateTime = orderDateTime,
            orderStatus = OrderStatus.결제_대기중
        )
    }

    fun finishPay(): Order {
        return Order(
            id = id,
            userId = userId,
            issuedCouponId = issuedCouponId,
            orderLines = orderLines,
            orderDateTime = orderDateTime,
            orderStatus = OrderStatus.결제_완료
        )
    }

    fun failOrder(): Order {
        return Order(
            id = id,
            userId = userId,
            issuedCouponId = issuedCouponId,
            orderLines = orderLines,
            orderDateTime = orderDateTime,
            orderStatus = OrderStatus.주문_실패
        )
    }
}

@Embeddable
class OrderLine(
    val productId: Long,
    val productPrice: Long,
    val quantity: Long,
) {
    val totalPrice: Long = productPrice * quantity
}

@Entity(name = "orderHistories")
class OrderHistory(
    @Id
    val id: Long = -1L,
    val orderId: Long,
    val userId: Long,
    val issuedCouponId: Long? = null, // 주문에 사용될 쿠폰 정보

    @ElementCollection
    @CollectionTable(
        name = "order_history_lines",
        joinColumns = [JoinColumn(name = "order_history_id")]
    )
    val orderLines: List<OrderLine>,

    val orderDateTime: LocalDateTime,
    val totalPrice: Long,
    val orderStatus: OrderStatus  // 주문요청됨, 상품준비중, 결제 대기중, 결제 완료, 주문실패
)