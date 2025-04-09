package kr.hhplus.be.server.domain.service.product

import kr.hhplus.be.server.domain.model.Product
import kr.hhplus.be.server.domain.port.out.ProductRepository
import kr.hhplus.be.server.exceptions.ProductNotFoundException

class ProductService(
    private val productRepository: ProductRepository
) {
    fun findAll(): List<Product> {
        return productRepository.findAll()
    }

    fun findById(id: Long): Product {
        return productRepository.findById(id) ?: throw ProductNotFoundException(id)
    }
}