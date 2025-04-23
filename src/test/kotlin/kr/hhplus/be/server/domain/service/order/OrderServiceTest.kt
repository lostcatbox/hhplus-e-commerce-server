package kr.hhplus.be.server.domain.service.order

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.order.*
import kr.hhplus.be.server.domain.product.ProductService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class OrderServiceTest {

    @MockK
    private lateinit var orderRepository: OrderRepository

    @MockK
    private lateinit var orderHistoryRepository: OrderHistoryRepository

    @MockK
    private lateinit var productService: ProductService

    @InjectMockKs
    private lateinit var orderService: OrderService

    private val now = LocalDateTime.now()
    private lateinit var order: Order
    private lateinit var savedOrder: Order

    @BeforeEach
    fun setUp() {
        val orderLines = mutableListOf(
            OrderLine(
                productId = 1L,
                productPrice = 1000L,
                quantity = 2L
            )
        )

        order = Order(
            id = 1L,
            userId = 1L,
            issuedCouponId = null,
            orderLines = orderLines,
            orderDateTime = now,
            orderStatus = OrderStatus.주문_요청됨
        )

        savedOrder = Order(
            id = order.id,
            userId = order.userId,
            issuedCouponId = order.issuedCouponId,
            orderLines = order.orderLines,
            orderDateTime = order.orderDateTime
        )

        every { orderRepository.save(any()) } returns savedOrder
        every { orderHistoryRepository.save(any()) } returns OrderHistory(
            id = 1L,
            orderId = 1L,
            userId = 1L,
            issuedCouponId = null,
            orderDateTime = now,
            totalPrice = 2000L,
            orderStatus = OrderStatus.주문_요청됨
        )
    }

    @Test
    fun `상품 준비 상태로 전환`() {
        // Given
        val expectedStatus = OrderStatus.상품_준비중
        val expectedOrder = Order(
            id = order.id,
            userId = order.userId,
            issuedCouponId = order.issuedCouponId,
            orderLines = order.orderLines,
            orderDateTime = order.orderDateTime, orderStatus = expectedStatus
        )
        every { orderRepository.save(any()) } returns expectedOrder

        // When
        val result = orderService.changeProductReady(order)

        // Then
        assertEquals(expectedStatus, result.orderStatus)
        verify(exactly = 1) { orderRepository.save(any()) }
        verify(exactly = 1) { orderHistoryRepository.save(any()) }
    }

    @Test
    fun `결제 대기 상태로 전환`() {
        // Given
        val expectedStatus = OrderStatus.결제_대기중
        val expectedOrder = Order(
            id = order.id,
            userId = order.userId,
            issuedCouponId = order.issuedCouponId,
            orderLines = order.orderLines,
            orderDateTime = order.orderDateTime, orderStatus = expectedStatus
        )
        every { orderRepository.save(any()) } returns expectedOrder

        // When
        val result = orderService.changePaymentReady(order)

        // Then
        assertEquals(expectedStatus, result.orderStatus)
        verify(exactly = 1) { orderRepository.save(any()) }
        verify(exactly = 1) { orderHistoryRepository.save(any()) }
    }

    @Test
    fun `결제 완료 상태로 전환`() {
        // Given
        val expectedStatus = OrderStatus.결제_완료
        val expectedOrder = Order(
            id = order.id,
            userId = order.userId,
            issuedCouponId = order.issuedCouponId,
            orderLines = order.orderLines,
            orderDateTime = order.orderDateTime, orderStatus = expectedStatus
        )
        every { orderRepository.save(any()) } returns expectedOrder

        // When
        val result = orderService.changePaymentComplete(order)

        // Then
        assertEquals(expectedStatus, result.orderStatus)
        verify(exactly = 1) { orderRepository.save(any()) }
        verify(exactly = 1) { orderHistoryRepository.save(any()) }
    }

    @Test
    fun `주문 실패 상태로 전환`() {
        // Given
        val expectedStatus = OrderStatus.주문_실패
        val expectedOrder = Order(
            id = order.id,
            userId = order.userId,
            issuedCouponId = order.issuedCouponId,
            orderLines = order.orderLines,
            orderDateTime = order.orderDateTime, orderStatus = expectedStatus
        )
        every { orderRepository.save(any()) } returns expectedOrder

        // When
        val result = orderService.changeOrderFailed(order)

        // Then
        assertEquals(expectedStatus, result.orderStatus)
        verify(exactly = 1) { orderRepository.save(any()) }
        verify(exactly = 1) { orderHistoryRepository.save(any()) }
    }
} 