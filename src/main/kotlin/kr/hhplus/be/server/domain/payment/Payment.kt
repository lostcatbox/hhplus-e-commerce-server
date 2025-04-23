package kr.hhplus.be.server.domain.payment

import java.time.LocalDateTime

// 순수 도메인 모델로 변경
class Payment(
    val id: Long = 0L,
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