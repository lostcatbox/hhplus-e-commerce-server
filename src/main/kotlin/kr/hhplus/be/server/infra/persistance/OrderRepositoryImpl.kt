package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderLine
import kr.hhplus.be.server.domain.order.OrderRepository
import kr.hhplus.be.server.infra.persistance.jpa.OrderJpaRepository
import kr.hhplus.be.server.infra.persistance.jpa.OrderLineJpaRepository
import kr.hhplus.be.server.infra.persistance.model.OrderEntity
import kr.hhplus.be.server.infra.persistance.model.OrderLineEntity
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class OrderRepositoryImpl(
    private val orderJpaRepository: OrderJpaRepository,
    private val orderLineJpaRepository: OrderLineJpaRepository
) : OrderRepository {
    override fun save(order: Order): Order {
        // 1. Order 엔티티 저장
        val orderEntity = OrderEntity.from(order)
        val savedOrderEntity = orderJpaRepository.save(orderEntity)
        
        // 2. 주문 라인 엔티티들 저장 (orderId 설정)
        val orderLineEntities = order.orderLines.map { 
            val entity = OrderLineEntity.from(it)
            // entity의 orderId 필드 설정
            OrderLineEntity(
                id = entity.id,
                orderId = savedOrderEntity.id,
                productId = entity.productId,
                productPrice = entity.productPrice,
                quantity = entity.quantity
            )
        }
        orderLineJpaRepository.saveAll(orderLineEntities)
        
        // 3. 저장된 주문 라인 조회 및 도메인 모델로 변환
        val savedOrderLines = getOrderLinesForOrder(savedOrderEntity.id)
        
        // 4. 완성된 도메인 모델 반환
        return savedOrderEntity.toDomain(savedOrderLines)
    }

    override fun findByIdWithPessimisticLock(orderId: Long): Order? {
        val orderEntity = orderJpaRepository.findByIdWithPessimisticLock(orderId) ?: return null
        val orderLines = getOrderLinesForOrder(orderId)
        return orderEntity.toDomain(orderLines)
    }

    override fun findById(orderId: Long): Order? {
        val orderEntity = orderJpaRepository.findById(orderId).getOrNull() ?: return null
        val orderLines = getOrderLinesForOrder(orderId)
        return orderEntity.toDomain(orderLines)
    }

    override fun findAll(): List<Order> {
        val orderEntities = orderJpaRepository.findAll()
        return orderEntities.map { orderEntity ->
            val orderLines = getOrderLinesForOrder(orderEntity.id)
            orderEntity.toDomain(orderLines)
        }
    }
    
    /**
     * 주문 ID로 해당 주문의 모든 주문 라인을 조회하여 도메인 모델로 변환
     */
    private fun getOrderLinesForOrder(orderId: Long): List<OrderLine> {
        val orderLineEntities = orderLineJpaRepository.findAllByOrderId(orderId)
        return orderLineEntities.map { it.toDomain() }
    }
}