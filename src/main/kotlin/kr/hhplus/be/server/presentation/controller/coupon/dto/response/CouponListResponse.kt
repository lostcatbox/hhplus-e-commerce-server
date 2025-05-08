package kr.hhplus.be.server.presentation.controller.coupon.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.coupon.Coupon

@Schema(description = "쿠폰 목록 응답")
data class CouponListResponse(
    @Schema(description = "쿠폰 목록")
    val coupons: List<CouponResponse>
) {
    companion object {
        fun of(coupons: List<Coupon>): CouponListResponse {
            return CouponListResponse(
                coupons = coupons.map { CouponResponse.of(it) }
            )
        }
    }
}