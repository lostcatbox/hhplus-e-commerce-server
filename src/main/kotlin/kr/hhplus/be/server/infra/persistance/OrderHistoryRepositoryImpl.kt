package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.order.OrderHistory
import kr.hhplus.be.server.domain.order.OrderHistoryRepository
import kr.hhplus.be.server.infra.persistance.jpa.OrderHistoryJpaRepository
import kr.hhplus.be.server.infra.persistance.model.OrderHistoryEntity
import org.springframework.stereotype.Repository

@Repository
class OrderHistoryRepositoryImpl(
    private val orderHistoryJpaRepository: OrderHistoryJpaRepository
) : OrderHistoryRepository {
    override fun save(orderHistory: OrderHistory): OrderHistory {
        val entity = OrderHistoryEntity.from(orderHistory)
        val savedEntity = orderHistoryJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findByOrderId(orderId: Long): List<OrderHistory> {
        return orderHistoryJpaRepository.findByOrderId(orderId).map { it.toDomain() }
    }
} 