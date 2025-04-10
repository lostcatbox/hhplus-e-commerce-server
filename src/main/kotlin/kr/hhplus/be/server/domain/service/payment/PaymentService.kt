package kr.hhplus.be.server.domain.service.payment

import kr.hhplus.be.server.domain.model.Coupon
import kr.hhplus.be.server.domain.model.Order
import kr.hhplus.be.server.domain.model.Payment
import kr.hhplus.be.server.domain.model.PaymentStatus
import kr.hhplus.be.server.domain.port.out.PaymentHistoryRepository
import kr.hhplus.be.server.domain.service.point.PointService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// DownStream = PaymentService <-- UpStream = PointService
@Service
class PaymentService(
    val pointService: PointService,
    val paymentHistoryRepository: PaymentHistoryRepository
) {
    @Transactional
    fun pay(order: Order, coupon: Coupon?) {
        val point = pointService.getPoint(order.userId)
        var finalPayAmount = order.totalPrice

        // 쿠폰 존재 시 사용
        if (coupon != null) {
            finalPayAmount = coupon.discountAmount(finalPayAmount)
        }

        // 포인트 결제 처리
        val usedPoint = point.usePoint(finalPayAmount)

        // 결제 정보 생성
        val payment = Payment(
            id = -1L,
            orderId = order.id,
            userId = order.userId,
            payAmount = finalPayAmount,
            status = PaymentStatus.COMPLETED,
            remainPointAmount = usedPoint.amount,
            couponId = order.issuedCouponId
        )
        paymentHistoryRepository.save(payment)
    }
}