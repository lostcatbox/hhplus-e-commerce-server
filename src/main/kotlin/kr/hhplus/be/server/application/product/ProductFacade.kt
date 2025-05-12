package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class ProductFacade(
    private val productService: ProductService
) {
    fun getAllProducts(): List<Product> {
        return productService.findAll()
    }

    fun getProductById(productId: Long): Product {
        return productService.findById(productId)
    }

    // 인기 상품 목록을 가져오는 메서드
    // Redis 캐싱 적용
    @Cacheable(
        value = ["popularProducts"],
        unless = "#result.isEmpty()"
    )
    fun getPopularProducts(): List<Product> {
        // 여기서는, 단순히 전체 상품 목록을 반환
        // 실제로는 인기 상품을 판별하는 로직이 필요
        return productService.findAll()
    }
} 