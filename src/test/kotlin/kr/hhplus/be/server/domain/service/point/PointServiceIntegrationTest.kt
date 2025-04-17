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
    fun `usePoint - 포인트 부족시 예외 발생`() {
        // given
        val userId = 1L
        val point = Point(userId, 1000L)
        pointRepository.save(point)

        // when & then
        assertThrows<IllegalArgumentException> {
            pointService.usePoint(userId, 2000L)
        }

        // 포인트 변경되지 않았는지 확인
        val updatedPoint = pointRepository.findByUserId(userId)
        assertEquals(1000L, updatedPoint!!.amount)
    }

    @Test
    fun `chargePoint - 포인트 충전 성공`() {
        // given
        val userId = 1L
        val point = Point(userId, 1000L)
        pointRepository.save(point)

        // when
        pointService.chargePoint(userId, 5000L)

        // then
        val updatedPoint = pointRepository.findByUserId(userId)
        assertNotNull(updatedPoint)
        assertEquals(6000L, updatedPoint!!.amount)
    }

    @Test
    fun `chargePoint - 음수 충전 시도시 예외 발생`() {
        // given
        val userId = 1L
        val point = Point(userId, 1000L)
        pointRepository.save(point)

        // when & then
        assertThrows<IllegalArgumentException> {
            pointService.chargePoint(userId, -1000L)
        }
    }

    @Test
    fun `chargePoint - 최대 충전 금액 초과시 예외 발생`() {
        // given
        val userId = 1L
        val point = Point(userId, 1000L)
        pointRepository.save(point)

        // when & then
        assertThrows<IllegalArgumentException> {
            pointService.chargePoint(userId, 1500000L) // 100만원 초과
        }
    }
} 