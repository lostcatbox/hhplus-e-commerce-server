package kr.hhplus.be.server.infrastructure.scheduler

import kr.hhplus.be.server.domain.coupon.CouponService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@EnableScheduling
class CouponIssueScheduler(
    private val couponService: CouponService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * 5초 간격으로 대기 중인 쿠폰 발급 요청 처리
     */
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    fun processPendingCouponIssues() {
        try {
            val batchSize = 100 // 한 번에 처리할 최대 요청 수
            val processedCount = couponService.processPendingCouponIssues(batchSize)
            
            if (processedCount > 0) {
                logger.info("쿠폰 발급 요청 처리 완료: $processedCount 건")
            }
        } catch (e: Exception) {
            logger.error("쿠폰 발급 요청 처리 중 오류 발생", e)
        }
    }
} 