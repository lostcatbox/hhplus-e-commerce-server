package kr.hhplus.be.server.domain.point

import kr.hhplus.be.server.support.distributedlock.DistributedLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointService(
    private val pointRepository: PointRepository
) {

    fun getPoint(userId: Long): Point {
        return pointRepository.findByUserId(userId) ?: Point.EMPTY(userId)
    }

    @DistributedLock(key = "point_lock")
    @Transactional
    fun usePoint(userId: Long, useAmount: Long): Point {
        // 분산락을 적용했으므로 비관적 락을 사용하지 않아도 됨
        val point = pointRepository.findByUserId(userId) ?: Point.EMPTY(userId)
        val updatedPoint = point.usePoint(useAmount)
        return pointRepository.save(updatedPoint)
    }

    @DistributedLock(key = "point_lock")
    @Transactional
    fun chargePoint(userId: Long, chargeAmount: Long): Point {
        // 분산락을 적용했으므로 비관적 락을 사용하지 않아도 됨
        val point = pointRepository.findByUserId(userId) ?: Point.EMPTY(userId)
        val updatedPoint = point.chargePoint(chargeAmount)
        return pointRepository.save(updatedPoint)
    }
}