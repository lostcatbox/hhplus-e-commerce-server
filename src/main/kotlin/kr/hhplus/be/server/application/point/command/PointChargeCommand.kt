package kr.hhplus.be.server.application.point.command

data class PointChargeCommand(
    val userId: Long,
    val amount: Long
) 