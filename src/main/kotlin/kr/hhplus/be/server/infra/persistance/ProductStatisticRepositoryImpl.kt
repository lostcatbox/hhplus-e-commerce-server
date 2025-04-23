package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.product.PopularProduct
import kr.hhplus.be.server.domain.product.ProductStatisticRepository
import kr.hhplus.be.server.infra.persistance.jpa.PopularProductJpaRepository
import org.springframework.stereotype.Repository

@Repository
class ProductStatisticRepositoryImpl(
    private val popularProductJpaRepository: PopularProductJpaRepository
) : ProductStatisticRepository {
    override fun findPopularProducts(): List<PopularProduct> {
        return popularProductJpaRepository.findPopularProducts().map { it.toDomain() }
    }
} 