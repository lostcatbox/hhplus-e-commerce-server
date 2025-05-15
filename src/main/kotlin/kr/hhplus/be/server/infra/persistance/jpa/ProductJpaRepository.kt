package kr.hhplus.be.server.infra.persistance.jpa

import jakarta.persistence.LockModeType
import kr.hhplus.be.server.infra.persistance.model.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductJpaRepository : JpaRepository<ProductEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM products p WHERE p.id = :productId")
    fun findByIdWithPessimisticLock(@Param("productId") productId: Long): ProductEntity?
    fun findAllByIdIn(ids: List<Long>): List<ProductEntity>
}