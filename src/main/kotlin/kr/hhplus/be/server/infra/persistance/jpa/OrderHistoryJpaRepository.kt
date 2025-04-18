package kr.hhplus.be.server.infra.persistance.jpa

import kr.hhplus.be.server.domain.order.OrderHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderHistoryJpaRepository : JpaRepository<OrderHistory, Long> {
    fun findByOrderId(orderId: Long): List<OrderHistory>
} 