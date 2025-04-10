package kr.hhplus.be.server.domain.port.out

import kr.hhplus.be.server.domain.model.Product
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository {
    fun findById(productId: Long): Product?
    fun findAll(): List<Product>
    fun save(product: Product)
}
