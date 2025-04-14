package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.point.Product
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository {
    fun findById(productId: Long): Product?
    fun findAll(): List<Product>
    fun save(product: Product)
}
