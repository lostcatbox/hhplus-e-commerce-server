package kr.hhplus.be.server.infra.persistance.jpa

import kr.hhplus.be.server.infra.persistance.model.OrderHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderHistoryJpaRepository : JpaRepository<OrderHistoryEntity, Long> {
    fun findByOrderId(orderId: Long): List<OrderHistoryEntity>
} 