package kr.hhplus.be.server.presentation.controller.order.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import kr.hhplus.be.server.domain.order.OrderCriteria
import kr.hhplus.be.server.domain.order.OrderLineCriteria

data class OrderRequest(
    @field:NotNull
    val userId: Long,
    @field:NotNull
    val couponId: Long?,
    @field:NotNull
    val orderLines: List<OrderLine>
) {
    fun toCommand(): OrderCriteria {
        return OrderCriteria(
            userId = userId,
            issuedCouponId = couponId,
            orderLines = orderLines.map { 
                OrderLineCriteria(
                    productId = it.productId,
                    quantity = it.quantity.toLong()
                )
            }
        )
    }
}

data class OrderLine(
    @field:NotNull
    val productId: Long,

    @field:NotNull
    @field:Min(1)
    val quantity: Int,
)