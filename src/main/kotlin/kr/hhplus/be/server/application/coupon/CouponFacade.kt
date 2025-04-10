package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.model.Coupon
import kr.hhplus.be.server.domain.service.coupon.CouponService
import kr.hhplus.be.server.domain.service.user.UserService
import org.springframework.stereotype.Service

@Service
class CouponFacade(
    private val userService: UserService,
    private val couponService: CouponService
) {
    fun getUserCouponList(userId: Long): List<Coupon> {
        userService.checkActiveUser(userId)
        return couponService.getUserCouponList(userId)
    }

    fun issuedCouponTo(userId: Long, couponId: Long) {
        userService.checkActiveUser(userId)
        couponService.issudCoupon(userId, couponId)
    }
}