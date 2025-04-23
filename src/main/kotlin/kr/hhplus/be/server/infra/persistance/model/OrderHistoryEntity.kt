package kr.hhplus.be.server.infra.persistance.model

import jakarta.persistence.*
import kr.hhplus.be.server.domain.order.OrderHistory
import kr.hhplus.be.server.domain.order.OrderStatus
import java.time.LocalDateTime

@Entity(name = "orderHistories")
class OrderHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    
    val orderId: Long,
    
    val userId: Long,
    
    val issuedCouponId: Long? = null,
    
    val orderDateTime: LocalDateTime,
    
    val totalPrice: Long,
    
    @Enumerated(EnumType.STRING)
    val orderStatus: OrderStatus
) {
    // 도메인 모델로 변환
    fun toDomain(): OrderHistory {
        return OrderHistory(
            id = this.id,
            orderId = this.orderId,
            userId = this.userId,
            issuedCouponId = this.issuedCouponId,
            orderDateTime = this.orderDateTime,
            totalPrice = this.totalPrice,
            orderStatus = this.orderStatus
        )
    }
    
    companion object {
        // 도메인 모델로부터 엔티티 생성
        fun from(domain: OrderHistory): OrderHistoryEntity {
            return OrderHistoryEntity(
                id = domain.id,
                orderId = domain.orderId,
                userId = domain.userId,
                issuedCouponId = domain.issuedCouponId,
                orderDateTime = domain.orderDateTime,
                totalPrice = domain.totalPrice,
                orderStatus = domain.orderStatus
            )
        }
    }
} 