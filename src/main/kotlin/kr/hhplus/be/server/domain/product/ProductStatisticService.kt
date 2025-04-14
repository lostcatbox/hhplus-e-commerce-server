package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Service

@Service
class ProductStatisticService(
    private val productStatisticRepository: ProductStatisticRepository
) {
    fun findAll(): List<PopularProduct> {
        return productStatisticRepository.findPopularProducts()
    }
}