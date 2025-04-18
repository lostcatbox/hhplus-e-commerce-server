package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.point.Point
import kr.hhplus.be.server.domain.point.PointRepository
import kr.hhplus.be.server.infra.persistance.jpa.PointJpaRepository
import org.springframework.stereotype.Repository

@Repository
class PointRepositoryImpl(
    private val pointJpaRepository: PointJpaRepository
) : PointRepository {
    override fun save(point: Point) {
        pointJpaRepository.save(point)
    }

    override fun findByUserIdWithPessimisticLock(userId: Long): Point? {
        return pointJpaRepository.findByUserIdWithPessimisticLock(userId)
    }

    override fun findByUserId(userId: Long): Point? {
        return pointJpaRepository.findByUserId(userId)
    }
} 