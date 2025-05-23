package kr.hhplus.be.server.domain.model

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderHistory
import kr.hhplus.be.server.domain.order.OrderLine
import kr.hhplus.be.server.domain.order.OrderStatus
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
        val orderLines = mutableListOf(
            OrderLine(
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
        val orderLines = mutableListOf(
            OrderLine(
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
        // 상품 준비중 상태로 변경
        val readyProductOrder = order.readyProduct()
        assertEquals(OrderStatus.상품_준비중, readyProductOrder.orderStatus)
        // 원본 order는 변경되지 않음
        assertEquals(OrderStatus.주문_요청됨, order.orderStatus)

        // 결제 대기중 상태로 변경
        val readyPayOrder = readyProductOrder.readyPay()
        assertEquals(OrderStatus.결제_대기중, readyPayOrder.orderStatus)
        // 이전 order는 변경되지 않음
        assertEquals(OrderStatus.상품_준비중, readyProductOrder.orderStatus)

        // 결제 완료 상태로 변경
        val finishPayOrder = readyPayOrder.finishPay()
        assertEquals(OrderStatus.결제_완료, finishPayOrder.orderStatus)
        // 이전 order는 변경되지 않음
        assertEquals(OrderStatus.결제_대기중, readyPayOrder.orderStatus)

        // 주문 실패 상태로 변경
        val failOrder = finishPayOrder.failOrder()
        assertEquals(OrderStatus.주문_실패, failOrder.orderStatus)
        // 이전 order는 변경되지 않음
        assertEquals(OrderStatus.결제_완료, finishPayOrder.orderStatus)
    }

    @Test
    fun `OrderLine 생성 시 totalPrice가 자동 계산된다`() {
        // given & when
        val orderLine = OrderLine(
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
        val orderLines = mutableListOf(
            OrderLine(
                productId = 1L,
                productPrice = 1000L,
                quantity = 2L
            )
        )

        // when
        val orderHistory = OrderHistory(
            userId = 1L,
            orderId = 1L,
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
        val orderLines = mutableListOf(
            OrderLine(
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
        val orderLines = mutableListOf(
            OrderLine(
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