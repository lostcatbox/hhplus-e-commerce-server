package kr.hhplus.be.server.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.point.Point
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PointTest {

    @Nested
    @DisplayName("포인트 사용 테스트")
    inner class UsePointTest {
        @Test
        fun `포인트 정상 사용`() {
            // given
            val point = Point(userId = 1L, amount = 10000L)
            val useAmount = 3000L

            // when
            val resultPoint = point.usePoint(useAmount)

            // then
            resultPoint.amount shouldBe 7000L
            resultPoint.userId shouldBe 1L
        }

        @Test
        fun `보유 포인트보다 많은 금액 사용 시 예외 발생`() {
            // given
            val point = Point(userId = 1L, amount = 1000L)
            val useAmount = 2000L

            // when & then
            shouldThrow<IllegalArgumentException> {
                point.usePoint(useAmount)
            }.message shouldBe "금액은 0 이상이여야 합니다."
        }

        @Test
        fun `보유 포인트와 동일한 금액 사용 가능`() {
            // given
            val point = Point(userId = 1L, amount = 1000L)
            val useAmount = 1000L

            // when
            val resultPoint = point.usePoint(useAmount)

            // then
            resultPoint.amount shouldBe 0L
        }
    }

    @Nested
    @DisplayName("포인트 충전 테스트")
    inner class ChargePointTest {
        @Test
        fun `포인트 정상 충전`() {
            // given
            val point = Point(userId = 1L, amount = 1000L)
            val chargeAmount = 5000L

            // when
            val resultPoint = point.chargePoint(chargeAmount)

            // then
            resultPoint.amount shouldBe 6000L
            resultPoint.userId shouldBe 1L
        }

        @Test
        fun `0 이하 금액 충전 시 예외 발생`() {
            // given
            val point = Point(userId = 1L, amount = 1000L)
            val chargeAmount = 0L

            // when & then
            shouldThrow<IllegalArgumentException> {
                point.chargePoint(chargeAmount)
            }.message shouldBe "한번 충전 시 충전 금액은 0 초과 100만원 이하여야합니다."
        }

        @Test
        fun `100만원 초과 충전 시 예외 발생`() {
            // given
            val point = Point(userId = 1L, amount = 1000L)
            val chargeAmount = 1000001L

            // when & then
            shouldThrow<IllegalArgumentException> {
                point.chargePoint(chargeAmount)
            }.message shouldBe "한번 충전 시 충전 금액은 0 초과 100만원 이하여야합니다."
        }

        @Test
        fun `100만원 충전 가능`() {
            // given
            val point = Point(userId = 1L, amount = 1000L)
            val chargeAmount = 1000000L

            // when
            val resultPoint = point.chargePoint(chargeAmount)

            // then
            resultPoint.amount shouldBe 1001000L
        }
    }

    @Nested
    @DisplayName("Point 생성 테스트")
    inner class CreatePointTest {
        @Test
        fun `Point 객체 생성`() {
            // when
            val point = Point(userId = 1L, amount = 0L)

            // then
            point.userId shouldBe 1L
            point.amount shouldBe 0L
        }
    }
}