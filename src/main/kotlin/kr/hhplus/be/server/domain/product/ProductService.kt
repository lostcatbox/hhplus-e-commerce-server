package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.order.OrderLine
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
    fun saleOrderProducts(orderLines: List<OrderLine>) {
        for (orderLine in orderLines) {
            val product = findById(orderLine.productId)
            // 재고 차감
            product.sale(orderLine.quantity)
            productRepository.save(product)
        }
    }
}