package kr.hhplus.be.server.domain.point

import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointRepository: PointRepository
) {

    fun getPoint(userId: Long): Point {
        return pointRepository.findById(userId) ?: Point.EMPTY(userId)
    }


    fun usePoint(userId: Long, useAmount: Long) {
        val point = pointRepository.findById(userId) ?: Point.EMPTY(userId)
        val usedPoint = point.usePoint(useAmount)
        pointRepository.save(usedPoint)
    }

    fun chargePoint(userId: Long, chargeAmount: Long) {
        val point = pointRepository.findById(userId) ?: Point.EMPTY(userId)
        val chargedPoint = point.chargePoint(chargeAmount)
        pointRepository.save(chargedPoint)
    }
}