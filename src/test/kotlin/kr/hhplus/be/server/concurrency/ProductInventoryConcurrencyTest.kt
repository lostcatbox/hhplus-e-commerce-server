package kr.hhplus.be.server.concurrency

import kr.hhplus.be.server.domain.order.OrderLineCriteria
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import kr.hhplus.be.server.domain.product.ProductService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("test")
class ProductInventoryConcurrencyTest {

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var productRepository: ProductRepository

    private val threadCount = 50
    private lateinit var initialProduct: Product

    @BeforeEach
    @Transactional
    fun setup() {
        // 테스트용 상품 생성 (초기 재고: 30개)
        initialProduct = productRepository.save(
            Product(name = "테스트 상품", price = 10000L, stock = 30L)
        )
    }

    @Test
    fun `동시에 여러 요청이 재고를 차감할 때 데이터 일관성 검증`() {
        // Given
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        // When - 여러 스레드에서 동시에 재고 차감 요청
        repeat(threadCount) {
            executorService.submit {
                try {
                    val orderLines = listOf(
                        OrderLineCriteria(
                            productId = initialProduct.id,
                            quantity = 1L // 각 요청당 1개씩 차감
                        )
                    )
                    
                    productService.saleOrderProducts(orderLines)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    println("재고 차감 실패: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 요청이 처리될 때까지 대기
        latch.await()
        executorService.shutdown()

        // Then
        // 최종 상품 재고 확인
        val finalProduct = productRepository.findById(initialProduct.id)
        
        println("=== 재고 차감 동시성 테스트 결과 ===")
        println("초기 재고: ${initialProduct.stock}")
        println("성공한 요청 수: ${successCount.get()}")
        println("실패한 요청 수: ${failCount.get()}")
        println("최종 재고: ${finalProduct.stock}")
        
        // 초기 재고 - 성공한 요청 수 = 현재 재고 검증
        assertEquals(initialProduct.stock - successCount.get(), finalProduct.stock, 
            "재고가 정확히 차감되어야 함")
        
        // 모든 요청의 합이 스레드 수와 일치해야 함
        assertEquals(threadCount, successCount.get() + failCount.get(),
            "모든 요청이 성공 또는 실패로 처리되어야 함")
    }
} 