package kr.hhplus.be.server.infra.persistance.model

import jakarta.persistence.*
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentStatus
import java.time.LocalDateTime

@Entity(name = "payments")
class PaymentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    
    val orderId: Long,
    
    val userId: Long,
    
    val payAmount: Long,
    
    @Enumerated(EnumType.STRING)
    val status: PaymentStatus = PaymentStatus.PENDING,
    
    val remainPointAmount: Long? = null,
    
    val couponId: Long? = null,
    
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    // 도메인 모델로 변환
    fun toDomain(): Payment {
        return Payment(
            id = this.id,
            orderId = this.orderId,
            userId = this.userId,
            payAmount = this.payAmount,
            status = this.status,
            remainPointAmount = this.remainPointAmount,
            couponId = this.couponId,
            createdAt = this.createdAt
        )
    }
    
    companion object {
        // 도메인 모델로부터 엔티티 생성
        fun from(domain: Payment): PaymentEntity {
            return PaymentEntity(
                id = domain.id,
                orderId = domain.orderId,
                userId = domain.userId,
                payAmount = domain.payAmount,
                status = domain.status,
                remainPointAmount = domain.remainPointAmount,
                couponId = domain.couponId,
                createdAt = domain.createdAt
            )
        }
    }
} 