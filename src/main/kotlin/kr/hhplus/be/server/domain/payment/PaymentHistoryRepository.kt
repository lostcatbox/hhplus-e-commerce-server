package kr.hhplus.be.server.domain.payment

interface PaymentHistoryRepository {
    fun save(payment: Payment)
}