package kr.hhplus.be.server.domain.model

import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentStatus
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PaymentTest {
    private val now = LocalDateTime.now()

    @Test
    fun `Payment 생성 시 기본 상태는 PENDING 테스트`() {
        // given & when
        val payment = Payment(
            id = 1L,
            orderId = 1L,
            userId = 1L,
            payAmount = 10000L
        )

        // then
        assertEquals(PaymentStatus.PENDING, payment.status)
        assertNull(payment.remainPointAmount)
        assertNull(payment.couponId)
        assertNotNull(payment.createdAt)
    }

    @Test
    fun `Payment에 쿠폰이 적용된 경우 생성 테스트`() {
        // given & when
        val payment = Payment(
            id = 1L,
            orderId = 1L,
            userId = 1L,
            payAmount = 10000L,
            couponId = 1L
        )

        // then
        assertEquals(1L, payment.couponId)
    }

    @Test
    fun `Payment에 잔액 포인트와 쿠폰이 모두 적용된 경우`() {
        // given & when
        val payment = Payment(
            id = 1L,
            orderId = 1L,
            userId = 1L,
            payAmount = 10000L,
            remainPointAmount = 1000L,
            couponId = 1L
        )

        // then
        assertEquals(1000L, payment.remainPointAmount)
        assertEquals(1L, payment.couponId)
    }

    @Test
    fun `Payment 생성 시 createdAt과 updatedAt이 현재 시간으로 설정된다`() {
        // given & when
        val payment = Payment(
            id = 1L,
            orderId = 1L,
            userId = 1L,
            payAmount = 10000L
        )

        // then
        assertNotNull(payment.createdAt)
    }
} 