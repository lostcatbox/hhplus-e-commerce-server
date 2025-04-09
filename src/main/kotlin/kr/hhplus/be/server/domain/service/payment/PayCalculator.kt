package kr.hhplus.be.server.domain.service.payment

import kr.hhplus.be.server.domain.model.Order
import kr.hhplus.be.server.domain.model.OrderLine
import kr.hhplus.be.server.domain.service.coupon.IssuedCouponAndCoupon
import org.springframework.stereotype.Service

@Service
class PayCalculator {
    fun calculateTotalAmount(orderLines: List<OrderLine>): Long {
        return orderLines.sumOf { it.totalPrice }
    }

    fun calculateFinalAmount(order: Order, issuedCouponAndCoupon: IssuedCouponAndCoupon): Long {
        val totalAmount = calculateTotalAmount(order.orderLines)
        val discountAmount = issuedCouponAndCoupon.coupon.discountAmount(totalAmount)
        issuedCouponAndCoupon.issuedCoupon.useCoupon()
        return discountAmount
    }
} 