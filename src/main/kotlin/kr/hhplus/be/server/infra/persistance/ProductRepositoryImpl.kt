package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import kr.hhplus.be.server.exceptions.ProductNotFoundException
import kr.hhplus.be.server.infra.persistance.jpa.ProductJpaRepository
import kr.hhplus.be.server.infra.persistance.model.ProductEntity
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl(
    private val productJpaRepository: ProductJpaRepository
) : ProductRepository {
    override fun findAll(): List<Product> {
        return productJpaRepository.findAll().map { it.toDomain() }
    }

    override fun findAllByIdInIds(ids: List<Long>): List<Product> {
        return productJpaRepository.findAllByIdIn(ids).map { it.toDomain() }
    }

    override fun findById(productId: Long): Product {
        return productJpaRepository.findById(productId)
            .map { it.toDomain() }
            .orElseThrow { ProductNotFoundException(productId) }
    }

    override fun save(product: Product): Product {
        val entity = ProductEntity.from(product)
        val savedEntity = productJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findByIdWithPessimisticLock(productId: Long): Product {
        return productJpaRepository.findByIdWithPessimisticLock(productId)
            ?.toDomain()
            ?: throw ProductNotFoundException(productId)
    }
}