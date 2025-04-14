package kr.hhplus.be.server.domain.payment

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "payments")
data class Payment(
    @Id
    val id: Long,
    val orderId: Long,
    val userId: Long,
    val payAmount: Long,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val remainPointAmount: Long? = null,
    val couponId: Long? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED
} 