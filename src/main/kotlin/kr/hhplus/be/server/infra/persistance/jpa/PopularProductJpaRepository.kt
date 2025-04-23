package kr.hhplus.be.server.infra.persistance.jpa

import kr.hhplus.be.server.infra.persistance.model.PopularProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PopularProductJpaRepository : JpaRepository<PopularProductEntity, Long> {
    @Query("SELECT p FROM popular_products p ORDER BY p.orderCount DESC")
    fun findPopularProducts(): List<PopularProductEntity>
} 