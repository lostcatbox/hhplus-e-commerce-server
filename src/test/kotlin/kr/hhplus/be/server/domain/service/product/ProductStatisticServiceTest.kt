package kr.hhplus.be.server.domain.service.product

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.point.PopularProduct
import kr.hhplus.be.server.domain.product.ProductStatisticRepository
import kr.hhplus.be.server.domain.product.ProductStatisticService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class ProductStatisticServiceTest {

    @MockK
    private lateinit var productStatisticRepository: ProductStatisticRepository

    @InjectMockKs
    private lateinit var productStatisticService: ProductStatisticService

    private lateinit var popularProducts: List<PopularProduct>

    @BeforeEach
    fun setUp() {
        popularProducts = listOf(
            PopularProduct(
                productId = 1L,
                orderCount = 100L,
                dateTime = LocalDateTime.now()
            ),
            PopularProduct(
                productId = 2L,
                orderCount = 80L,
                dateTime = LocalDateTime.now()
            ),
            PopularProduct(
                productId = 3L,
                orderCount = 60L,
                dateTime = LocalDateTime.now()
            )
        )

        every { productStatisticRepository.findPopularProducts() } returns popularProducts
    }

    @Test
    fun `인기 상품 목록 조회`() {
        // When
        val result = productStatisticService.findAll()

        // Then
        assertEquals(3, result.size)
        assertEquals(popularProducts[0].productId, result[0].productId)
        assertEquals(popularProducts[0].orderCount, result[0].orderCount)
        verify(exactly = 1) { productStatisticRepository.findPopularProducts() }
    }
} 