package kr.hhplus.be.server.presentation.controller.coupon.dto.request

import kr.hhplus.be.server.application.coupon.command.IssueCouponCommand
import jakarta.validation.constraints.NotNull

data class CouponIssueRequest(
    @field:NotNull(message = "유저 ID는 필수입니다.")
    val userId: Long,
    
    @field:NotNull(message = "쿠폰 ID는 필수입니다.")
    val couponId: Long
) {
    fun toCommand(): IssueCouponCommand {
        return IssueCouponCommand(
            userId = userId,
            couponId = couponId
        )
    }
}