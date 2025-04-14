package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderRepository
import kr.hhplus.be.server.infra.persistance.jpa.OrderJpaRepository
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class OrderRepositoryImpl(
    private val orderJpaRepository: OrderJpaRepository
) : OrderRepository {
    override fun save(order: Order): Order {
        return orderJpaRepository.save(order)
    }

    override fun findByIdWithPessimisticLock(orderId: Long): Order? {
        return orderJpaRepository.findByIdWithPessimisticLock(orderId)
    }

    override fun findById(orderId: Long): Order? {
        return orderJpaRepository.findById(orderId).getOrNull()
    }
} 