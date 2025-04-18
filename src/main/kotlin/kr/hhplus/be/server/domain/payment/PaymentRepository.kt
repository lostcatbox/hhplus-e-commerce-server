package kr.hhplus.be.server.domain.payment

interface PaymentRepository {
    fun save(payment: Payment)
    fun findByOrderId(orderId: Long): Payment
}