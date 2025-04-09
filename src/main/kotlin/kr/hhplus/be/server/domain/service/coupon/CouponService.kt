package kr.hhplus.be.server.domain.service.coupon

import kr.hhplus.be.server.domain.model.Coupon
import kr.hhplus.be.server.domain.port.out.CouponRepository

class CouponService(
    private val couponRepository: CouponRepository
) {
    fun getUserCouponList(userId: Long): List<Coupon> {
        return couponRepository.findAllByUserId(userId)
    }

    fun issudCoupon(userId: Long, couponId: Long) {
        val coupon = couponRepository.findById(couponId)
        val issuedCouponAndCoupon = coupon.issueTo(userId)
        couponRepository.save(issuedCouponAndCoupon.remainingCoupon)
        couponRepository.save(issuedCouponAndCoupon.issuedCoupon)
    }
}