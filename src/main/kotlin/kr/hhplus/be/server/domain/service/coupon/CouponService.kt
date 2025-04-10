package kr.hhplus.be.server.domain.service.coupon

import kr.hhplus.be.server.domain.model.Coupon
import kr.hhplus.be.server.domain.model.IssuedCoupon
import kr.hhplus.be.server.domain.port.out.CouponRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponService(
    private val couponRepository: CouponRepository
) {
    fun getUserCouponList(userId: Long): List<Coupon> {
        return couponRepository.findAllByUserId(userId)
    }

    @Transactional
    fun issuedCoupon(userId: Long, couponId: Long) {
        // 비관적 락을 사용하여 쿠폰 조회
        val coupon = couponRepository.findByIdWithPessimisticLock(couponId)
        val issuedCouponAndCoupon = coupon.issueTo(userId)
        couponRepository.save(issuedCouponAndCoupon)
    }

    fun findByIssuedCouponId(issuedCouponId: Long): IssuedCouponAndCouponVO {
        val issuedCouponById = couponRepository.findIssuedCouponById(issuedCouponId)
        val coupon = couponRepository.findById(issuedCouponById.couponId)
        return IssuedCouponAndCouponVO(
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

data class IssuedCouponAndCouponVO(
    val issuedCoupon: IssuedCoupon,
    val coupon: Coupon
)