package kr.hhplus.be.server.domain.coupon

import org.springframework.stereotype.Repository

@Repository
interface CouponRepository {
    fun findAllByUserId(userId: Long): List<Coupon>
    fun findById(couponId: Long): Coupon
    fun save(issuedCoupon: IssuedCoupon): IssuedCoupon
    fun save(coupon: Coupon): Coupon
    fun findIssuedCouponById(issuedCouponId: Long): IssuedCoupon
    fun save(issuedCouponAndCoupon: IssueCouponAndIssuedCoupon)
    fun findByIdWithPessimisticLock(id: Long): Coupon
}