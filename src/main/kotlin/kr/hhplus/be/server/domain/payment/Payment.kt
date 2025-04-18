package kr.hhplus.be.server.domain.payment

import java.time.LocalDateTime

data class Payment(
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