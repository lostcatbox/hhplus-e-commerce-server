package kr.hhplus.be.server.infra.persistance.jpa

import kr.hhplus.be.server.domain.product.PopularProduct
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PopularProductJpaRepository : JpaRepository<PopularProduct, Long> {
    @Query("SELECT p FROM popular_products p ORDER BY p.orderCount DESC")
    fun findPopularProducts(): List<PopularProduct>
} 