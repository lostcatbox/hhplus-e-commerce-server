package kr.hhplus.be.server.application.coupon.command

data class IssueCouponCommand(
    val userId: Long,
    val couponId: Long
) 