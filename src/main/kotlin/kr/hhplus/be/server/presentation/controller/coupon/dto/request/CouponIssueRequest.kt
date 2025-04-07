package kr.hhplus.be.server.presentation.controller.coupon.dto.request

import jakarta.validation.constraints.NotNull

data class CouponIssueRequest(
    @field:NotNull
    val userId: Long
)