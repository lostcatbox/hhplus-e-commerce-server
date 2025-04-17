package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.point.PointService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// DownStream = PaymentService <-- UpStream = PointService
@Service
class PaymentService(
    val pointService: PointService,
    val paymentRepository: PaymentRepository
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
        point.usePoint(finalPayAmount)

        // 결제 정보 생성
        val payment = Payment(
            orderId = order.id,
            userId = order.userId,
            payAmount = finalPayAmount,
            status = PaymentStatus.COMPLETED,
            remainPointAmount = point.amount,
            couponId = order.issuedCouponId
        )
        paymentRepository.save(payment)
    }
}