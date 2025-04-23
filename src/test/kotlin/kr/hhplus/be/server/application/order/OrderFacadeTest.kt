package kr.hhplus.be.server.application.order

import jakarta.transaction.Transactional
import kr.hhplus.be.server.domain.order.*
import kr.hhplus.be.server.domain.point.Point
import kr.hhplus.be.server.domain.point.PointRepository
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import kotlin.test.Test

@SpringBootTest
@Transactional
class OrderFacadeIntegrationTest {
    @Autowired
    private lateinit var pointRepository: PointRepository

    @Autowired
    private lateinit var orderFacade: OrderFacade

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var orderHistoryRepository: OrderHistoryRepository

    private var testProductId: Long = 1L


    @BeforeEach
    fun setup() {
        // 테스트용 상품 데이터 생성
        val product = Product(
            name = "테스트 상품",
            price = 10000L,
            stock = 100L
        )
        val savedProduct = productRepository.save(product)
        testProductId = savedProduct.id

        val point = Point(
            userId = 1L,
            amount = 100000L
        )
        pointRepository.save(point)
    }

    @Test
    fun `주문 처리 성공 케이스 테스트`() {
        // given
        val order = Order(
            userId = 1L,
            orderLines = mutableListOf(
                OrderLine(
                    productId = testProductId,
                    productPrice = 10000L,
                    quantity = 2L
                )
            ),
            orderDateTime = LocalDateTime.now(),
            orderStatus = OrderStatus.주문_요청됨
        )

        // OrderCriteria 생성
        val orderCriteria = OrderCriteria(
            userId = order.userId,
            issuedCouponId = order.issuedCouponId,
            orderLines = order.orderLines.map {
                OrderLineCriteria(
                    productId = it.productId,
                    quantity = it.quantity
                )
            }
        )

        // when
        orderFacade.processOrder(orderCriteria)

        // then
        val processedOrder = orderRepository.findById(1L)
        assertNotNull(processedOrder)
        assertEquals(OrderStatus.결제_완료, processedOrder?.orderStatus)

        // 재고 확인
        val product = productRepository.findById(testProductId)
        assertNotNull(product)
        // 초기 재고가 100이었다면
        assertEquals(98L, product?.stock)  // 2개 감소
    }

    @Test
    fun `재고 부족으로 인한 주문 실패 테스트`() {
        // given
        val order = Order(
            userId = 1L,
            orderLines = mutableListOf(
                OrderLine(
                    productId = testProductId,
                    productPrice = 1L,
                    quantity = 999L  // 재고보다 많은 수량
                )
            ),
            orderDateTime = LocalDateTime.now(),
            orderStatus = OrderStatus.주문_요청됨
        )

        // OrderCriteria 생성
        val orderCriteria = OrderCriteria(
            userId = order.userId,
            issuedCouponId = order.issuedCouponId,
            orderLines = order.orderLines.map {
                OrderLineCriteria(
                    productId = it.productId,
                    quantity = it.quantity
                )
            }
        )

        // when
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> { orderFacade.processOrder(orderCriteria) }

        // 재고는 그대로인지 확인
        val product = productRepository.findById(testProductId)
        assertEquals(100L, product?.stock)  // 초기 재고 유지
    }

    @Test
    fun `주문 상태 변경 흐름 테스트`() {
        // given
        val order = Order(
            userId = 1L,
            orderLines = mutableListOf(
                OrderLine(
                    productId = testProductId,
                    productPrice = 10000L,
                    quantity = 1L
                )
            ),
            orderDateTime = LocalDateTime.now(),
            orderStatus = OrderStatus.주문_요청됨
        )

        // OrderCriteria 생성
        val orderCriteria = OrderCriteria(
            userId = order.userId,
            issuedCouponId = order.issuedCouponId,
            orderLines = order.orderLines.map {
                OrderLineCriteria(
                    productId = it.productId,
                    quantity = it.quantity
                )
            }
        )

        // when
        orderFacade.processOrder(orderCriteria)

        // then
        val processedOrder = orderRepository.findById(2L)
        assertNotNull(processedOrder)

        // OrderHistory 테이블에 상태 변경 이력이 제대로 저장되었는지 확인
        val orderHistories = orderHistoryRepository.findByOrderId(2L)
        assertTrue(orderHistories.isNotEmpty())

        // 상태 변경 순서 확인
        val statusSequence = orderHistories.map { it.orderStatus }
        assertEquals(
            listOf(
                OrderStatus.주문_요청됨,
                OrderStatus.상품_준비중,
                OrderStatus.결제_대기중,
                OrderStatus.결제_완료
            ),
            statusSequence
        )
    }

//    @Test
//    fun `동시에 여러 주문 처리시 재고 정합성 테스트`() {
//        // given
//        val orders = (1..5).map { userId ->
//            Order(
//                userId = userId.toLong(),
//                orderLines = listOf(
//                    OrderLine(
//                        productId = 1L,
//                        productPrice = 10000L,
//                        quantity = 1L
//                    )
//                ),
//                orderDateTime = LocalDateTime.now(),
//                orderStatus = OrderStatus.주문_요청됨
//            )
//        }
//        val savedOrders = orders.map { orderRepository.save(it) }
//
//        // when
//        runBlocking {
//            val jobs = savedOrders.map { order ->
//                async {
//                    // OrderCriteria 생성
//                    val orderCriteria = OrderCriteria(
//                        userId = order.userId,
//                        issuedCouponId = order.issuedCouponId,
//                        orderLines = order.orderLines.map { 
//                            OrderLineCriteria(
//                                productId = it.productId,
//                                quantity = it.quantity
//                            )
//                        }
//                    )
//                    orderFacade.processOrder(orderCriteria)
//                }
//            }
//            jobs.awaitAll()
//        }
//
//        // then
//        // 재고 정확히 감소했는지 확인
//        val product = productRepository.findById(1L)
//        assertEquals(95L, product?.stock)  // 100 - 5
//
//        // 모든 주문이 성공했는지 확인
//        savedOrders.forEach { order ->
//            val processedOrder = orderRepository.findById(order.id)
//            assertEquals(OrderStatus.결제_완료, processedOrder?.orderStatus)
//        }
//    }
}