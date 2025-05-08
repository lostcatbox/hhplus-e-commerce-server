package kr.hhplus.be.server.domain.service.point

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.point.Point
import kr.hhplus.be.server.domain.point.PointRepository
import kr.hhplus.be.server.domain.point.PointService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PointServiceTest {

    @MockK
    private lateinit var pointRepository: PointRepository

    @InjectMockKs
    private lateinit var pointService: PointService

    private val userId = 1L
    private lateinit var point: Point
    private lateinit var emptyPoint: Point

    @BeforeEach
    fun setUp() {
        point = Point(userId = userId, amount = 10000L)
        emptyPoint = Point.EMPTY(userId)

        every { pointRepository.save(any()) } returnsArgument 0

    }

    @Test
    fun `포인트 조회 - 포인트가 존재하는 경우`() {
        every { pointRepository.findByUserId(userId) } returns point

        // When
        val result = pointService.getPoint(userId)

        // Then
        assertEquals(userId, result.userId)
        assertEquals(10000L, result.amount)
        verify(exactly = 1) { pointRepository.findByUserId(userId) }
    }

    @Test
    fun `포인트 조회 - 포인트가 존재하지 않는 경우 빈 포인트 반환`() {

        every { pointRepository.findByUserId(999L) } returns null
        // When
        val result = pointService.getPoint(999L)

        // Then
        assertEquals(999L, result.userId)
        assertEquals(0L, result.amount)
        verify(exactly = 1) { pointRepository.findByUserId(999L) }
    }

    @Test
    fun `포인트 사용`() {
        every { pointRepository.findByUserId(userId) } returns point
        // Given
        val useAmount = 5000L

        // When
        val usePoint = pointService.usePoint(userId, useAmount)

        // Then
        assertEquals(point.amount - useAmount, usePoint.amount)
        verify { pointRepository.save(usePoint) }
    }

    @Test
    fun `포인트 충전`() {
        every { pointRepository.findByUserId(userId) } returns point
        // Given
        val chargeAmount = 5000L

        // When
        val chargePoint = pointService.chargePoint(userId, chargeAmount)

        // Then
        assertEquals(chargeAmount + point.amount, chargePoint.amount)
        verify { pointRepository.save(any()) }
    }

    @Test
    fun `포인트가 없는 사용자에게 포인트 충전`() {
        every { pointRepository.findByUserId(999L) } returns null
        // Given
        val noPointUserId = 999L
        val chargeAmount = 5000L

        // When
        val chargePoint = pointService.chargePoint(noPointUserId, chargeAmount)

        // Then
        assertEquals(chargeAmount, chargePoint.amount)
        verify { pointRepository.save(chargePoint) }
    }
} 