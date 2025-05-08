package kr.hhplus.be.server.domain.service.coupon

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ExtendWith(MockKExtension::class)
@Transactional
class CouponServiceTest {

    @MockK
    private lateinit var couponRepository: CouponRepository

    @InjectMockKs
    private lateinit var couponService: CouponService

    private val userId = 1L
    private val couponId = 1L
    private lateinit var coupon: PercentageCoupon
    private lateinit var amountCoupon: AmountCoupon
    private lateinit var issuedCoupon: IssuedCoupon
    private lateinit var userCoupons: List<Coupon>
    private lateinit var updatedIssuedCoupon: IssuedCoupon

    @BeforeEach
    fun setUp() {
        // Percentage 쿠폰 목
        coupon = mockk<PercentageCoupon>()
        every { coupon.id } returns couponId
        every { coupon.name } returns "테스트 쿠폰"
        every { coupon.stock } returns 10L
        every { coupon.active } returns true
        every { coupon.percent } returns 10.0

        // Amount 쿠폰 목
        amountCoupon = mockk<AmountCoupon>()
        every { amountCoupon.id } returns 2L
        every { amountCoupon.name } returns "테스트 쿠폰 2"
        every { amountCoupon.stock } returns 10L
        every { amountCoupon.active } returns true
        every { amountCoupon.amount } returns 5000L

        // 발급된 쿠폰 목
        issuedCoupon = mockk<IssuedCoupon>()
        every { issuedCoupon.id } returns 1L
        every { issuedCoupon.couponId } returns couponId
        every { issuedCoupon.userId } returns userId
        every { issuedCoupon.isUsed } returns false

        // 업데이트된 발급 쿠폰 목
        updatedIssuedCoupon = mockk<IssuedCoupon>()
        every { updatedIssuedCoupon.id } returns 1L
        every { updatedIssuedCoupon.couponId } returns couponId
        every { updatedIssuedCoupon.userId } returns userId
        every { updatedIssuedCoupon.isUsed } returns true

        userCoupons = listOf(
            coupon,
            amountCoupon
        )

        // 쿠폰 발급 관련 모킹
        val issueCouponAndIssuedCoupon = mockk<IssueCouponAndIssuedCoupon>()
        every { coupon.issueTo(any()) } returns issueCouponAndIssuedCoupon
        every { coupon.isAvailable() } returns true

        // 레포지토리 응답 모킹
        every { couponRepository.findAllByUserId(userId) } returns userCoupons
        every { couponRepository.findById(couponId) } returns coupon
        every { couponRepository.findByIdWithPessimisticLock(couponId) } returns coupon
        every { couponRepository.findIssuedCouponById(any()) } returns issuedCoupon
        every { couponRepository.save(any<IssueCouponAndIssuedCoupon>()) } returns Unit
        every { couponRepository.save(any<IssuedCoupon>()) } returns updatedIssuedCoupon
        every { issuedCoupon.useCoupon() } returns updatedIssuedCoupon
    }

    @Test
    fun `사용자의 쿠폰 목록 조회`() {
        // When
        val result = couponService.getUserCouponList(userId)

        // Then
        assertEquals(2, result.size)
        assertEquals(couponId, result[0].id)
        assertEquals("테스트 쿠폰", result[0].name)
        verify(exactly = 1) { couponRepository.findAllByUserId(userId) }
    }

    @Test
    fun `쿠폰 발급`() {
        // Given
        val issueCouponAndIssuedCoupon = mockk<IssueCouponAndIssuedCoupon>()
        every { coupon.issueTo(userId) } returns issueCouponAndIssuedCoupon

        // When
        couponService.issuedCoupon(userId, couponId)

        // Then
        verify(exactly = 1) { couponRepository.findByIdWithPessimisticLock(couponId) }
        verify(exactly = 1) { coupon.issueTo(userId) }
        verify(exactly = 1) { couponRepository.save(issueCouponAndIssuedCoupon) }
    }

    @Test
    fun `동시에 여러 사용자가 쿠폰 발급 요청 시 비관적 락으로 인해 순차적으로 처리`() {
        // Given
        val threadCount = 5
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // 각 쿠폰 발급 결과 및 쿠폰 객체
        val mockCoupons = List(threadCount) { mockk<PercentageCoupon>() }
        val mockResults = List(threadCount) { mockk<IssueCouponAndIssuedCoupon>() }

        // 비관적 락 호출 시 쿠폰 반환 설정
        every { couponRepository.findByIdWithPessimisticLock(any()) } returnsMany mockCoupons

        // 각 모의 쿠폰에 공통 설정
        mockCoupons.forEach { mockCoupon ->
            every { mockCoupon.id } returns couponId
            every { mockCoupon.active } returns true
            every { mockCoupon.isAvailable() } returns true
        }

        // 쿠폰 발급 및 저장 설정
        mockCoupons.forEachIndexed { idx, mockCoupon ->
            every { mockCoupon.issueTo(any()) } returns mockResults[idx]
            every { couponRepository.save(mockResults[idx]) } returns Unit
        }

        // When - 여러 스레드에서 동시에 쿠폰 발급 요청
        repeat(threadCount) { threadIdx ->
            val userIdForThread = threadIdx + 1L
            executorService.submit {
                try {
                    couponService.issuedCoupon(userIdForThread, couponId)
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 작업이 완료될 때까지 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()

        // Then
        // 비관적 락 메서드가 호출된 횟수 검증
        verify(exactly = threadCount) { couponRepository.findByIdWithPessimisticLock(any()) }

        // 각 모의 쿠폰에 대해 issueTo 메서드가 호출된 횟수 검증
        mockCoupons.forEach { mockCoupon ->
            verify(exactly = 1) { mockCoupon.issueTo(any()) }
        }

        // 각 결과가 저장되었는지 검증
        mockResults.forEach { result ->
            verify(exactly = 1) { couponRepository.save(result) }
        }
    }

    @Test
    fun `발급된 쿠폰 ID로 쿠폰 정보 조회`() {
        // When
        val result = couponService.findByIssuedCouponId(1L)

        // Then
        assertEquals(issuedCoupon, result.issuedCoupon)
        assertEquals(coupon, result.coupon)
        verify(exactly = 1) { couponRepository.findIssuedCouponById(1L) }
        verify(exactly = 1) { couponRepository.findById(couponId) }
    }

    @Test
    fun `쿠폰 사용 처리 - 발급된 쿠폰이 있는 경우`() {
        // When
        val result = couponService.useIssuedCoupon(1L)

        // Then
        assertEquals(coupon, result)
        verify(exactly = 1) { couponRepository.findIssuedCouponById(1L) }
        verify(exactly = 1) { issuedCoupon.useCoupon() }
        verify(exactly = 1) { couponRepository.save(any<IssuedCoupon>()) }
        verify(exactly = 1) { couponRepository.findById(couponId) }
    }

    @Test
    fun `쿠폰 사용 처리 - 발급된 쿠폰이 없는 경우`() {
        // When
        val result = couponService.useIssuedCoupon(null)

        // Then
        assertEquals(null, result)
        verify(exactly = 0) { couponRepository.findIssuedCouponById(any()) }
        verify(exactly = 0) { issuedCoupon.useCoupon() }
        verify(exactly = 0) { couponRepository.findById(any()) }
    }
}