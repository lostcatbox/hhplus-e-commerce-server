package kr.hhplus.be.server.domain.payment

import org.springframework.stereotype.Repository

@Repository
interface PaymentHistoryRepository {
    fun save(payment: Payment)
}