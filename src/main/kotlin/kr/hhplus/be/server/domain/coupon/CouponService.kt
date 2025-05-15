package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.support.distributedlock.DistributedLock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val issuedCouponRepository: IssuedCouponRepository,
    private val couponRedisRepository: CouponRedisRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getUserCouponList(userId: Long): List<Coupon> {
        return couponRepository.findAllByUserId(userId)
    }

    @Transactional
    @DistributedLock(key = "issued_coupon_lock")
    fun issuedCoupon(userId: Long, couponId: Long) {
        // 비관적 락을 사용하여 쿠폰 조회
        val coupon = couponRepository.findByIdWithPessimisticLock(couponId)
        val issuedCouponAndCoupon = coupon.issueTo(userId)
        couponRepository.save(issuedCouponAndCoupon)
    }

    fun findByIssuedCouponId(issuedCouponId: Long): IssuedCouponAndCouponVO {
        val issuedCouponById = couponRepository.findIssuedCouponById(issuedCouponId)
        val coupon = couponRepository.findById(issuedCouponById.couponId)
        return IssuedCouponAndCouponVO(
            issuedCoupon = issuedCouponById,
            coupon = coupon
        )
    }

    fun useIssuedCoupon(issuedCouponId: Long?): Coupon? {
        if (issuedCouponId == null) {
            return null
        }
        val issuedCouponById = couponRepository.findIssuedCouponById(issuedCouponId)
        couponRepository.save(issuedCouponById.useCoupon())
        return couponRepository.findById(issuedCouponById.couponId)
    }

    /**
     * 쿠폰 생성 및 Redis 초기화
     */
    @Transactional
    fun createCoupon(name: String, stock: Long, amount: Long? = null, percent: Double? = null): Coupon {
        // 쿠폰 타입 결정 및 생성
        val coupon = when {
            amount != null -> AmountCoupon(
                name = name,
                stock = stock,
                active = true,
                amount = amount
            )

            percent != null -> PercentageCoupon(
                name = name,
                stock = stock,
                active = true,
                percent = percent
            )

            else -> throw IllegalArgumentException("쿠폰 타입(금액 또는 퍼센트)을 지정해야 합니다.")
        }

        // DB에 쿠폰 저장
        val savedCoupon = couponRepository.save(coupon)

        // Redis에 재고 초기화
        couponRedisRepository.initializeStock(savedCoupon.id, stock)

        return savedCoupon
    }

    /**
     * 선착순 쿠폰 발급 요청 (Redis 기반)
     */
    fun requestCouponIssue(couponId: Long, userId: Long): Boolean {
        // Redis에서 재고 차감 및 발급 요청 등록
        return couponRedisRepository.decreaseStockAndRegisterUser(couponId, userId)
    }

    /**
     * 쿠폰 발급 처리 (배치 처리용)
     */
    @Transactional
    fun processPendingCouponIssues(batchSize: Int = 100): Int {
        // 1. Redis에서 처리되지 않은 발급 요청 가져오기
        val pendingRequests = couponRedisRepository.getPendingIssueRequests(batchSize)
        var processedCount = 0

        pendingRequests.forEach { (couponId, userId) ->
            try {
                // 2. DB에서 쿠폰 조회
                val coupon = couponRepository.findById(couponId)
                if (coupon != null && coupon.isAvailable()) {
                    // 3. 쿠폰 발급 처리
                    val (issuedCoupon, remainingCoupon) = coupon.issueTo(userId)

                    // 4. DB 업데이트
                    couponRepository.save(remainingCoupon)
                    issuedCouponRepository.save(issuedCoupon)

                    // 5. Redis에서 처리 완료 표시
                    couponRedisRepository.markRequestProcessed(couponId, userId)

                    processedCount++
                } else {
                    // 쿠폰이 없거나 비활성화된 경우 처리
                    logger.warn("쿠폰을 발급할 수 없습니다. couponId: $couponId, userId: $userId")
                    couponRedisRepository.markRequestProcessed(couponId, userId)
                }
            } catch (e: Exception) {
                logger.error("쿠폰 발급 처리 중 오류 발생. couponId: $couponId, userId: $userId", e)
                // 실패한 요청은 다음 배치에서 재처리 (대기열에서 제거하지 않음)
            }
        }

        return processedCount
    }

    /**
     * 쿠폰 발급 가능 여부 확인
     */
    fun canIssueCoupon(couponId: Long, userId: Long): Boolean {
        // Redis에서 재고 확인
        val stock = couponRedisRepository.getStock(couponId)
        if (stock <= 0) {
            return false
        }

        // 중복 발급 확인
        if (couponRedisRepository.hasUserIssuedCoupon(couponId, userId)) {
            return false
        }

        return true
    }

    /**
     * 쿠폰 재고 조회 (Redis 우선, 없으면 DB에서 조회)
     */
    fun getCouponStock(couponId: Long): Long {
        // 1. Redis에서 먼저 조회 시도
        val redisStock = couponRedisRepository.getStock(couponId)
        
        // 2. Redis에 재고 정보가 있으면 해당 값 반환
        if (redisStock > 0) {
            return redisStock
        }
        
        // 3. Redis에 재고 정보가 없거나 0이면 DB에서 조회
        val coupon = couponRepository.findById(couponId)
        
        // 4. DB에서 조회된 값 반환 (없으면 0)
        return coupon?.stock ?: 0
    }

}

data class IssuedCouponAndCouponVO(
    val issuedCoupon: IssuedCoupon,
    val coupon: Coupon
)