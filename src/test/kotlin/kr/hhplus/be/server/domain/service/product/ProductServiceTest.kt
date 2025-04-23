package kr.hhplus.be.server.domain.service.product

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.order.OrderLineCriteria
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.exceptions.ProductNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ProductServiceTest {

    @MockK
    private lateinit var productRepository: ProductRepository

    @InjectMockKs
    private lateinit var productService: ProductService

    private lateinit var product: Product
    private lateinit var products: List<Product>

    @BeforeEach
    fun setUp() {
        product = Product(
            id = 1L,
            name = "테스트 상품",
            price = 1000L,
            stock = 10L
        )

        products = listOf(
            product,
            Product(
                id = 2L,
                name = "테스트 상품 2",
                price = 2000L,
                stock = 20L
            )
        )

        every { productRepository.findAll() } returns products
        every { productRepository.findById(1L) } returns product
        every { productRepository.save(any()) } returnsArgument 0
    }

    @Test
    fun `모든 상품 조회`() {
        // When
        val result = productService.findAll()

        // Then
        assertEquals(2, result.size)
        assertEquals(product.id, result[0].id)
        verify(exactly = 1) { productRepository.findAll() }
    }

    @Test
    fun `아이디로 상품 조회 - 존재하는 경우`() {
        // When
        val result = productService.findById(1L)

        // Then
        assertEquals(product.id, result.id)
        assertEquals(product.name, result.name)
        assertEquals(product.price, result.price)
        assertEquals(product.stock, result.stock)
        verify(exactly = 1) { productRepository.findById(1L) }
    }

    @Test
    fun `아이디로 상품 조회 - 존재하지 않는 경우`() {
        every { productRepository.findById(999L) } returns null

        // When & Then
        assertThrows<ProductNotFoundException> {
            productService.findById(999L)
        }

        verify(exactly = 1) { productRepository.findById(999L) }
    }

    @Test
    fun `주문 상품 판매 처리 - 재고 차감`() {
        // Given
        val orderLines = listOf(
            OrderLineCriteria(
                productId = 1L,
                quantity = 2L
            )
        )

        val updatedProduct = Product(
            id = product.id,
            name = product.name,
            price = product.price,
            stock = 8L
        )

        every { productRepository.findById(1L) } returns product
        every { productRepository.save(any()) } returns updatedProduct

        // When
        productService.saleOrderProducts(orderLines)

        // Then
        verify(exactly = 1) { productRepository.findById(1L) }
        verify(exactly = 1) { productRepository.save(any()) }
    }
} 