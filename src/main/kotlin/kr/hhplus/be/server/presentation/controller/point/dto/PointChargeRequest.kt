package kr.hhplus.be.server.presentation.controller.point.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class PointChargeRequest(
    @field:NotNull
    @field:Min(1)
    val amount: Long
) 