package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import kr.hhplus.be.server.infra.persistance.jpa.ProductJpaRepository
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl(
    private val productJPARepository: ProductJpaRepository
) : ProductRepository {
    override fun findById(productId: Long): Product? {
        return productJPARepository.findById(productId).orElse(null)
    }

    override fun findAll(): List<Product> {
        return productJPARepository.findAll()
    }

    override fun save(product: Product) {
        productJPARepository.save(product)
    }

}