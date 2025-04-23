package kr.hhplus.be.server.infra.persistance.jpa

import kr.hhplus.be.server.infra.persistance.model.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentJpaRepository : JpaRepository<PaymentEntity, Long> {
    fun findByOrderId(orderId: Long): PaymentEntity
}