package kr.hhplus.be.server.domain.service.coupon

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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
    private lateinit var coupon: Coupon
    private lateinit var issuedCoupon: IssuedCoupon
    private lateinit var userCoupons: List<Coupon>

    @BeforeEach
    fun setUp() {
        coupon = PercentageCoupon(
            id = couponId,
            name = "테스트 쿠폰",
            percent = 10.0,
            stock = 100L,
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(1),
            active = true
        )

        issuedCoupon = IssuedCoupon(
            userId = userId,
            couponId = couponId,
            isUsed = false
        )

        userCoupons = listOf(
            coupon,
            AmountCoupon(
                id = 2L,
                name = "테스트 쿠폰 2",
                amount = 1000L,
                stock = 100L,
                startDate = LocalDateTime.now().minusDays(1),
                endDate = LocalDateTime.now().plusDays(1),
                active = true,
            )
        )

        every { couponRepository.findAllByUserId(userId) } returns userCoupons
        every { couponRepository.findById(couponId) } returns coupon
        every { couponRepository.findIssuedCouponById(1L) } returns issuedCoupon
        every { couponRepository.save(any<IssueCouponAndIssuedCoupon>()) } returns Unit
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
        val remainingCoupon = PercentageCoupon(
            id = couponId,
            name = "테스트 쿠폰",
            percent = 10.0,
            stock = 99L,
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(1),
            active = true
        )
        val issueCouponAndIssuedCoupon = IssueCouponAndIssuedCoupon(
            issuedCoupon = issuedCoupon,
            remainingCoupon = remainingCoupon
        )
//        every { coupon.issueTo(userId) } returns issuedCouponAndCoupon

        // When
        couponService.issuedCoupon(userId, couponId)

        // Then
        verify(exactly = 1) { couponRepository.findById(couponId) }
//        verify(exactly = 1) { coupon.issueTo(userId) }
//        verify(exactly = 1) { couponRepository.save(issueCouponAndIssuedCoupon) } TODO: ???
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
//        verify(exactly = 1) { issuedCoupon.useCoupon() } // 어떻게 테스트하지?
        verify(exactly = 1) { couponRepository.findById(couponId) }
    }

    @Test
    fun `쿠폰 사용 처리 - 발급된 쿠폰이 없는 경우`() {
        // When
        val result = couponService.useIssuedCoupon(null)

        // Then
        assertEquals(null, result)
        verify(exactly = 0) { couponRepository.findIssuedCouponById(any()) }
        verify(exactly = 0) { couponRepository.findById(any()) }
    }
}