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
    var id: Long = 0L,
    var userId: Long,
    var issuedCouponId: Long? = null, // 주문에 사용될 쿠폰 정보
    @ElementCollection
    @CollectionTable(
        name = "order_lines",
        joinColumns = [JoinColumn(name = "order_id")]
    )
    var orderLines: MutableList<OrderLine>,
    var orderDateTime: LocalDateTime,
    @Enumerated(EnumType.STRING)
    var orderStatus: OrderStatus = OrderStatus.주문_요청됨  // 주문요청됨, 상품준비중, 결제 대기중, 결제 완료, 주문실패
) {
    var totalPrice: Long = orderLines.sumOf { it.totalPrice }

    fun readyProduct() {

        orderStatus = OrderStatus.상품_준비중

    }

    fun readyPay() {
        orderStatus = OrderStatus.결제_대기중
    }

    fun finishPay() {
        orderStatus = OrderStatus.결제_완료
    }

    fun failOrder() {
        orderStatus = OrderStatus.주문_실패
    }
}

@Embeddable
class OrderLine(
    var productId: Long,
    var productPrice: Long,
    var quantity: Long,
) {
    var totalPrice: Long = productPrice * quantity
}

@Entity(name = "orderHistories")
class OrderHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,
    var orderId: Long,
    var userId: Long,
    var issuedCouponId: Long? = null, // 주문에 사용될 쿠폰 정보
    var orderDateTime: LocalDateTime,
    var totalPrice: Long,
    var orderStatus: OrderStatus  // 주문요청됨, 상품준비중, 결제 대기중, 결제 완료, 주문실패
)