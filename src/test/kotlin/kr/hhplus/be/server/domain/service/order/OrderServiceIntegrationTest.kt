package kr.hhplus.be.server.domain.service.order

import kr.hhplus.be.server.domain.order.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var orderHistoryRepository: OrderHistoryRepository

    private lateinit var testOrder: Order

    @BeforeEach
    fun setup() {
        // 테스트용 주문 생성
        val orderLines = mutableListOf(
            OrderLine(productId = 1L, productPrice = 10000L, quantity = 2L)
        )

        testOrder = Order(
            userId = 1L,
            orderLines = orderLines,
            orderDateTime = LocalDateTime.now(),
            orderStatus = OrderStatus.주문_요청됨
        )

        testOrder = orderRepository.save(testOrder)
    }

    @Test
    fun `changeProductReady - 상품 준비중 상태로 변경`() {
        // when
        val result = orderService.changeProductReady(testOrder)

        // then
        assertEquals(OrderStatus.상품_준비중, result.orderStatus)

        // 이력이 저장되었는지 확인
        val histories = orderHistoryRepository.findByOrderId(result.id)
        assertTrue(histories.isNotEmpty())
        assertEquals(OrderStatus.상품_준비중, histories.last().orderStatus)
    }

    @Test
    fun `changePaymentReady - 결제 대기중 상태로 변경`() {
        // given
        testOrder.readyProduct() // 먼저 상품 준비중 상태로
        orderRepository.save(testOrder)

        // when
        val result = orderService.changePaymentReady(testOrder)

        // then
        assertEquals(OrderStatus.결제_대기중, result.orderStatus)

        // 이력이 저장되었는지 확인
        val histories = orderHistoryRepository.findByOrderId(result.id)
        assertTrue(histories.isNotEmpty())
        assertTrue(histories.any { it.orderStatus == OrderStatus.결제_대기중 })
    }

    @Test
    fun `changePaymentComplete - 결제 완료 상태로 변경`() {
        // given
        testOrder.readyProduct()
        testOrder.readyPay()
        orderRepository.save(testOrder)

        // when
        val result = orderService.changePaymentComplete(testOrder)

        // then
        assertEquals(OrderStatus.결제_완료, result.orderStatus)

        // 이력이 저장되었는지 확인
        val histories = orderHistoryRepository.findByOrderId(result.id)
        assertTrue(histories.isNotEmpty())
        assertTrue(histories.any { it.orderStatus == OrderStatus.결제_완료 })
    }

    @Test
    fun `changeOrderFailed - 주문 실패 상태로 변경`() {
        // when
        val result = orderService.changeOrderFailed(testOrder)

        // then
        assertEquals(OrderStatus.주문_실패, result.orderStatus)

        // 이력이 저장되었는지 확인
        val histories = orderHistoryRepository.findByOrderId(result.id)
        assertTrue(histories.isNotEmpty())
        assertTrue(histories.any { it.orderStatus == OrderStatus.주문_실패 })
    }

    @Test
    fun `saveOrderHistory - 주문 이력 저장`() {
        // when
        orderService.saveOrderHistory(testOrder)

        // then
        val histories = orderHistoryRepository.findByOrderId(testOrder.id)
        assertFalse(histories.isEmpty())

        val savedHistory = histories.last()
        assertEquals(testOrder.id, savedHistory.orderId)
        assertEquals(testOrder.userId, savedHistory.userId)
        assertEquals(testOrder.totalPrice, savedHistory.totalPrice)
        assertEquals(testOrder.orderStatus, savedHistory.orderStatus)
    }
} 