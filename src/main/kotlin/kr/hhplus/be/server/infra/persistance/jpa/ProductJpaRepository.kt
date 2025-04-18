package kr.hhplus.be.server.infra.persistance.jpa

import kr.hhplus.be.server.domain.product.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductJpaRepository : JpaRepository<Product, Long>