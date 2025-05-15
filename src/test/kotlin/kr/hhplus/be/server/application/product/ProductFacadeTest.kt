package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.PopularProduct
import kr.hhplus.be.server.domain.product.PopularProductId
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStatisticService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cache.CacheManager
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ContextConfiguration(classes = [TestCacheConfig::class])
class ProductFacadeTest {

    @MockBean
    private lateinit var productService: ProductService

    @MockBean
    private lateinit var popularProductStatisticService: ProductStatisticService

    @Autowired
    private lateinit var productFacade: ProductFacade

    @Autowired
    private lateinit var cacheManager: CacheManager

    @BeforeEach
    fun setup() {
        cacheManager.getCache(TestCacheConfig.POPULAR_PRODUCTS_CACHE)?.clear()
    }

    @Test
    fun `두 번째 호출에서는 캐시된 데이터를 반환해야 한다`() {
        // Given
        val dummyProducts = generateDummyProducts(1000)
        `when`(popularProductStatisticService.findAll()).thenReturn(dummyProducts)

        // When
        val firstCall = productFacade.getPopularProducts()
        val secondCall = productFacade.getPopularProducts()

        // Then
        assertEquals(dummyProducts.size, firstCall.size)
        assertEquals(dummyProducts.size, secondCall.size)
        verify(popularProductStatisticService, times(1)).findAll()
    }

    @Test
    fun `서비스가 빈 리스트를 반환하면 빈 리스트를 반환해야 한다`() {
        // Given
        `when`(popularProductStatisticService.findAll()).thenReturn(emptyList())

        // When
        val result = productFacade.getPopularProducts()

        // Then
        assertTrue(result.isEmpty())
        verify(popularProductStatisticService, times(1)).findAll()
    }

    @Test
    fun `올바른 데이터 구조를 반환해야 한다`() {
        // Given
        val dummyProducts = generateDummyProducts(1000)
        `when`(popularProductStatisticService.findAll()).thenReturn(dummyProducts)

        // When
        val result = productFacade.getPopularProducts()

        // Then
        assertEquals(dummyProducts.size, result.size)
        result.forEachIndexed { index, popularProduct ->
            assertEquals(dummyProducts[index].popularProductId.productId, popularProduct.popularProductId.productId)
            assertEquals(dummyProducts[index].popularProductId.dateTime, popularProduct.popularProductId.dateTime)
            assertEquals(dummyProducts[index].orderCount, popularProduct.orderCount)
        }
    }

    private fun generateDummyProducts(count: Int): List<PopularProduct> {
        val now = LocalDate.now()
        return (1..count).map { id ->
            PopularProduct(
                popularProductId = PopularProductId(
                    productId = id.toLong(),
                    dateTime = now
                ),
                orderCount = (id * 100).toLong()
            )
        }
    }
} 