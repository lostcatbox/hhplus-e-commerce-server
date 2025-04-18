package kr.hhplus.be.server.domain.order

import org.springframework.stereotype.Repository

@Repository
interface OrderRepository {
    fun save(order: Order): Order
    fun findByIdWithPessimisticLock(orderId: Long): Order?
    fun findById(orderId: Long): Order?
}