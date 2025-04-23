package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import kr.hhplus.be.server.infra.persistance.jpa.ProductJpaRepository
import kr.hhplus.be.server.infra.persistance.model.ProductEntity
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl(
    private val productJPARepository: ProductJpaRepository
) : ProductRepository {
    override fun findById(productId: Long): Product? {
        return productJPARepository.findById(productId).orElse(null)?.toDomain()
    }

    override fun findAll(): List<Product> {
        return productJPARepository.findAll().map { it.toDomain() }
    }

    override fun save(product: Product): Product {
        val entity = ProductEntity.from(product)
        val savedEntity = productJPARepository.save(entity)
        return savedEntity.toDomain()
    }
}