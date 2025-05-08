package kr.hhplus.be.server.concurrency

import kr.hhplus.be.server.domain.coupon.CouponRepository
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.PercentageCoupon
import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
class FirstComeCouponConcurrencyTest {

    @Autowired
    private lateinit var couponService: CouponService

    @Autowired
    private lateinit var couponRepository: CouponRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private val threadCount = 50
    private val limitedStock = 10L
    private lateinit var testUsers: List<User>
    private lateinit var limitedCoupon: PercentageCoupon

    @BeforeEach
    @Transactional
    fun setup() {
        // 테스트용 사용자 생성 (50명)
        testUsers = List(threadCount) { idx ->
            userRepository.save(
                User(
                    name = "쿠폰 테스트 사용자 $idx",
                    email = "coupon$idx@example.com",
                    password = "password",
                    active = true
                )
            )
        }

        // 한정 수량 쿠폰 생성 (10개만 발급 가능)
        limitedCoupon = couponRepository.save(
            PercentageCoupon(
                name = "선착순 할인 쿠폰",
                stock = limitedStock,
                active = true,
                percent = 10.0 // 10% 할인
            )
        ) as PercentageCoupon
    }

    @Test
    fun `동시에 여러 사용자가 한정 수량 쿠폰을 요청할 때 일관성 검증`() {
        // Given
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        // When - 50명의 사용자가 동시에 10개 한정 쿠폰 발급 요청
        repeat(threadCount) { idx ->
            val userId = testUsers[idx].id
            executorService.submit {
                try {
                    couponService.issuedCoupon(userId, limitedCoupon.id)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    println("쿠폰 발급 실패 (userId: $userId): ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 요청이 처리될 때까지 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()

        // Then
        // 최종 쿠폰 재고 확인
        val finalCoupon = couponRepository.findById(limitedCoupon.id)

        println("=== 선착순 쿠폰 동시성 테스트 결과 ===")
        println("초기 쿠폰 수량: $limitedStock")
        println("요청한 사용자 수: $threadCount")
        println("성공한 요청 수: ${successCount.get()}")
        println("실패한 요청 수: ${failCount.get()}")
        println("최종 남은 쿠폰 수량: ${finalCoupon.stock}")

        // 쿠폰 발급 성공 수 + 남은 재고 = 초기 재고 검증
        assertEquals(
            limitedStock, successCount.get() + finalCoupon.stock,
            "성공한 쿠폰 발급 수와 남은 재고의 합이 초기 재고와 일치해야 함"
        )

        // 모든 요청의 합이 스레드 수와 일치해야 함
        assertEquals(
            threadCount, successCount.get() + failCount.get(),
            "모든 요청이 성공 또는 실패로 처리되어야 함"
        )

        // 성공한 요청 수가 초기 재고 이하여야 함
        assertTrue(
            successCount.get() <= limitedStock,
            "성공한 쿠폰 발급은 초기 재고($limitedStock)를 초과할 수 없음"
        )
    }
} 