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

        every { pointRepository.findByUserId(userId) } returns point
        every { pointRepository.findByUserId(999L) } returns null
        every { pointRepository.save(any()) } returnsArgument 0
    }

    @Test
    fun `포인트 조회 - 포인트가 존재하는 경우`() {
        // When
        val result = pointService.getPoint(userId)

        // Then
        assertEquals(userId, result.userId)
        assertEquals(10000L, result.amount)
        verify(exactly = 1) { pointRepository.findByUserId(userId) }
    }

    @Test
    fun `포인트 조회 - 포인트가 존재하지 않는 경우 빈 포인트 반환`() {
        // When
        val result = pointService.getPoint(999L)

        // Then
        assertEquals(999L, result.userId)
        assertEquals(0L, result.amount)
        verify(exactly = 1) { pointRepository.findByUserId(999L) }
    }

    @Test
    fun `포인트 사용`() {
        // Given
        val useAmount = 5000L
        val expectedPoint = Point(userId = point.userId, amount = 5000L)
//        every { point.usePoint(useAmount) } returns expectedPoint

        // When
        pointService.usePoint(userId, useAmount)

        // Then
        verify(exactly = 1) { pointRepository.findByUserId(userId) }
//        verify(exactly = 1) { point.usePoint(useAmount) }
//        verify(exactly = 1) { pointRepository.save(expectedPoint) }
    }

    @Test
    fun `포인트 충전`() {
        // Given
        val chargeAmount = 5000L
        val expectedPoint = Point(userId = point.userId, amount = 15000L)
//        every { point.chargePoint(chargeAmount) } returns expectedPoint

        // When
        pointService.chargePoint(userId, chargeAmount)

        // Then
        verify(exactly = 1) { pointRepository.findByUserId(userId) }
//        verify(exactly = 1) { point.chargePoint(chargeAmount) }
//        verify(exactly = 1) { pointRepository.save(expectedPoint) }
    }

    @Test
    fun `포인트가 없는 사용자에게 포인트 충전`() {
        // Given
        val noPointUserId = 999L
        val chargeAmount = 5000L
        val expectedPoint = Point(
            userId = emptyPoint.userId, amount = 5000L
        )
//        every { emptyPoint.chargePoint(chargeAmount) } returns expectedPoint

        // When
        pointService.chargePoint(noPointUserId, chargeAmount)

        // Then
        verify(exactly = 1) { pointRepository.findByUserId(noPointUserId) }
        verify(exactly = 1) { pointRepository.save(any()) }
    }
} 