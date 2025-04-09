package kr.hhplus.be.server.domain.port.out

import kr.hhplus.be.server.domain.model.Product

interface ProductRepository {
    fun findById(productId: Long): Product?
    fun findAll(): List<Product>
}
