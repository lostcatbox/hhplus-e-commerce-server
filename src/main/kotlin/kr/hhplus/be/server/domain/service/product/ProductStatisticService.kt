package kr.hhplus.be.server.domain.service.product

import kr.hhplus.be.server.domain.model.PopularProduct
import kr.hhplus.be.server.domain.port.out.ProductStatisticRepository
import org.springframework.stereotype.Service

@Service
class ProductStatisticService(
    private val productStatisticRepository: ProductStatisticRepository
) {
    fun findAll(): List<PopularProduct> {
        return productStatisticRepository.findPopularProducts()
    }
}