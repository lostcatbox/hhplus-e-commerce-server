package kr.hhplus.be.server.concurrency

import kr.hhplus.be.server.domain.point.Point
import kr.hhplus.be.server.domain.point.PointRepository
import kr.hhplus.be.server.domain.point.PointService
import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("test")
class PointBalanceConcurrencyTest {

    @Autowired
    private lateinit var pointService: PointService

    @Autowired
    private lateinit var pointRepository: PointRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private val initialAmount = 10000L
    private val threadCount = 50
    private val withdrawAmount = 100L
    private lateinit var testUser: User
    private lateinit var initialPoint: Point

    @BeforeEach
    @Transactional
    fun setup() {
        // 테스트용 사용자 생성
        testUser = userRepository.save(
            User(name = "포인트 테스트 사용자", email = "test@example.com", password = "password", active = true)
        )

        // 테스트용 포인트 생성 (초기 금액: 10000)
        initialPoint = pointRepository.save(Point(userId = testUser.id, amount = initialAmount))
    }

    @Test
    fun `동시에 여러 요청이 포인트를 사용할 때 데이터 일관성 검증`() {
        // Given
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        // When - 여러 스레드에서 동시에 포인트 사용 요청
        repeat(threadCount) {
            executorService.submit {
                try {
                    pointService.usePoint(testUser.id, withdrawAmount)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    println("포인트 사용 실패: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 요청이 처리될 때까지 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()

        // Then
        // 최종 포인트 잔액 확인
        val finalPoint = pointRepository.findByUserId(testUser.id)
        assertNotNull(finalPoint)

        println("=== 포인트 사용 동시성 테스트 결과 ===")
        println("초기 잔액: $initialAmount")
        println("차감 요청 금액: ${withdrawAmount * threadCount}")
        println("성공한 요청 수: ${successCount.get()}")
        println("실패한 요청 수: ${failCount.get()}")
        println("최종 잔액: ${finalPoint!!.amount}")

        // 초기 잔액 - (성공한 요청 수 * 차감 금액) = 현재 잔액 검증
        assertEquals(
            initialAmount - (successCount.get() * withdrawAmount), finalPoint.amount,
            "포인트가 정확히 차감되어야 함"
        )

        // 모든 요청의 합이 스레드 수와 일치해야 함
        assertEquals(
            threadCount, successCount.get() + failCount.get(),
            "모든 요청이 성공 또는 실패로 처리되어야 함"
        )
    }

    @Test
    fun `동시에 여러 요청이 포인트를 충전할 때 데이터 일관성 검증`() {
        // Given
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val chargeAmount = 100L

        // When - 여러 스레드에서 동시에 포인트 충전 요청
        repeat(threadCount) {
            executorService.submit {
                try {
                    pointService.chargePoint(testUser.id, chargeAmount)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    println("포인트 충전 실패: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 요청이 처리될 때까지 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()

        // Then
        // 최종 포인트 잔액 확인
        val finalPoint = pointRepository.findByUserId(testUser.id)
        assertNotNull(finalPoint)

        println("=== 포인트 충전 동시성 테스트 결과 ===")
        println("초기 잔액: $initialAmount")
        println("충전 요청 금액: ${chargeAmount * threadCount}")
        println("성공한 요청 수: ${successCount.get()}")
        println("실패한 요청 수: ${failCount.get()}")
        println("최종 잔액: ${finalPoint!!.amount}")

        // 초기 잔액 + (성공한 요청 수 * 충전 금액) = 현재 잔액 검증
        assertEquals(
            initialAmount + (successCount.get() * chargeAmount), finalPoint.amount,
            "포인트가 정확히 충전되어야 함"
        )

        // 모든 요청의 합이 스레드 수와 일치해야 함
        assertEquals(
            threadCount, successCount.get() + failCount.get(),
            "모든 요청이 성공 또는 실패로 처리되어야 함"
        )
    }
} 