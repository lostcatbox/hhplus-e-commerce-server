package kr.hhplus.be.server.domain.port.out

import kr.hhplus.be.server.domain.model.Payment
import org.springframework.stereotype.Repository

@Repository
interface PaymentHistoryRepository {
    fun save(payment: Payment)
}