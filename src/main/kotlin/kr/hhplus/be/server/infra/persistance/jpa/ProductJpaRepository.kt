package kr.hhplus.be.server.infra.persistance.jpa

import kr.hhplus.be.server.infra.persistance.model.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductJpaRepository : JpaRepository<ProductEntity, Long>