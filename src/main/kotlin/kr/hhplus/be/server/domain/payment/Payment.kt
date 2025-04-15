package kr.hhplus.be.server.domain.payment

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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