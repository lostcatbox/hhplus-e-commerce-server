package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.point.Point
import kr.hhplus.be.server.domain.point.PointRepository
import kr.hhplus.be.server.infra.persistance.jpa.PointJpaRepository
import kr.hhplus.be.server.infra.persistance.model.PointEntity
import org.springframework.stereotype.Repository

@Repository
class PointRepositoryImpl(
    private val pointJpaRepository: PointJpaRepository
) : PointRepository {
    override fun save(point: Point) {
        val entity = PointEntity.from(point)
        pointJpaRepository.save(entity)
    }

    override fun findByUserIdWithPessimisticLock(userId: Long): Point? {
        return pointJpaRepository.findByUserIdWithPessimisticLock(userId)?.toDomain()
    }

    override fun findByUserId(userId: Long): Point? {
        return pointJpaRepository.findByUserId(userId)?.toDomain()
    }
} 