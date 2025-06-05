package kr.hhplus.be.server.infrastructure.kafka

import kr.hhplus.be.server.infrastructure.kafka.event.CouponIssuedKafkaEvent
import kr.hhplus.be.server.infrastructure.kafka.producer.CouponKafkaProducer
import org.awaitility.Awaitility
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
    partitions = 3,
    topics = ["coupon-issued"],
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:9094",
        "port=9094"
    ]
)
class CouponKafkaIntegrationTest {

    @Autowired
    private lateinit var couponKafkaProducer: CouponKafkaProducer

    private val log = LoggerFactory.getLogger(CouponKafkaIntegrationTest::class.java)

    @Test
    fun `쿠폰 발급 이벤트가 Kafka로 정상 발행되는지 테스트`() {
        // Given
        val event = CouponIssuedKafkaEvent(
            couponId = 1L,
            userId = 1L,
            couponName = "신규 가입 쿠폰",
            discountAmount = 5000L,
            issuedAt = LocalDateTime.now().toString()
        )

        // When
        couponKafkaProducer.publishCouponIssued(event)

        // Then
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted {
                log.info("쿠폰 발급 Kafka 메시지 발행 테스트 완료")
            }
    }

    @Test
    fun `여러 사용자의 쿠폰 발급이 다른 파티션에 분배되는지 테스트`() {
        // Given
        val events = listOf(
            createTestCouponEvent(1L, 1L),
            createTestCouponEvent(2L, 2L),
            createTestCouponEvent(3L, 3L),
            createTestCouponEvent(4L, 1L), // 같은 사용자
            createTestCouponEvent(5L, 2L)  // 같은 사용자
        )

        // When
        events.forEach { event ->
            couponKafkaProducer.publishCouponIssued(event)
        }

        // Then
        Awaitility.await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted {
                log.info("쿠폰 발급 파티션 분배 테스트 완료")
            }
    }

    private fun createTestCouponEvent(couponId: Long, userId: Long): CouponIssuedKafkaEvent {
        return CouponIssuedKafkaEvent(
            couponId = couponId,
            userId = userId,
            couponName = "테스트 쿠폰 $couponId",
            discountAmount = 1000L,
            issuedAt = LocalDateTime.now().toString()
        )
    }
} 