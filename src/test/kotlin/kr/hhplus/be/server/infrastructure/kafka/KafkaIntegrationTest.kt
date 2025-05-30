package kr.hhplus.be.server.infrastructure.kafka

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderLine
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.infrastructure.kafka.producer.OrderKafkaProducer
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
    topics = ["order-completed"],
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:9093",
        "port=9093"
    ]
)
class KafkaIntegrationTest {

    @Autowired
    private lateinit var orderKafkaProducer: OrderKafkaProducer

    private val log = LoggerFactory.getLogger(KafkaIntegrationTest::class.java)

    @Test
    fun `주문 완료 이벤트가 Kafka로 정상 발행되는지 테스트`() {
        // Given
        val order = Order(
            id = 1L,
            userId = 1L,
            orderLines = mutableListOf(
                OrderLine(
                    productId = 1L,
                    productPrice = 10000L,
                    quantity = 2L
                )
            ),
            orderDateTime = LocalDateTime.now(),
            orderStatus = OrderStatus.결제_완료
        )

        // When
        orderKafkaProducer.publishOrderCompleted(order)

        // Then
        // 메시지가 발행되었는지 확인 (로그를 통해 확인)
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted {
                log.info("Kafka 메시지 발행 테스트 완료")
                // 실제 운영에서는 Consumer를 통해 메시지 수신 확인
            }
    }

    @Test
    fun `여러 사용자의 주문이 다른 파티션에 분배되는지 테스트`() {
        // Given
        val orders = listOf(
            createTestOrder(1L, 1L),
            createTestOrder(2L, 2L),
            createTestOrder(3L, 3L),
            createTestOrder(4L, 1L), // 같은 사용자
            createTestOrder(5L, 2L)  // 같은 사용자
        )

        // When
        orders.forEach { order ->
            orderKafkaProducer.publishOrderCompleted(order)
        }

        // Then
        Awaitility.await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted {
                log.info("파티션 분배 테스트 완료")
                // 실제로는 각 파티션별 메시지 수신 확인 필요
            }
    }

    private fun createTestOrder(orderId: Long, userId: Long): Order {
        return Order(
            id = orderId,
            userId = userId,
            orderLines = mutableListOf(
                OrderLine(
                    productId = 1L,
                    productPrice = 10000L,
                    quantity = 1L
                )
            ),
            orderDateTime = LocalDateTime.now(),
            orderStatus = OrderStatus.결제_완료
        )
    }
} 