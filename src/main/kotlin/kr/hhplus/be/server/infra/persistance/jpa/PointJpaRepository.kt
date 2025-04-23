package kr.hhplus.be.server.infra.persistance.jpa

import jakarta.persistence.LockModeType
import kr.hhplus.be.server.infra.persistance.model.PointEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PointJpaRepository : JpaRepository<PointEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM points p WHERE p.userId = :userId")
    fun findByUserIdWithPessimisticLock(@Param("userId") userId: Long): PointEntity?

    fun findByUserId(userId: Long): PointEntity?
} 