package kr.hhplus.be.server.domain.service.payment

import kr.hhplus.be.server.domain.coupon.AmountCoupon
import kr.hhplus.be.server.domain.coupon.PercentageCoupon
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderLine
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.payment.PaymentRepository
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.PaymentStatus
import kr.hhplus.be.server.domain.point.Point
import kr.hhplus.be.server.domain.point.PointRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@Transactional
class PaymentServiceIntegrationTest {

    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var pointRepository: PointRepository

    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    private var testUser: Long = 0L
    private lateinit var testOrder: Order

    @BeforeEach
    fun setup() {
        testUser = 1L

        // 테스트용 포인트 설정
        val point = Point(testUser, 50000L)
        pointRepository.save(point)

        // 테스트용 주문 생성
        val orderLines = mutableListOf(
            OrderLine(productId = 1L, productPrice = 10000L, quantity = 2L)
        )

        testOrder = Order(
            id = 1L,
            userId = testUser,
            orderLines = orderLines,
            orderDateTime = LocalDateTime.now(),
            orderStatus = OrderStatus.결제_대기중
        )
        // Order의 totalPrice 필드는 20000L (10000 * 2)
    }

    @Test
    fun `pay - 쿠폰 없이 결제 성공`() {
        // when
        paymentService.pay(testOrder, null)

        // then
        // 포인트 차감 확인
        val updatedPoint = pointRepository.findByUserId(testUser)
        assertNotNull(updatedPoint)
        assertEquals(30000L, updatedPoint!!.amount) // 50000 - 20000

        // 결제 내역 저장 확인
        val payment = paymentRepository.findByOrderId(testOrder.id)
        assertNotNull(payment)
        assertEquals(testOrder.id, payment.orderId)
        assertEquals(testUser, payment.userId)
        assertEquals(20000L, payment.payAmount)
        assertEquals(PaymentStatus.COMPLETED, payment.status)
        assertEquals(30000L, payment.remainPointAmount)
    }

    @Test
    fun `pay - 금액 쿠폰으로 결제 성공`() {
        // given
        val amountCoupon = AmountCoupon(
            id = 1L,
            name = "5000원 할인 쿠폰",
            stock = 100L,
            amount = 5000L,
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(7),
            active = true
        )

        // when
        paymentService.pay(testOrder, amountCoupon)

        // then
        // 포인트 차감 확인
        val updatedPoint = pointRepository.findByUserId(testUser)
        assertNotNull(updatedPoint)
        assertEquals(35000L, updatedPoint!!.amount) // 50000 - (20000 - 5000)

        // 결제 내역 저장 확인
        val payment = paymentRepository.findByOrderId(testOrder.id)
        assertNotNull(payment)
        assertEquals(15000L, payment.payAmount) // 20000 - 5000
    }

    @Test
    fun `pay - 퍼센트 쿠폰으로 결제 성공`() {
        // given
        val percentageCoupon = PercentageCoupon(
            id = 2L,
            name = "20% 할인 쿠폰",
            stock = 100L,
            percent = 20.0,
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(7),
            active = true
        )

        // when
        paymentService.pay(testOrder, percentageCoupon)

        // then
        // 포인트 차감 확인
        val updatedPoint = pointRepository.findByUserId(testUser)
        assertNotNull(updatedPoint)
        assertEquals(34000L, updatedPoint!!.amount) // 50000 - (20000 - 4000(20%))

        // 결제 내역 저장 확인
        val payment = paymentRepository.findByOrderId(testOrder.id)
        assertNotNull(payment)
        assertEquals(16000L, payment.payAmount) // 20000 - 4000
    }

    @Test
    fun `pay - 포인트 부족시 예외 발생`() {
        // given
        // 포인트를 주문 금액보다 적게 설정
        val point = Point(testUser, 10000L)
        pointRepository.save(point)

        // when & then
        assertThrows<IllegalArgumentException> {
            paymentService.pay(testOrder, null)
        }

        // 결제 내역이 저장되지 않았는지 확인
        assertThrows<EmptyResultDataAccessException> { paymentRepository.findByOrderId(testOrder.id) }
    }
} 