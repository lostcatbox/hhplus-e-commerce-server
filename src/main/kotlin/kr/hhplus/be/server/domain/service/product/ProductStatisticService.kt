package kr.hhplus.be.server.domain.service.product

import kr.hhplus.be.server.domain.model.PopularProduct
import kr.hhplus.be.server.domain.port.out.ProductStatisticRepository

class ProductStatisticService(
    private val productStatisticRepository: ProductStatisticRepository
) {
    fun findAll(): List<PopularProduct> {
        return productStatisticRepository.findPopularProducts()
    }
}