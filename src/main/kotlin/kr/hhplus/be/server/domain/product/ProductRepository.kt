package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Repository

@Repository
interface ProductRepository {
    fun findById(productId: Long): Product?
    fun findAll(): List<Product>
    fun save(product: Product): Product
}
