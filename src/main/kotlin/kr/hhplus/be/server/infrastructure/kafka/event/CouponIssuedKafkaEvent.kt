package kr.hhplus.be.server.infrastructure.kafka.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * Kafka로 전송되는 쿠폰 발급 완료 이벤트
 */
data class CouponIssuedKafkaEvent(
    @JsonProperty("couponId")
    val couponId: Long,

    @JsonProperty("userId")
    val userId: Long,

    @JsonProperty("couponName")
    val couponName: String,

    @JsonProperty("discountAmount")
    val discountAmount: Long,

    @JsonProperty("issuedAt")
    val issuedAt: String,

    @JsonProperty("eventType")
    val eventType: String = "COUPON_ISSUED",

    @JsonProperty("timestamp")
    val timestamp: String = LocalDateTime.now().toString()
) 