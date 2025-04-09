package kr.hhplus.be.server.domain.service.payment

import kr.hhplus.be.server.domain.model.Order
import kr.hhplus.be.server.domain.model.Payment
import kr.hhplus.be.server.domain.model.PaymentStatus
import kr.hhplus.be.server.domain.model.Point
import kr.hhplus.be.server.domain.port.out.PaymentHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    val paymentHistoryRepository: PaymentHistoryRepository
) {
    @Transactional
    fun processPayment(order: Order, point: Point, finalAmount: Long) {
        // 포인트 결제 처리
        val usedPoint = point.usePoint(order.userId)

        // 결제 정보 생성
        val payment = Payment(
            id = -1L, // DB에서 생성
            orderId = order.id,
            userId = order.userId,
            amount = finalAmount,
            status = PaymentStatus.COMPLETED,
            remainPointAmount = usedPoint.amount,
            couponId = order.issuedCouponId
        )
        paymentHistoryRepository.save(payment)
    }
}