package kr.hhplus.be.server.infra.persistance.jpa

import kr.hhplus.be.server.infra.persistance.model.OrderLineEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OrderLineJpaRepository : JpaRepository<OrderLineEntity, Long> {
    /**
     * 주문 ID로 해당 주문의 모든 주문 라인을 조회
     */
    @Query("SELECT ol FROM order_lines ol WHERE ol.orderId = :orderId")
    fun findAllByOrderId(@Param("orderId") orderId: Long): List<OrderLineEntity>
} 