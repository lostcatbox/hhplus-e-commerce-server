package kr.hhplus.be.server.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CouponsTest {

    private val now = LocalDateTime.now()
    private val validStartDate = now.minusDays(1)
    private val validEndDate = now.plusDays(1)

    @Nested
    @DisplayName("AmountCoupon 테스트")
    inner class AmountCouponTest {
        @Test
        fun `정상적인 금액 할인 쿠폰 생성`() {
            // when & then
            AmountCoupon(
                couponId = 1L,
                name = "1만원 할인 쿠폰",
                stock = 100L,
                startDate = validStartDate,
                endDate = validEndDate,
                active = true,
                amount = 10000L
            )
        }

        @Test
        fun `할인 금액이 0 이하면 생성 실패`() {
            shouldThrow<IllegalArgumentException> {
                AmountCoupon(
                    couponId = 1L,
                    name = "잘못된 쿠폰",
                    stock = 100L,
                    startDate = validStartDate,
                    endDate = validEndDate,
                    active = true,
                    amount = 0L
                )
            }
        }

        @Test
        fun `정상적인 금액 할인 계산`() {
            // given
            val coupon = AmountCoupon(
                couponId = 1L,
                name = "1만원 할인 쿠폰",
                stock = 100L,
                startDate = validStartDate,
                endDate = validEndDate,
                active = true,
                amount = 10000L
            )

            // when
            val discounted = coupon.discountAmount(30000L)

            // then
            discounted shouldBe 20000L
        }

        @Test
        fun `할인 금액이 원래 금액보다 크면 예외 발생`() {
            // given
            val coupon = AmountCoupon(
                couponId = 1L,
                name = "1만원 할인 쿠폰",
                stock = 100L,
                startDate = validStartDate,
                endDate = validEndDate,
                active = true,
                amount = 10000L
            )

            // when & then
            shouldThrow<IllegalArgumentException> {
                coupon.discountAmount(5000L)
            }
        }
    }

    @Nested
    @DisplayName("PercentageCoupon 테스트")
    inner class PercentageCouponTest {
        @Test
        fun `정상적인 퍼센트 할인 쿠폰 생성`() {
            // when & then
            PercentageCoupon(
                couponId = 1L,
                name = "10% 할인 쿠폰",
                stock = 100L,
                startDate = validStartDate,
                endDate = validEndDate,
                active = true,
                percent = 10.0
            )
        }

        @Test
        fun `할인율이 0% 이하면 생성 실패`() {
            shouldThrow<IllegalArgumentException> {
                PercentageCoupon(
                    couponId = 1L,
                    name = "잘못된 쿠폰",
                    stock = 100L,
                    startDate = validStartDate,
                    endDate = validEndDate,
                    active = true,
                    percent = 0.0
                )
            }
        }

        @Test
        fun `할인율이 100% 초과면 생성 실패`() {
            shouldThrow<IllegalArgumentException> {
                PercentageCoupon(
                    couponId = 1L,
                    name = "잘못된 쿠폰",
                    stock = 100L,
                    startDate = validStartDate,
                    endDate = validEndDate,
                    active = true,
                    percent = 101.0
                )
            }
        }

        @Test
        fun `정상적인 퍼센트 할인 계산`() {
            // given
            val coupon = PercentageCoupon(
                couponId = 1L,
                name = "10% 할인 쿠폰",
                stock = 100L,
                startDate = validStartDate,
                endDate = validEndDate,
                active = true,
                percent = 10.0
            )

            // when
            val discounted = coupon.discountAmount(10000L)

            // then
            discounted shouldBe 9000L
        }
    }

    @Nested
    @DisplayName("쿠폰 발급 테스트")
    inner class CouponIssueTest {
        @Test
        fun `정상적인 쿠폰 발급`() {
            // given
            val coupon = AmountCoupon(
                couponId = 1L,
                name = "1만원 할인 쿠폰",
                stock = 100L,
                startDate = validStartDate,
                endDate = validEndDate,
                active = true,
                amount = 10000L
            )

            // when
            val result = coupon.issueTo(1L)

            // then
            result.remainingCoupon.stock shouldBe 99L
            result.issuedCoupon.couponId shouldBe 1L
            result.issuedCoupon.userId shouldBe 1L
            result.issuedCoupon.isUsed shouldBe false
        }

        @Test
        fun `재고가 없으면 발급 실패`() {
            // given
            val coupon = AmountCoupon(
                couponId = 1L,
                name = "1만원 할인 쿠폰",
                stock = 0L,
                startDate = validStartDate,
                endDate = validEndDate,
                active = true,
                amount = 10000L
            )

            // when & then
            shouldThrow<IllegalArgumentException> {
                coupon.issueTo(1L)
            }
        }

        @Test
        fun `비활성화된 쿠폰은 발급 실패`() {
            // given
            val coupon = AmountCoupon(
                couponId = 1L,
                name = "1만원 할인 쿠폰",
                stock = 100L,
                startDate = validStartDate,
                endDate = validEndDate,
                active = false,
                amount = 10000L
            )

            // when & then
            shouldThrow<IllegalArgumentException> {
                coupon.issueTo(1L)
            }
        }
    }

    @Nested
    @DisplayName("IssuedCoupon 테스트")
    inner class IssuedCouponTest {
        @Test
        fun `발급된 쿠폰 사용`() {
            // given
            val issuedCoupon = IssuedCoupon(
                couponId = 1L,
                userId = 1L,
                isUsed = false
            )

            // when
            val usedCoupon = issuedCoupon.useCoupon()

            // then
            usedCoupon.isUsed shouldBe true
        }

        @Test
        fun `이미 사용된 쿠폰은 재사용 불가`() {
            // given
            val issuedCoupon = IssuedCoupon(
                couponId = 1L,
                userId = 1L,
                isUsed = true
            )

            // when & then
            shouldThrow<IllegalArgumentException> {
                issuedCoupon.useCoupon()
            }
        }
    }
}