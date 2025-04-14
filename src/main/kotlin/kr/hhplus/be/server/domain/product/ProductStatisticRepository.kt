package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Repository

@Repository
interface ProductStatisticRepository {
    fun findPopularProducts(): List<PopularProduct>
}
