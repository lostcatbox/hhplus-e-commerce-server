package kr.hhplus.be.server.presentation.controller.coupon.dto

import kr.hhplus.be.server.domain.coupon.AmountCoupon
import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.PercentageCoupon

/**
 * 쿠폰 생성 요청 DTO
 */
data class CreateCouponRequest(
    val name: String,
    val stock: Long,
    val amount: Long? = null,
    val percent: Double? = null
)

/**
 * 쿠폰 발급 요청 DTO
 */
data class CouponIssueRequest(
    val couponId: Long,
    val userId: Long
)

/**
 * 쿠폰 발급 응답 DTO
 */
data class CouponIssueResponse(
    val success: Boolean,
    val message: String
)

/**
 * 쿠폰 정보 응답 DTO
 */
data class CouponResponse(
    val id: Long,
    val name: String,
    val stock: Long,
    val active: Boolean,
    val type: String,
    val value: Any
) {
    companion object {
        fun from(coupon: Coupon): CouponResponse {
            return when (coupon) {
                is AmountCoupon -> CouponResponse(
                    id = coupon.id,
                    name = coupon.name,
                    stock = coupon.stock,
                    active = coupon.active,
                    type = "AMOUNT",
                    value = coupon.amount
                )

                is PercentageCoupon -> CouponResponse(
                    id = coupon.id,
                    name = coupon.name,
                    stock = coupon.stock,
                    active = coupon.active,
                    type = "PERCENTAGE",
                    value = coupon.percent
                )

                else -> throw IllegalArgumentException("지원하지 않는 쿠폰 타입입니다.")
            }
        }
    }
} 