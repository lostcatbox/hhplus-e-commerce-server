package kr.hhplus.be.server.domain.port.out

import kr.hhplus.be.server.domain.model.PopularProduct

interface ProductStatisticRepository {
    fun findPopularProducts(): List<PopularProduct>
}
