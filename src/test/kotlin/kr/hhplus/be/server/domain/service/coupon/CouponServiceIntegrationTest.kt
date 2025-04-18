package kr.hhplus.be.server.domain.service.coupon

import kr.hhplus.be.server.domain.coupon.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@Transactional
class CouponServiceIntegrationTest {

    @Autowired
    private lateinit var couponService: CouponService

    @Autowired
    private lateinit var couponRepository: CouponRepository

    private val userId = 1L
    private lateinit var amountCoupon: Coupon
    private lateinit var percentageCoupon: Coupon

    @BeforeEach
    fun setup() {
        // 금액 쿠폰 생성
        amountCoupon = AmountCoupon(
            name = "5000원 할인 쿠폰",
            stock = 10L,
            amount = 5000L,
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(7),
            active = true
        )

        // 퍼센트 쿠폰 생성
        percentageCoupon = PercentageCoupon(
            name = "10% 할인 쿠폰",
            stock = 5L,
            percent = 10.0,
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(7),
            active = true
        )

        amountCoupon = couponRepository.save(amountCoupon)
        percentageCoupon = couponRepository.save(percentageCoupon)
    }

    @Test
    fun `getUserCouponList - 사용자의 쿠폰 목록 조회`() {
        // given
        // 쿠폰 발급
        couponService.issuedCoupon(userId, amountCoupon.id)
        couponService.issuedCoupon(userId, percentageCoupon.id)

        // when
        val userCoupons = couponService.getUserCouponList(userId)

        // then
        assertEquals(2, userCoupons.size)
        assertTrue(userCoupons.any { it.id == amountCoupon.id })
        assertTrue(userCoupons.any { it.id == percentageCoupon.id })
    }

    @Test
    fun `issuedCoupon - 쿠폰 발급 성공`() {
        // when
        couponService.issuedCoupon(userId, amountCoupon.id)

        // then
        val userCoupons = couponService.getUserCouponList(userId)
        assertEquals(1, userCoupons.size)

        // 쿠폰 재고 감소 확인
        val updatedCoupon = couponRepository.findById(amountCoupon.id)
        assertEquals(9L, updatedCoupon.stock) // 10 - 1
    }

    @Test
    fun `issuedCoupon - 재고 없을 경우 발급 실패`() {
        // given
        val zeroCoupon = AmountCoupon(
            name = "재고 없는 쿠폰",
            stock = 0L,
            amount = 1000L,
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(7),
            active = true
        )
        couponRepository.save(zeroCoupon)

        // when & then
        assertThrows<IllegalArgumentException> {
            couponService.issuedCoupon(userId, zeroCoupon.id)
        }
    }

    @Test
    fun `findByIssuedCouponId - 발급된 쿠폰 조회`() {
        // given
        couponService.issuedCoupon(userId, amountCoupon.id)
        val issuedCoupon = couponRepository.findByUserIdAndCouponId(userId, amountCoupon.id)

        // when
        val result = couponService.findByIssuedCouponId(issuedCoupon.id)

        // then
        assertNotNull(result)
        assertEquals(issuedCoupon.id, result.issuedCoupon.id)
        assertEquals(amountCoupon.id, result.coupon.id)
    }

    @Test
    fun `useIssuedCoupon - 쿠폰 사용 성공`() {
        // given
        couponService.issuedCoupon(userId, amountCoupon.id)
        val issuedCoupon = couponRepository.findByUserIdAndCouponId(userId, amountCoupon.id)

        // when
        val result = couponService.useIssuedCoupon(issuedCoupon.id)

        // then
        assertNotNull(result)
        assertEquals(amountCoupon.id, result?.id)

        // 발급된 쿠폰 상태 확인
        val usedCoupon = couponRepository.findIssuedCouponById(issuedCoupon.id)
        assertTrue(usedCoupon.isUsed)
    }

    @Test
    fun `useIssuedCoupon - null 쿠폰 ID 처리`() {
        // when
        val result = couponService.useIssuedCoupon(null)

        // then
        assertNull(result)
    }
} 