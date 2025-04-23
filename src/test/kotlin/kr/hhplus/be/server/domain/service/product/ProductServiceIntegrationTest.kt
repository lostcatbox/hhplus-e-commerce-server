package kr.hhplus.be.server.domain.service.product

import kr.hhplus.be.server.domain.order.OrderLineCriteria
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.exceptions.ProductNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Test
    fun `findAll - 상품 목록 조회`() {
        // given
        val product1 = Product(name = "상품1", price = 10000L, stock = 100L)
        val product2 = Product(name = "상품2", price = 20000L, stock = 50L)
        productRepository.save(product1)
        productRepository.save(product2)

        // when
        val products = productService.findAll()

        // then
        assertTrue(products.size >= 2)
        assertTrue(products.any { it.name == "상품1" && it.price == 10000L })
        assertTrue(products.any { it.name == "상품2" && it.price == 20000L })
    }

    @Test
    fun `findById - 존재하는 상품 조회`() {
        // given
        val product = Product(name = "테스트 상품", price = 15000L, stock = 30L)
        val savedProduct = productRepository.save(product)

        // when
        val result = productService.findById(savedProduct.id)

        // then
        assertNotNull(result)
        assertEquals(savedProduct.id, result.id)
        assertEquals("테스트 상품", result.name)
        assertEquals(15000L, result.price)
        assertEquals(30L, result.stock)
    }

    @Test
    fun `findById - 존재하지 않는 상품 조회시 예외 발생`() {
        // when & then
        assertThrows<ProductNotFoundException> {
            productService.findById(999L)
        }
    }

    @Test
    fun `saleOrderProducts - 상품 재고 차감 성공`() {
        // given
        val product = Product(name = "테스트 상품", price = 15000L, stock = 30L)
        val savedProduct = productRepository.save(product)
        
        val orderLines = listOf(
            OrderLineCriteria(
                productId = savedProduct.id,
                quantity = 5L
            )
        )

        // when
        productService.saleOrderProducts(orderLines)

        // then
        val updatedProduct = productService.findById(savedProduct.id)
        assertEquals(25L, updatedProduct.stock) // 30 - 5
    }

    @Test
    fun `saleOrderProducts - 재고 부족시 예외 발생`() {
        // given
        val product = Product(name = "재고 부족 상품", price = 15000L, stock = 3L)
        val savedProduct = productRepository.save(product)
        
        val orderLines = listOf(
            OrderLineCriteria(
                productId = savedProduct.id,
                quantity = 5L
            )
        )

        // when & then
        assertThrows<IllegalArgumentException> {
            productService.saleOrderProducts(orderLines)
        }

        // 재고가 차감되지 않았는지 확인
        val unchangedProduct = productService.findById(savedProduct.id)
        assertEquals(3L, unchangedProduct.stock)
    }
} 