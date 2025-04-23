package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentRepository
import kr.hhplus.be.server.infra.persistance.jpa.PaymentJpaRepository
import kr.hhplus.be.server.infra.persistance.model.PaymentEntity
import org.springframework.stereotype.Repository

@Repository
class PaymentRepositoryImpl(
    private val paymentJpaRepository: PaymentJpaRepository
) : PaymentRepository {
    override fun save(payment: Payment) {
        val entity = PaymentEntity.from(payment)
        paymentJpaRepository.save(entity)
    }

    override fun findByOrderId(orderId: Long): Payment {
        return paymentJpaRepository.findByOrderId(orderId).toDomain()
    }
} 