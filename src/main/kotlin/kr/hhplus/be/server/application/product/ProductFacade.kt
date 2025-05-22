package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.PopularProduct
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStatisticRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ProductFacade(
    private val productService: ProductService,
    private val popularProductStatisticRepository: ProductStatisticRepository
) {
    val log = LoggerFactory.getLogger(javaClass)

    fun getAllProducts(): List<Product> {
        return productService.findAll()
    }

    fun getProductById(productId: Long): Product {
        return productService.findById(productId)
    }

    // 인기 상품 목록을 가져오는 메서드
    // 클라이언트 사이드 캐싱만 활용 (Redis에서 이미 관리됨)
    fun getPopularProducts(): List<PopularProduct> {
        return popularProductStatisticRepository.findAll()
    }

    // 특정 날짜의 인기 상품 목록 조회
    fun getPopularProductsByDate(date: LocalDate): List<PopularProduct> {
        return popularProductStatisticRepository.findAllByDate(date)
    }
} 