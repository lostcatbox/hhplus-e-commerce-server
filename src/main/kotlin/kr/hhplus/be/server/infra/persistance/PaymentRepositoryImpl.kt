package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentRepository
import kr.hhplus.be.server.infra.persistance.jpa.PaymentJpaRepository
import org.springframework.stereotype.Repository

@Repository
class PaymentRepositoryImpl(
    private val paymentJpaRepository: PaymentJpaRepository
) : PaymentRepository {
    override fun save(payment: Payment) {
        paymentJpaRepository.save(payment)
    }

    override fun findByOrderId(orderId: Long): Payment {
        return paymentJpaRepository.findByOrderId(orderId)
    }
} 