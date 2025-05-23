package kr.hhplus.be.server.domain.service.point

import kr.hhplus.be.server.domain.point.Point
import kr.hhplus.be.server.domain.point.PointRepository
import kr.hhplus.be.server.domain.point.PointService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class PointServiceIntegrationTest {

    @Autowired
    private lateinit var pointService: PointService

    @Autowired
    private lateinit var pointRepository: PointRepository

    @Test
    fun `getPoint - 존재하는 사용자의 포인트 조회`() {
        // given
        val userId = 1L
        val point = Point(userId, 5000L)
        pointRepository.save(point)

        // when
        val result = pointService.getPoint(userId)

        // then
        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(5000L, result.amount)
    }

    @Test
    fun `getPoint - 존재하지 않는 사용자는 0 포인트 반환`() {
        // given
        val userId = 999L

        // when
        val result = pointService.getPoint(userId)

        // then
        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(0L, result.amount)
    }

    @Test
    fun `usePoint - 포인트 사용 성공`() {
        // given
        val userId = 1L
        val point = Point(userId, 5000L)
        pointRepository.save(point)

        // when
        pointService.usePoint(userId, 3000L)

        // then
        val updatedPoint = pointRepository.findByUserId(userId)
        assertNotNull(updatedPoint)
        assertEquals(2000L, updatedPoint!!.amount)
    }

    @Test
    fun `usePoint - 잔액 부족으로 실패`() {
        // given
        val userId = 1L
        val point = Point(userId, 2000L)
        pointRepository.save(point)

        // when & then
        assertThrows<IllegalArgumentException> {
            pointService.usePoint(userId, 3000L)
        }

        // 포인트가 차감되지 않았는지 확인
        val unchangedPoint = pointRepository.findByUserId(userId)
        assertEquals(2000L, unchangedPoint!!.amount)
    }

    @Test
    fun `chargePoint - 포인트 충전 성공`() {
        // given
        val userId = 1L
        val point = Point(userId, 5000L)
        pointRepository.save(point)

        // when
        pointService.chargePoint(userId, 3000L)

        // then
        val updatedPoint = pointRepository.findByUserId(userId)
        assertNotNull(updatedPoint)
        assertEquals(8000L, updatedPoint!!.amount)
    }

    @Test
    fun `chargePoint - 새 사용자에게 포인트 충전`() {
        // given
        val newUserId = 999L

        // when
        pointService.chargePoint(newUserId, 5000L)

        // then
        val newUserPoint = pointRepository.findByUserId(newUserId)
        assertNotNull(newUserPoint)
        assertEquals(5000L, newUserPoint!!.amount)
    }

    @Test
    fun `chargePoint - 음수 충전액으로 실패`() {
        // given
        val userId = 1L

        // when & then
        assertThrows<IllegalArgumentException> {
            pointService.chargePoint(userId, -1000L)
        }
    }

    @Test
    fun `chargePoint - 최대 충전액 초과로 실패`() {
        // given
        val userId = 1L

        // when & then
        assertThrows<IllegalArgumentException> {
            pointService.chargePoint(userId, 2000000L) // 100만원 초과
        }
    }
} 