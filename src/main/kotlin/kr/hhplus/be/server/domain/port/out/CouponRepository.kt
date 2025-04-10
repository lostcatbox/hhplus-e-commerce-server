package kr.hhplus.be.server.domain.port.out

import kr.hhplus.be.server.domain.model.Coupon
import kr.hhplus.be.server.domain.model.IssueCouponAndIssuedCoupon
import kr.hhplus.be.server.domain.model.IssuedCoupon
import org.springframework.stereotype.Repository

@Repository
interface CouponRepository {
    fun findAllByUserId(userId: Long): List<Coupon>
    fun findById(couponId: Long): Coupon
    fun save(issuedCoupon: IssuedCoupon)
    fun save(coupon: Coupon)
    fun findIssuedCouponById(issuedCouponId: Long): IssuedCoupon
    fun save(issuedCouponAndCoupon: IssueCouponAndIssuedCoupon)
}