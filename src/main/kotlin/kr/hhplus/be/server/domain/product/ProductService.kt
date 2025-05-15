package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.order.OrderLineCriteria
import kr.hhplus.be.server.exceptions.ProductNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    fun findAll(): List<Product> {
        return productRepository.findAll()
    }

    fun findAllByIdInIds(ids: List<Long>): List<Product> {
        return productRepository.findAllByIdInIds(ids)
    }

    fun findById(id: Long): Product {
        return productRepository.findById(id) ?: throw ProductNotFoundException(id)
    }

    @Transactional
    fun saleOrderProducts(orderLines: List<OrderLineCriteria>) {
        for (orderLine in orderLines) {
            // 비관적 락을 사용하여 상품 조회
            val product = productRepository.findByIdWithPessimisticLock(orderLine.productId)
            // 재고 차감
            val updatedProduct = product.sale(orderLine.quantity)
            productRepository.save(updatedProduct)
        }
    }
}