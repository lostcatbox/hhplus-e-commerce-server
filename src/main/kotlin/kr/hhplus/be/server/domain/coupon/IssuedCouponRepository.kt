package kr.hhplus.be.server.domain.coupon

interface IssuedCouponRepository {
    fun save(issuedCoupon: IssuedCoupon): IssuedCoupon
    fun findById(id: Long): IssuedCoupon?
    fun findByUserId(userId: Long): List<IssuedCoupon>
    fun findByCouponId(couponId: Long): List<IssuedCoupon>
    fun findByCouponIdAndUserId(couponId: Long, userId: Long): IssuedCoupon?
} 