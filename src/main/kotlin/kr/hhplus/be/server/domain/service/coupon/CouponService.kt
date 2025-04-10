package kr.hhplus.be.server.domain.service.coupon

import kr.hhplus.be.server.domain.model.Coupon
import kr.hhplus.be.server.domain.model.IssuedCoupon
import kr.hhplus.be.server.domain.port.out.CouponRepository
import org.springframework.stereotype.Service

@Service
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

    fun findByIssuedCouponId(issuedCouponId: Long): IssuedCouponAndCoupon {
        val issuedCouponById = couponRepository.findIssuedCouponById(issuedCouponId)
        val coupon = couponRepository.findById(issuedCouponById.couponId)
        return IssuedCouponAndCoupon(
            issuedCoupon = issuedCouponById,
            coupon = coupon
        )
    }

    fun useIssuedCoupon(issuedCouponId: Long?): Coupon? {
        if (issuedCouponId == null) {
            return null
        }
        val issuedCouponById = couponRepository.findIssuedCouponById(issuedCouponId)
        issuedCouponById.useCoupon()
        return couponRepository.findById(issuedCouponById.couponId)
    }


}

data class IssuedCouponAndCoupon(
    val issuedCoupon: IssuedCoupon,
    val coupon: Coupon
)