package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentHistoryRepository
import kr.hhplus.be.server.infra.persistance.jpa.PaymentJpaRepository
import org.springframework.stereotype.Repository

@Repository
class PaymentHistoryRepositoryImpl(
    private val paymentJpaRepository: PaymentJpaRepository
) : PaymentHistoryRepository {
    override fun save(payment: Payment) {
        paymentJpaRepository.save(payment)
    }
} 