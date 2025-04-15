package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.order.OrderHistory
import kr.hhplus.be.server.domain.order.OrderHistoryRepository
import kr.hhplus.be.server.infra.persistance.jpa.OrderHistoryJpaRepository
import org.springframework.stereotype.Repository

@Repository
class OrderHistoryRepositoryImpl(
    private val orderHistoryJpaRepository: OrderHistoryJpaRepository
) : OrderHistoryRepository {
    override fun save(orderHistory: OrderHistory): OrderHistory {
        return orderHistoryJpaRepository.save(orderHistory)
    }

    override fun findByOrderId(orderId: Long): List<OrderHistory> {
        return orderHistoryJpaRepository.findByOrderId(orderId = orderId)
    }
} 