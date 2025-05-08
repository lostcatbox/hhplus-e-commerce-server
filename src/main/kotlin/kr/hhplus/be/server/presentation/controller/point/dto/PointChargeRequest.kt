package kr.hhplus.be.server.presentation.controller.point.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import kr.hhplus.be.server.application.point.command.PointChargeCommand

data class PointChargeRequest(
    @field:NotNull
    @field:Min(1)
    val amount: Long
) {
    fun toCommand(userId: Long): PointChargeCommand {
        return PointChargeCommand(
            userId = userId,
            amount = amount
        )
    }
} 