package kr.hhplus.be.server.domain.service.coupon

import kr.hhplus.be.server.domain.coupon.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class CouponServiceIntegrationTest {

    @Autowired
    private lateinit var couponService: CouponService

    @Autowired
    private lateinit var couponRepository: CouponRepository

    private val userId = 2L
    private lateinit var savedAmountCoupon: Coupon
    private lateinit var savedPercentCoupon: Coupon

    @BeforeEach
    fun setup() {
        // 각 테스트가 시작되기 전에 데이터베이스를 깨끗한 상태로 유지
        // 기존에 발급된 쿠폰 등이 있다면 테스트에 방해가 될 수 있음

        // 금액 쿠폰 생성 및 저장
        val amountCoupon = AmountCoupon(
            name = "5000원 할인 쿠폰",
            stock = 10L,
            amount = 5000L,
            active = true
        )

        // 퍼센트 쿠폰 생성 및 저장
        val percentCoupon = PercentageCoupon(
            name = "10% 할인 쿠폰",
            stock = 5L,
            percent = 10.0,
            active = true
        )

        // 데이터베이스에 저장하고 반환된 객체를 필드에 저장
        savedAmountCoupon = couponRepository.save(amountCoupon)
        savedPercentCoupon = couponRepository.save(percentCoupon)

        // 저장된 쿠폰이 유효한지 확인
        assertNotNull(savedAmountCoupon.id)
        assertNotNull(savedPercentCoupon.id)
        assertTrue(savedAmountCoupon.id > 0)
        assertTrue(savedPercentCoupon.id > 0)
    }

    @Test
    fun `getUserCouponList - 사용자의 쿠폰 목록 조회`() {
        // given
        // 쿠폰 발급 (반드시 저장된 쿠폰의 ID를 사용)
        couponService.issuedCoupon(userId + 1, savedAmountCoupon.id)
        couponService.issuedCoupon(userId + 1, savedPercentCoupon.id)

        // when
        val userCoupons = couponService.getUserCouponList(userId + 1)

        // then
        assertEquals(2, userCoupons.size)
        assertTrue(userCoupons.any { it.id == savedAmountCoupon.id })
        assertTrue(userCoupons.any { it.id == savedPercentCoupon.id })
    }

    @Test
    fun `issuedCoupon - 쿠폰 발급 성공`() {
        // when
        couponService.issuedCoupon(userId + 3, savedAmountCoupon.id)

        // then
        val userCoupons = couponService.getUserCouponList(userId + 3)
        assertEquals(1, userCoupons.size)

        // 쿠폰 재고 감소 확인
        val updatedCoupon = couponRepository.findById(savedAmountCoupon.id)
        assertEquals(9L, updatedCoupon.stock) // 10 - 1
    }

    @Test
    fun `issuedCoupon - 재고 없을 경우 발급 실패`() {
        // given
        // 재고가 0인 쿠폰 생성 및 저장
        val zeroCoupon = AmountCoupon(
            name = "재고 없는 쿠폰",
            stock = 0L,
            amount = 1000L,
            active = true
        )
        val savedZeroCoupon = couponRepository.save(zeroCoupon)

        // 저장 확인
        assertNotNull(savedZeroCoupon)
        assertEquals(0L, savedZeroCoupon.stock)

        // when & then
        // 실제로 IllegalArgumentException이 발생하는지 검증
        val exception = assertThrows<IllegalArgumentException> {
            couponService.issuedCoupon(userId, savedZeroCoupon.id)
        }

        // 에러 메시지 검증 (선택적)
        assertTrue(
            exception.message?.contains("사용 불가능한 쿠폰") == true ||
                    exception.message?.contains("쿠폰 재고가 없습니다") == true
        )
    }

    @Test
    fun `issuedCoupon - 존재하지 않는 쿠폰 발급 실패`() {
        // 존재하지 않는 쿠폰 ID (매우 큰 값으로 설정)
        val nonExistentCouponId = 999999L

        // when & then
        assertThrows<EmptyResultDataAccessException> {
            couponService.issuedCoupon(userId, nonExistentCouponId)
        }
    }

    @Test
    fun `findByIssuedCouponId - 발급된 쿠폰 조회`() {
        // given
        couponService.issuedCoupon(userId, savedAmountCoupon.id)
        val issuedCoupon = couponRepository.findByUserIdAndCouponId(userId, savedAmountCoupon.id)
        assertNotNull(issuedCoupon, "발급된 쿠폰이 존재해야 합니다.")

        // when
        val result = couponService.findByIssuedCouponId(issuedCoupon.id)

        // then
        assertNotNull(result, "조회 결과가 존재해야 합니다.")
        assertEquals(issuedCoupon.id, result.issuedCoupon.id)
        assertEquals(savedAmountCoupon.id, result.coupon.id)
    }

    @Test
    fun `useIssuedCoupon - 쿠폰 사용 성공`() {
        // given
        // 먼저 쿠폰을 발급하고
        couponService.issuedCoupon(userId, savedAmountCoupon.id)
        // 발급된 쿠폰을 찾아옴
        val issuedCoupon = couponRepository.findByUserIdAndCouponId(userId, savedAmountCoupon.id)
        assertNotNull(issuedCoupon, "발급된 쿠폰이 존재해야 합니다.")

        // when
        val result = couponService.useIssuedCoupon(issuedCoupon.id)

        // then
        assertNotNull(result, "사용된 쿠폰이 반환되어야 합니다.")
        assertEquals(savedAmountCoupon.id, result?.id)

        // 발급된 쿠폰 상태 확인
        val usedCoupon = couponRepository.findIssuedCouponById(issuedCoupon.id)
        assertNotNull(usedCoupon, "사용된 쿠폰이 존재해야 합니다.")
        assertTrue(usedCoupon.isUsed, "쿠폰은 사용된 상태여야 합니다.")
    }

    @Test
    fun `useIssuedCoupon - null 쿠폰 ID 처리`() {
        // when
        val result = couponService.useIssuedCoupon(null)

        // then
        assertNull(result, "쿠폰 ID가 null이면 결과도 null이어야 합니다.")
    }

    @Test
    fun `useIssuedCoupon - 존재하지 않는 쿠폰 ID 처리`() {
        // 존재하지 않는 발급 쿠폰 ID
        val nonExistentIssuedCouponId = 999999L

        // when & then
        assertThrows<EmptyResultDataAccessException> {
            couponService.useIssuedCoupon(nonExistentIssuedCouponId)
        }
    }
} 