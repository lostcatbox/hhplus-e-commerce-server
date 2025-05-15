package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStatisticService
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@EnableCaching
class TestCacheConfig {
    companion object {
        const val POPULAR_PRODUCTS_CACHE = "popularProducts"
    }

    @Bean
    @Primary
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager(POPULAR_PRODUCTS_CACHE)
    }

    @Bean
    fun productFacade(
        productService: ProductService,
        popularProductStatisticService: ProductStatisticService
    ): ProductFacade {
        return ProductFacade(productService, popularProductStatisticService)
    }
} 