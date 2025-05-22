package kr.hhplus.be.server.domain.coupon

interface IssuedCouponRepository {
    fun save(issuedCoupon: IssuedCoupon): IssuedCoupon
    fun findById(id: Long): IssuedCoupon?
} 