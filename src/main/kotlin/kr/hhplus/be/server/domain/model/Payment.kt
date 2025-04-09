package kr.hhplus.be.server.domain.model

import java.time.LocalDateTime

data class Payment(
    val id: Long,
    val orderId: Long,
    val userId: Long,
    val amount: Long,
    val status: PaymentStatus,
    val remainPointAmount: Long? = null,
    val couponId: Long? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED
} 