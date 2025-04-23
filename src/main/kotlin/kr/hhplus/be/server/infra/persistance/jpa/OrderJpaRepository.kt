package kr.hhplus.be.server.infra.persistance.jpa

import jakarta.persistence.LockModeType
import kr.hhplus.be.server.infra.persistance.model.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OrderJpaRepository : JpaRepository<OrderEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM orders o WHERE o.id = :orderId")
    fun findByIdWithPessimisticLock(@Param("orderId") orderId: Long): OrderEntity?
} 