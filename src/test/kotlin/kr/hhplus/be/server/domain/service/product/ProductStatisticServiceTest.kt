package kr.hhplus.be.server.domain.service.product

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.product.PopularProduct
import kr.hhplus.be.server.domain.product.PopularProductId
import kr.hhplus.be.server.domain.product.ProductStatisticRepository
import kr.hhplus.be.server.domain.product.ProductStatisticService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
class ProductStatisticServiceTest {

    @MockK
    private lateinit var productStatisticRepository: ProductStatisticRepository

    @InjectMockKs
    private lateinit var productStatisticService: ProductStatisticService

    private lateinit var popularProducts: List<PopularProduct>

    @BeforeEach
    fun setUp() {
        val now = LocalDate.now()
        popularProducts = listOf(
            PopularProduct(
                PopularProductId(
                    productId = 1L,
                    dateTime = now
                ),
                orderCount = 100L
            ),
            PopularProduct(
                PopularProductId(
                    productId = 2L,
                    dateTime = now
                ),
                orderCount = 80L
            ),
            PopularProduct(
                PopularProductId(
                    productId = 3L,
                    dateTime = now
                ),
                orderCount = 60L,
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
        assertEquals(popularProducts[0].popularProductId.productId, result[0].popularProductId.productId)
        assertEquals(popularProducts[0].orderCount, result[0].orderCount)
        verify(exactly = 1) { productStatisticRepository.findPopularProducts() }
    }
} 