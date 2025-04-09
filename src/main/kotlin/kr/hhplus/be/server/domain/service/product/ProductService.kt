package kr.hhplus.be.server.domain.service.product

import kr.hhplus.be.server.domain.model.OrderLine
import kr.hhplus.be.server.domain.model.Product
import kr.hhplus.be.server.domain.port.out.ProductRepository
import kr.hhplus.be.server.exceptions.ProductNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    fun findAll(): List<Product> {
        return productRepository.findAll()
    }

    fun findById(id: Long): Product {
        return productRepository.findById(id) ?: throw ProductNotFoundException(id)
    }

    @Transactional
    fun saleProcessBy(orderLines: List<OrderLine>) {
        for (orderLine in orderLines) {
            val product = findById(orderLine.productId)
            // 재고 차감
            val updatedProduct = product.sale(orderLine.quantity)
            productRepository.save(updatedProduct)
        }
    }
}