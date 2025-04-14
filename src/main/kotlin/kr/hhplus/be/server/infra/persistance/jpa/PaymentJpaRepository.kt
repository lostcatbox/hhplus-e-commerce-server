package kr.hhplus.be.server.infra.persistance.jpa

import kr.hhplus.be.server.domain.payment.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentJpaRepository : JpaRepository<Payment, Long>