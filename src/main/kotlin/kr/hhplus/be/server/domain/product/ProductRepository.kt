package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Repository

@Repository
interface ProductRepository {
    fun findAll(): List<Product>
    fun findById(productId: Long): Product
    fun save(product: Product): Product
    
    // 비관적 락을 이용한 조회 메서드 추가
    fun findByIdWithPessimisticLock(productId: Long): Product
}
