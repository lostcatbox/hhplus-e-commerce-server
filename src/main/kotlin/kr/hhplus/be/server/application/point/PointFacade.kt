package kr.hhplus.be.server.application.point

import kr.hhplus.be.server.domain.model.Point
import kr.hhplus.be.server.domain.service.point.PointService
import kr.hhplus.be.server.domain.service.user.UserService

class PointFacade(
    private val userService: UserService,
    private val pointService: PointService
) {
    fun getPoint(userId: Long): Point {
        userService.checkActiveUser(userId)
        return pointService.getPoint(userId)
    }

    fun usePoint(userId: Long, useAmount: Long) {
        userService.checkActiveUser(userId)
        return pointService.usePoint(userId, useAmount)
    }

    fun chargePoint(userId: Long, chargeAmount: Long) {
        userService.checkActiveUser(userId)
        pointService.chargePoint(userId, chargeAmount)
    }
}