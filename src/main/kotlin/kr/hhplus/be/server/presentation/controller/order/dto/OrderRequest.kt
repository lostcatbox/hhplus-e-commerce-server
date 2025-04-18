package kr.hhplus.be.server.presentation.controller.order.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class OrderRequest(
    @field:NotNull
    val userId: Long,
    @field:NotNull
    val couponId: Long?,
    @field:NotNull
    val orderLines: List<OrderLine>
)

data class OrderLine(
    @field:NotNull
    val productId: Long,

    @field:NotNull
    @field:Min(1)
    val quantity: Int,
)