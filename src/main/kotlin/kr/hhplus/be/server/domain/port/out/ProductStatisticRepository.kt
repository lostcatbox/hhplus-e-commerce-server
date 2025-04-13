package kr.hhplus.be.server.domain.port.out

import kr.hhplus.be.server.domain.model.PopularProduct
import org.springframework.stereotype.Repository

@Repository
interface ProductStatisticRepository {
    fun findPopularProducts(): List<PopularProduct>
}
