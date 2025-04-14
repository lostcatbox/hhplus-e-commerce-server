package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.point.PopularProduct
import org.springframework.stereotype.Repository

@Repository
interface ProductStatisticRepository {
    fun findPopularProducts(): List<PopularProduct>
}
