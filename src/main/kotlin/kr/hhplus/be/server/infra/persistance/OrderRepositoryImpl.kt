package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderRepository
import kr.hhplus.be.server.infra.persistance.jpa.OrderJpaRepository
import kr.hhplus.be.server.infra.persistance.model.OrderEntity
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class OrderRepositoryImpl(
    private val orderJpaRepository: OrderJpaRepository
) : OrderRepository {
    override fun save(order: Order): Order {
        val orderEntity = OrderEntity.from(order)
        val savedEntity = orderJpaRepository.save(orderEntity)
        return savedEntity.toDomain()
    }

    override fun findByIdWithPessimisticLock(orderId: Long): Order? {
        return orderJpaRepository.findByIdWithPessimisticLock(orderId)?.toDomain()
    }

    override fun findById(orderId: Long): Order? {
        return orderJpaRepository.findById(orderId).getOrNull()?.toDomain()
    }

    override fun findAll(): List<Order> {
        return orderJpaRepository.findAll().map { it.toDomain() }
    }
}