package kr.hhplus.be.server.domain.order

import java.time.LocalDateTime

/**
 * OrderCriteria: 주문 처리를 위한 입력 데이터 클래스
 */
data class OrderCriteria(
    val userId: Long,
    val issuedCouponId: Long? = null,
    val orderLines: List<OrderLineCriteria>,
    val orderDateTime: LocalDateTime = LocalDateTime.now()
) {
    fun createOrder(): Order {
        TODO("Not yet implemented")
    }

    fun toOrder(): Order {
        return Order(
            id = 0L,
            userId = this.userId,
            issuedCouponId = this.issuedCouponId,
            orderLines = this.orderLines.map { 
                OrderLine(
                    productId = it.productId,
                    productPrice = 0L, // 상품 가격은 ProductService에서 조회하여 설정해야 함
                    quantity = it.quantity
                )
            },
            orderDateTime = this.orderDateTime,
            orderStatus = OrderStatus.주문_요청됨
        )
    }
}

/**
 * OrderLineCriteria: 주문 상품 정보를 담는 데이터 클래스
 */
data class OrderLineCriteria(
    val productId: Long,
    val quantity: Long
) 