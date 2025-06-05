package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.infrastructure.kafka.event.CouponIssuedKafkaEvent
import kr.hhplus.be.server.infrastructure.kafka.producer.CouponKafkaProducer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponFacade(
    private val userService: UserService,
    private val couponService: CouponService,
    private val couponKafkaProducer: CouponKafkaProducer
) {
    private val log = LoggerFactory.getLogger(CouponFacade::class.java)

    fun getUserCouponList(userId: Long): List<Coupon> {
        userService.checkActiveUser(userId)
        return couponService.getUserCouponList(userId)
    }

    @Transactional
    fun issuedCouponTo(userId: Long, couponId: Long) {
        userService.checkActiveUser(userId)
        
        // 쿠폰 발급 (핵심 비즈니스 로직)
        val issuedResult = couponService.issuedCoupon(userId, couponId)
        
        try {
            // Kafka 이벤트 발행 (비동기 처리)
            val kafkaEvent = CouponIssuedKafkaEvent(
                couponId = issuedResult.issuedCoupon.id,
                userId = userId,
                couponName = issuedResult.remainingCoupon.name,
                discountAmount = issuedResult.remainingCoupon.getDiscountAmount(),
                issuedAt = issuedResult.issuedCoupon.issuedAt.toString()
            )
            
            couponKafkaProducer.publishCouponIssued(kafkaEvent)
            
            log.info("쿠폰 발급 완료 및 이벤트 발행: couponId={}, userId={}", couponId, userId)
            
        } catch (e: Exception) {
            // Kafka 발행 실패 시에도 핵심 비즈니스 로직에는 영향을 주지 않음
            log.warn("쿠폰 발급 이벤트 발행 실패: couponId={}, userId={}, error={}", 
                couponId, userId, e.message, e)
        }
    }
}