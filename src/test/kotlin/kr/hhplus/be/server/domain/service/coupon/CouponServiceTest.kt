package kr.hhplus.be.server.domain.service.coupon

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.model.*
import kr.hhplus.be.server.domain.port.out.CouponRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class CouponServiceTest {

    @MockK
    private lateinit var couponRepository: CouponRepository

    @InjectMockKs
    private lateinit var couponService: CouponService

    private val userId = 1L
    private val couponId = 1L
    private lateinit var coupon: PercentageCoupon
    private lateinit var amountCoupon: AmountCoupon
    private lateinit var issuedCoupon: IssuedCoupon
    private lateinit var userCoupons: List<Coupon>
    private lateinit var updatedIssuedCoupon: IssuedCoupon

    @BeforeEach
    fun setUp() {
        val now = LocalDateTime.now()

        coupon = mockk<PercentageCoupon>()
        every { coupon.id } returns couponId
        every { coupon.name } returns "테스트 쿠폰"
        every { coupon.startDate } returns now.minusDays(1)
        every { coupon.endDate } returns now.plusDays(1)

        amountCoupon = mockk<AmountCoupon>()
        every { amountCoupon.id } returns 2L
        every { amountCoupon.name } returns "테스트 쿠폰 2"

        issuedCoupon = mockk<IssuedCoupon>()
        every { issuedCoupon.couponId } returns couponId
        every { issuedCoupon.userId } returns userId

        updatedIssuedCoupon = mockk<IssuedCoupon>()

        userCoupons = listOf(
            coupon,
            amountCoupon
        )

        every { couponRepository.findAllByUserId(userId) } returns userCoupons
        every { couponRepository.findById(couponId) } returns coupon
        every { couponRepository.findByIdWithPessimisticLock(couponId) } returns coupon
        every { couponRepository.findIssuedCouponById(any()) } returns issuedCoupon
        every { couponRepository.save(any<IssueCouponAndIssuedCoupon>()) } returns Unit
        every { issuedCoupon.useCoupon() } returns updatedIssuedCoupon
    }

    @Test
    fun `사용자의 쿠폰 목록 조회`() {
        // When
        val result = couponService.getUserCouponList(userId)

        // Then
        assertEquals(2, result.size)
        assertEquals(couponId, result[0].id)
        assertEquals("테스트 쿠폰", result[0].name)
        verify(exactly = 1) { couponRepository.findAllByUserId(userId) }
    }

    @Test
    fun `쿠폰 발급`() {
        // Given
        val remainingCoupon = mockk<PercentageCoupon>()
        val issueCouponAndIssuedCoupon = mockk<IssueCouponAndIssuedCoupon>()
        every { coupon.issueTo(userId) } returns issueCouponAndIssuedCoupon

        // When
        couponService.issuedCoupon(userId, couponId)

        // Then
        verify(exactly = 1) { couponRepository.findByIdWithPessimisticLock(couponId) }
        verify(exactly = 1) { coupon.issueTo(userId) }
        verify(exactly = 1) { couponRepository.save(issueCouponAndIssuedCoupon) }
    }

    @Test
    fun `발급된 쿠폰 ID로 쿠폰 정보 조회`() {
        // When
        val result = couponService.findByIssuedCouponId(1L)

        // Then
        assertEquals(issuedCoupon, result.issuedCoupon)
        assertEquals(coupon, result.coupon)
        verify(exactly = 1) { couponRepository.findIssuedCouponById(1L) }
        verify(exactly = 1) { couponRepository.findById(couponId) }
    }

    @Test
    fun `쿠폰 사용 처리 - 발급된 쿠폰이 있는 경우`() {
        // When
        val result = couponService.useIssuedCoupon(1L)

        // Then
        assertEquals(coupon, result)
        verify(exactly = 1) { couponRepository.findIssuedCouponById(1L) }
        verify(exactly = 1) { issuedCoupon.useCoupon() }
        verify(exactly = 1) { couponRepository.findById(couponId) }
    }

    @Test
    fun `쿠폰 사용 처리 - 발급된 쿠폰이 없는 경우`() {
        // When
        val result = couponService.useIssuedCoupon(null)

        // Then
        assertEquals(null, result)
        verify(exactly = 0) { couponRepository.findIssuedCouponById(any()) }
        verify(exactly = 0) { issuedCoupon.useCoupon() }
        verify(exactly = 0) { couponRepository.findById(any()) }
    }
}