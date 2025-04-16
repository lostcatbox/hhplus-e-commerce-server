package kr.hhplus.be.server.domain.service.payment

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.PercentageCoupon
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderLine
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentHistoryRepository
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.PaymentStatus
import kr.hhplus.be.server.domain.point.Point
import kr.hhplus.be.server.domain.point.PointService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class PaymentServiceTest {

    @MockK
    private lateinit var pointService: PointService

    @MockK
    private lateinit var paymentHistoryRepository: PaymentHistoryRepository

    @InjectMockKs
    private lateinit var paymentService: PaymentService

    private val userId = 1L
    private val now = LocalDateTime.now()
    private lateinit var order: Order
    private lateinit var coupon: Coupon
    private lateinit var point: Point

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
            userId = userId,
            issuedCouponId = 1L,
            orderLines = orderLines,
            orderDateTime = now,
            orderStatus = OrderStatus.결제_대기중
        )

        coupon = PercentageCoupon(
            id = 1L,
            name = "테스트 쿠폰",
            percent = 10.0,
            stock = 100L,
            startDate = now,
            endDate = now,
            active = true
        )

        point = Point(userId = userId, amount = 10000L)

        every { pointService.getPoint(userId) } returns point
//        every { point.usePoint(userId) } returns usedPoint
        every { paymentHistoryRepository.save(any()) } returnsArgument 0
    }

    @Test
    fun `쿠폰 없이 결제 처리`() {
        // Given
        val paymentSlot = slot<Payment>()
        val orderWithoutCoupon = Order(
            id = order.id,
            userId = order.userId,
            issuedCouponId = null,
            orderLines = order.orderLines,
            orderDateTime = order.orderDateTime, orderStatus = order.orderStatus
        )
        every { paymentHistoryRepository.save(capture(paymentSlot)) } returnsArgument 0

        // When
        paymentService.pay(orderWithoutCoupon, null)

        // Then
        verify(exactly = 1) { pointService.getPoint(userId) }
//        verify(exactly = 1) { point.usePoint(userId) }
        verify(exactly = 1) { paymentHistoryRepository.save(any()) }

        val capturedPayment = paymentSlot.captured
        assert(capturedPayment.orderId == order.id)
        assert(capturedPayment.userId == userId)
        assert(capturedPayment.payAmount == order.totalPrice)
        assert(capturedPayment.status == PaymentStatus.COMPLETED)
        assert(capturedPayment.remainPointAmount == 8000L)
        assert(capturedPayment.couponId == null)
    }

    @Test
    fun `쿠폰 적용하여 결제 처리`() {
        // Given
        val paymentSlot = slot<Payment>()
        val discountedAmount = 1800L // 10% 할인 적용 (2000 * 0.9)
//        every { coupon.discountAmount(order.totalPrice) } returns discountedAmount
        every { paymentHistoryRepository.save(capture(paymentSlot)) } returnsArgument 0

        // When
        paymentService.pay(order, coupon)

        // Then
        verify(exactly = 1) { pointService.getPoint(userId) }
//        verify(exactly = 1) { coupon.discountAmount(order.totalPrice) }
//        verify(exactly = 1) { point.usePoint(userId) }
        verify(exactly = 1) { paymentHistoryRepository.save(any()) }

        val capturedPayment = paymentSlot.captured
        assert(capturedPayment.orderId == order.id)
        assert(capturedPayment.userId == userId)
        assert(capturedPayment.payAmount == discountedAmount)
        assert(capturedPayment.status == PaymentStatus.COMPLETED)
        assert(capturedPayment.remainPointAmount == 10000L - discountedAmount)
        assert(capturedPayment.couponId == order.issuedCouponId)
    }
} 