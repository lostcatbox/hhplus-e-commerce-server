package kr.hhplus.be.server.presentation.controller.point.dto

import kr.hhplus.be.server.domain.point.Point

data class PointResponse(
    val userId: Long,
    val amount: Long
) {
    companion object {
        fun of(point: Point): PointResponse {
            return PointResponse(
                userId = point.userId,
                amount = point.amount
            )
        }
    }
} 