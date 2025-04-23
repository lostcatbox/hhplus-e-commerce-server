//package kr.hhplus.be.server.application.order
//
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.runBlocking
//import kr.hhplus.be.server.domain.order.Order
//import kr.hhplus.be.server.domain.order.OrderLine
//import kr.hhplus.be.server.domain.order.OrderRepository
//import kr.hhplus.be.server.domain.point.Point
//import kr.hhplus.be.server.domain.point.PointRepository
//import kr.hhplus.be.server.domain.product.Product
//import kr.hhplus.be.server.domain.product.ProductRepository
//import kr.hhplus.be.server.domain.user.User
//import kr.hhplus.be.server.domain.user.UserRepository
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.ActiveProfiles
//import org.springframework.transaction.annotation.Transactional
//import java.time.LocalDateTime
//import java.util.concurrent.ConcurrentHashMap
//import java.util.concurrent.CountDownLatch
//import java.util.concurrent.Executors
//import java.util.concurrent.TimeUnit
//import kotlin.random.Random
//
//@SpringBootTest
//@ActiveProfiles("test")
//class OrderConcurrencyTest {
//
//    @Autowired
//    private lateinit var orderRepository: OrderRepository
//
//    @Autowired
//    private lateinit var orderFacade: OrderFacade
//
//    @Autowired
//    private lateinit var productRepository: ProductRepository
//
//    @Autowired
//    private lateinit var userRepository: UserRepository
//
//    @Autowired
//    private lateinit var pointRepository: PointRepository
//
//    private val totalRequests = 1000
//    private val timeoutSeconds = 10L
//    private val concurrentUsers = 100
//
//    @BeforeEach
//    @Transactional
//    fun setup() {
//        // 테스트 데이터 준비
//        // 1. 상품 데이터
//        val products = listOf(
//            Product(name = "테스트 상품 1", price = 10000L, stock = 2000L),
//            Product(name = "테스트 상품 2", price = 20000L, stock = 2000L),
//            Product(name = "테스트 상품 3", price = 15000L, stock = 2000L)
//        )
//        productRepository.save(products[0])
//        productRepository.save(products[1])
//        productRepository.save(products[2])
//
//        // 2. 사용자 데이터
//        val users = listOf(
//            User(name = "테스트 사용자 1", email = "user1@test.com", password = "password", active = true),
//            User(name = "테스트 사용자 2", email = "user2@test.com", password = "password", active = true)
//        )
//        userRepository.save(users[0])
//        userRepository.save(users[1])
//
//        // 3. 포인트 데이터
//        val points = listOf(
//            Point(userId = 1L, amount = 10_000_000L),
//            Point(userId = 2L, amount = 10_000_000L)
//        )
//        pointRepository.save(points[0])
//        pointRepository.save(points[1])
//    }
//
//    @Test
//    fun `초당 100개, 총 1000개 주문 10초 내 처리 테스트`() {
//        // 테스트 결과 저장용 맵
//        val results = ConcurrentHashMap<Long, Boolean>()
//        val latch = CountDownLatch(totalRequests)
//
//        // 스레드풀 생성
//        val executor = Executors.newFixedThreadPool(concurrentUsers)
//
//        // 시작 시간 기록
//        val startTime = System.currentTimeMillis()
//
//        // 1000개 요청 생성 및 실행
//        repeat(totalRequests) { index ->
//            executor.submit {
//                try {
//                    // 랜덤 주문 데이터 생성
//                    val userId = if (Random.nextBoolean()) 1L else 2L
//                    val productId = Random.nextInt(1, 4).toLong()
//                    val quantity = Random.nextInt(1, 6).toLong()
//
//                    // 주문 생성
//                    val orderLines = mutableListOf(
//                        OrderLine(
//                            productId = productId,
//                            productPrice = when (productId) {
//                                1L -> 10000L
//                                2L -> 20000L
//                                else -> 15000L
//                            },
//                            quantity = quantity
//                        )
//                    )
//
//                    val order = Order(
//                        userId = userId,
//                        orderLines = orderLines,
//                        orderDateTime = LocalDateTime.now()
//                    )
//
//                    // 주문 처리
//                    val savedOrder = orderFacade.processOrder(order)
//
//                } catch (e: Exception) {
//                    println("주문 처리 실패: ${e.message}")
//                } finally {
//                    latch.countDown()
//                }
//            }
//        }
//
//        // 모든 요청이 처리되거나 타임아웃될 때까지 대기
//        val completed = latch.await(timeoutSeconds, TimeUnit.SECONDS)
//
//        // 종료 시간 기록
//        val endTime = System.currentTimeMillis()
//        val elapsedSeconds = (endTime - startTime) / 1000.0
//
//        // 스레드풀 종료
//        executor.shutdown()
//
//        // 결과 검증
//        println("요청 처리 완료: ${results.size}/${totalRequests}")
//        println("소요 시간: $elapsedSeconds 초")
//        println("초당 처리량: ${results.size / elapsedSeconds} 요청/초")
//
//        // 테스트 결과 검증
//        if (completed) {
//            assertTrue(results.size >= totalRequests * 0.95, "95% 이상 요청이 성공해야 함")
//        } else {
//            fail("$timeoutSeconds 초 내에 모든 요청을 처리하지 못함")
//        }
//
//        // 데이터 정합성 검증
//        validateDataConsistency()
//    }
//
//    @Test
//    fun `동시에 여러 주문이 동일 상품 구매시 재고 정합성 테스트`() = runBlocking {
//        // 테스트용 상품 생성
//        val product = Product(name = "한정판 상품", price = 10000L, stock = 100L)
//        val savedProduct = productRepository.save(product)
//
//        // 코루틴으로 동시 요청 처리
//        val requests = List(200) { index ->
//            async(Dispatchers.IO) {
//                try {
//                    val userId = if (index % 2 == 0) 1L else 2L
//
//                    val orderLines = mutableListOf(
//                        OrderLine(
//                            productId = savedProduct.id,
//                            productPrice = savedProduct.price,
//                            quantity = 1L // 각 요청당 1개씩 구매
//                        )
//                    )
//
//                    val order = Order(
//                        userId = userId,
//                        orderLines = orderLines,
//                        orderDateTime = LocalDateTime.now()
//                    )
//
//                    orderFacade.processOrder(order)
//                    true
//                } catch (e: Exception) {
//                    println("주문 실패: ${e.message}")
//                    false
//                }
//            }
//        }
//
//        // 모든 요청 결과 대기
//        val results = requests.awaitAll()
//
//        // 성공한 요청 수
//        val successCount = results.count { it }
//        println("성공한 요청: $successCount/200")
//
//        // 상품 재고 확인
//        val updatedProduct = productRepository.findById(savedProduct.id)
//
//        // 정합성 검증: 재고는 정확히 (초기 재고 - 성공한 주문 수)여야 함
//        assertEquals(
//            100L - successCount, updatedProduct?.stock,
//            "재고가 정확히 차감되어야 함 (초기: 100, 성공한 주문: $successCount)"
//        )
//    }
//
//    /**
//     * 데이터 정합성 검증
//     */
//    private fun validateDataConsistency() {
//        // 1. 상품별 주문 수량 합계 조회
//        val orders = orderRepository.findAll()
//
//        // 상품별 주문 수량 집계
//        val productQuantities = mutableMapOf<Long, Long>()
//
//        orders.forEach { order ->
//            order.orderLines.forEach { line ->
//                val currentQuantity = productQuantities.getOrDefault(line.productId, 0L)
//                productQuantities[line.productId] = currentQuantity + line.quantity
//            }
//        }
//
//        // 2. 현재 상품 재고 확인 및 비교
//        productRepository.findAll().forEach { product ->
//            val orderedQuantity = productQuantities.getOrDefault(product.id, 0L)
//            val expectedStock = 2000L - orderedQuantity
//
//            assertEquals(
//                expectedStock, product.stock,
//                "상품 ID ${product.id}의 재고가 정확히 차감되어야 함 " +
//                        "(초기: 2000, 주문 수량: $orderedQuantity, 현재: ${product.stock})"
//            )
//        }
//    }
//}