package kr.hhplus.be.server.infra.persistance.model

import jakarta.persistence.*
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.PopularProduct
import kr.hhplus.be.server.domain.product.PopularProductId
import java.time.LocalDate

@Entity(name = "products")
class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    
    val name: String,
    
    val price: Long,
    
    val stock: Long //잔여 수량
) {
    // 도메인 모델로 변환
    fun toDomain(): Product {
        return Product(
            id = this.id,
            name = this.name,
            price = this.price,
            stock = this.stock
        )
    }
    
    companion object {
        // 도메인 모델로부터 엔티티 생성
        fun from(domain: Product): ProductEntity {
            return ProductEntity(
                id = domain.id,
                name = domain.name,
                price = domain.price,
                stock = domain.stock
            )
        }
    }
}

@Entity(name = "popular_products")
class PopularProductEntity(
    @EmbeddedId
    val popularProductId: PopularProductIdEntity,
    
    val orderCount: Long // 하루당 총 주문량
) {
    // 도메인 모델로 변환
    fun toDomain(): PopularProduct {
        return PopularProduct(
            popularProductId = this.popularProductId.toDomain(),
            orderCount = this.orderCount
        )
    }
    
    companion object {
        // 도메인 모델로부터 엔티티 생성
        fun from(domain: PopularProduct): PopularProductEntity {
            return PopularProductEntity(
                popularProductId = PopularProductIdEntity.from(domain.popularProductId),
                orderCount = domain.orderCount
            )
        }
    }
}

@Embeddable
class PopularProductIdEntity(
    val productId: Long,
    
    val dateTime: LocalDate
) {
    // 도메인 모델로 변환
    fun toDomain(): PopularProductId {
        return PopularProductId(
            productId = this.productId,
            dateTime = this.dateTime
        )
    }
    
    companion object {
        // 도메인 모델로부터 엔티티 생성
        fun from(domain: PopularProductId): PopularProductIdEntity {
            return PopularProductIdEntity(
                productId = domain.productId,
                dateTime = domain.dateTime
            )
        }
    }
} 