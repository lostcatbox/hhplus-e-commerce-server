package kr.hhplus.be.server.presentation.controller.coupon.dto.response

import kr.hhplus.be.server.domain.coupon.AmountCoupon
import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.PercentageCoupon

data class CouponResponse(
    val couponId: Long,
    val name: String,
    val type: CouponType,
    val discountValue: Number,
    val isActive: Boolean,
    val remainingStock: Long
) {
    companion object {
        fun of(coupon: Coupon): CouponResponse {
            return when (coupon) {
                is AmountCoupon -> CouponResponse(
                    couponId = coupon.id,
                    name = coupon.name,
                    type = CouponType.AMOUNT,
                    discountValue = coupon.amount.toDouble(),
                    isActive = coupon.active,
                    remainingStock = coupon.stock
                )

                is PercentageCoupon -> CouponResponse(
                    couponId = coupon.id,
                    name = coupon.name,
                    type = CouponType.PERCENTAGE,
                    discountValue = coupon.percent,
                    isActive = coupon.active,
                    remainingStock = coupon.stock
                )

                else -> throw IllegalArgumentException("Unsupported coupon type")
            }
        }
    }
}

enum class CouponType {
    AMOUNT,
    PERCENTAGE
} 