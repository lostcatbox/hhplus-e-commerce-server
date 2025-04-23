package kr.hhplus.be.server.infra.persistance.model

import jakarta.persistence.*
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderLine
import kr.hhplus.be.server.domain.order.OrderStatus
import java.time.LocalDateTime

@Entity(name = "orders")
class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    
    val userId: Long,
    
    val issuedCouponId: Long? = null,
    
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "order_id")
    val orderLineEntities: MutableList<OrderLineEntity> = mutableListOf(),
    
    val orderDateTime: LocalDateTime,
    
    @Enumerated(EnumType.STRING)
    val orderStatus: OrderStatus = OrderStatus.주문_요청됨
) {
    val totalPrice: Long = orderLineEntities.sumOf { it.totalPrice }
    
    // 도메인 모델로 변환
    fun toDomain(): Order {
        return Order(
            id = this.id,
            userId = this.userId,
            issuedCouponId = this.issuedCouponId,
            orderLines = this.orderLineEntities.map { it.toDomain() },
            orderDateTime = this.orderDateTime,
            orderStatus = this.orderStatus
        )
    }
    
    companion object {
        // 도메인 모델로부터 엔티티 생성
        fun from(domain: Order): OrderEntity {
            return OrderEntity(
                id = domain.id,
                userId = domain.userId,
                issuedCouponId = domain.issuedCouponId,
                orderLineEntities = domain.orderLines.map { OrderLineEntity.from(it) }.toMutableList(),
                orderDateTime = domain.orderDateTime,
                orderStatus = domain.orderStatus
            )
        }
    }
}

@Entity(name = "order_lines")
class OrderLineEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    
    val orderId: Long? = null,
    
    val productId: Long,
    
    val productPrice: Long,
    
    val quantity: Long
) {
    val totalPrice: Long = productPrice * quantity
    
    // 도메인 모델로 변환
    fun toDomain(): OrderLine {
        return OrderLine(
            productId = this.productId,
            productPrice = this.productPrice,
            quantity = this.quantity
        )
    }
    
    companion object {
        // 도메인 모델로부터 엔티티 생성
        fun from(domain: OrderLine): OrderLineEntity {
            return OrderLineEntity(
                productId = domain.productId,
                productPrice = domain.productPrice,
                quantity = domain.quantity
            )
        }
    }
} 