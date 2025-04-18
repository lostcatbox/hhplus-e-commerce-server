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
    var id: Long = 0L,
    var orderId: Long,
    var userId: Long,
    var payAmount: Long,
    var status: PaymentStatus = PaymentStatus.PENDING,
    var remainPointAmount: Long? = null,
    var couponId: Long? = null,
    var createdAt: LocalDateTime = LocalDateTime.now()
)

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED
} 