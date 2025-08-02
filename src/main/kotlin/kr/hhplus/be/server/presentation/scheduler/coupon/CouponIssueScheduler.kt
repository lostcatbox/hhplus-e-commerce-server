package kr.hhplus.be.server.presentation.scheduler.coupon

import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.domain.coupon.CouponRedisRepository
import kr.hhplus.be.server.domain.coupon.CouponRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Component
@EnableScheduling
class CouponIssueScheduler(
    private val couponFacade: CouponFacade,
    private val couponRedisRepository: CouponRedisRepository,
    private val couponRepository: CouponRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 5초 간격으로 대기 중인 쿠폰 발급 요청 처리
     * CouponFacade를 사용하여 Kafka 이벤트도 함께 발행
     */
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    @Transactional
    fun processPendingCouponIssues() {
        try {
            val batchSize = 100 // 한 번에 처리할 최대 요청 수
            val pendingRequests = couponRedisRepository.getPendingIssueRequests(batchSize)
            var processedCount = 0

            pendingRequests.forEach { (couponId, userId) ->
                try {
                    // DB에서 쿠폰 조회하여 발급 가능한지 확인
                    val coupon = couponRepository.findById(couponId)
                    if (coupon != null && coupon.isAvailable()) {
                        // CouponFacade를 사용하여 쿠폰 발급 (Kafka 이벤트도 함께 발행)
                        couponFacade.issuedCouponTo(userId, couponId)
                        
                        // Redis에서 처리 완료 표시
                        couponRedisRepository.markRequestProcessed(couponId, userId)
                        
                        processedCount++
                        
                        logger.debug("쿠폰 발급 및 이벤트 발행 완료: couponId={}, userId={}", couponId, userId)
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

            if (processedCount > 0) {
                logger.info("쿠폰 발급 요청 처리 완료: $processedCount 건 (Kafka 이벤트 포함)")
            }
        } catch (e: Exception) {
            logger.error("쿠폰 발급 요청 처리 중 오류 발생", e)
        }
    }
}