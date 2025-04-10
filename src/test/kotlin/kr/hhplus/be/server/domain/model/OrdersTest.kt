package kr.hhplus.be.server.domain.model

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OrdersTest {
    private val now = LocalDateTime.now()

    @Test
    fun `Order 생성 시 기본 상태는 주문_요청됨 테스트`() {
        // given
        val orderLines = listOf(
            OrderLine(
                orderId = 1L,
                productId = 1L,
                productPrice = 1000L,
                quantity = 2L
            )
        )

        // when
        val order = Order(
            userId = 1L,
            orderLines = orderLines,
            orderDateTime = now
        )

        // then
        assertEquals(OrderStatus.주문_요청됨, order.orderStatus)
        assertEquals(2000L, order.totalPrice)
    }

    @Test
    fun `Order 상태 전환 테스트`() {
        // given
        val orderLines = listOf(
            OrderLine(
                orderId = 1L,
                productId = 1L,
                productPrice = 1000L,
                quantity = 2L
            )
        )
        val order = Order(
            userId = 1L,
            orderLines = orderLines,
            orderDateTime = now
        )

        // when & then
        val productReadyOrder = order.readyProduct()
        assertEquals(OrderStatus.상품_준비중, productReadyOrder.orderStatus)

        val paymentReadyOrder = productReadyOrder.readyPay()
        assertEquals(OrderStatus.결제_대기중, paymentReadyOrder.orderStatus)

        val paymentCompleteOrder = paymentReadyOrder.finishPay()
        assertEquals(OrderStatus.결제_완료, paymentCompleteOrder.orderStatus)

        val failedOrder = paymentReadyOrder.failOrder()
        assertEquals(OrderStatus.주문_실패, failedOrder.orderStatus)
    }

    @Test
    fun `OrderLine 생성 시 totalPrice가 자동 계산된다`() {
        // given & when
        val orderLine = OrderLine(
            orderId = 1L,
            productId = 1L,
            productPrice = 1000L,
            quantity = 2L
        )

        // then
        assertEquals(2000L, orderLine.totalPrice)
    }

    @Test
    fun `OrderHistory 생성 테스트`() {
        // given
        val orderLines = listOf(
            OrderLine(
                orderId = 1L,
                productId = 1L,
                productPrice = 1000L,
                quantity = 2L
            )
        )

        // when
        val orderHistory = OrderHistory(
            orderId = 1L,
            userId = 1L,
            orderLines = orderLines,
            orderDateTime = now,
            totalPrice = 2000L,
            orderStatus = OrderStatus.결제_완료
        )

        // then
        assertEquals(1L, orderHistory.orderId)
        assertEquals(1L, orderHistory.userId)
        assertEquals(2000L, orderHistory.totalPrice)
        assertEquals(OrderStatus.결제_완료, orderHistory.orderStatus)
    }

    @Test
    fun `Order에 쿠폰이 적용된 경우 생성 테스트`() {
        // given
        val orderLines = listOf(
            OrderLine(
                orderId = 1L,
                productId = 1L,
                productPrice = 1000L,
                quantity = 2L
            )
        )

        // when
        val order = Order(
            userId = 1L,
            issuedCouponId = 1L,
            orderLines = orderLines,
            orderDateTime = now
        )

        // then
        assertNotNull(order.issuedCouponId)
        assertEquals(1L, order.issuedCouponId)
    }

    @Test
    fun `Order에 쿠폰이 적용되지 않은 경우 생성 테스트`() {
        // given
        val orderLines = listOf(
            OrderLine(
                orderId = 1L,
                productId = 1L,
                productPrice = 1000L,
                quantity = 2L
            )
        )

        // when
        val order = Order(
            userId = 1L,
            orderLines = orderLines,
            orderDateTime = now
        )

        // then
        assertNull(order.issuedCouponId)
    }
} 