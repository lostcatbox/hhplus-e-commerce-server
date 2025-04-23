package kr.hhplus.be.server.domain.point

import org.springframework.stereotype.Repository

@Repository
interface PointRepository {
    fun findByUserId(userId: Long): Point?
    fun save(point: Point): Point
    fun findByUserIdWithPessimisticLock(userId: Long): Point?
}