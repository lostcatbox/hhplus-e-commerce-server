package kr.hhplus.be.server.domain.point

import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointRepository: PointRepository
) {

    fun getPoint(userId: Long): Point {
        return pointRepository.findByUserId(userId) ?: Point.EMPTY(userId)
    }


    fun usePoint(userId: Long, useAmount: Long) {
        val point = pointRepository.findByUserId(userId) ?: Point.EMPTY(userId)
        val updatedPoint = point.usePoint(useAmount)
        pointRepository.save(updatedPoint)
    }

    fun chargePoint(userId: Long, chargeAmount: Long) {
        val point = pointRepository.findByUserId(userId) ?: Point.EMPTY(userId)
        val updatedPoint = point.chargePoint(chargeAmount)
        pointRepository.save(updatedPoint)
    }
}