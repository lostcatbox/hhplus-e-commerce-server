package kr.hhplus.be.server.domain.point

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointService(
    private val pointRepository: PointRepository
) {

    fun getPoint(userId: Long): Point {
        return pointRepository.findByUserId(userId) ?: Point.EMPTY(userId)
    }

    @Transactional
    fun usePoint(userId: Long, useAmount: Long): Point {
        // 비관적 락을 사용하여 포인트 조회
        val point = pointRepository.findByUserIdWithPessimisticLock(userId) ?: Point.EMPTY(userId)
        val updatedPoint = point.usePoint(useAmount)
        return pointRepository.save(updatedPoint)
    }

    @Transactional
    fun chargePoint(userId: Long, chargeAmount: Long): Point {
        // 비관적 락을 사용하여 포인트 조회
        val point = pointRepository.findByUserIdWithPessimisticLock(userId) ?: Point.EMPTY(userId)
        val updatedPoint = point.chargePoint(chargeAmount)
        return pointRepository.save(updatedPoint)
    }
}