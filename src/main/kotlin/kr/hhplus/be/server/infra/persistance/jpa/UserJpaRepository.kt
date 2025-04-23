package kr.hhplus.be.server.infra.persistance.jpa

import jakarta.persistence.LockModeType
import kr.hhplus.be.server.infra.persistance.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM users u WHERE u.id = :userId")
    fun findByIdWithPessimisticLock(@Param("userId") userId: Long): UserEntity?
} 