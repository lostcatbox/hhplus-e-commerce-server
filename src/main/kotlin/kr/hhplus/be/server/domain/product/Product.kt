package kr.hhplus.be.server.domain.product

import jakarta.persistence.*
import java.time.LocalDate

@Entity(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val name: String,
    val price: Long,
    val stock: Long, //잔여 수량
) {
    init {
        require(price >= 0) { "가격은 0 이상이여야 합니다." }
        require(stock >= 0) { "재고는 0 이상이여야 합니다." }
    }

    //판매 요청 수량
    fun sale(saleAmount: Long): Product {
        return Product(
            id = this.id,
            name = this.name,
            price = price,
            stock = stock - saleAmount
        )
    }
}

@Entity(name = "popular_products")
class PopularProduct(
    @EmbeddedId
    val popularProductId: PopularProductId,
    val orderCount: Long, // 하루당 총 주문량
) {
    fun saleCount(saleAmount: Long): PopularProduct {
        return PopularProduct(
            popularProductId = popularProductId,
            orderCount = orderCount + saleAmount
        )
    }

    fun cancelSaleCount(cancelSaleAmount: Long): PopularProduct {
        return PopularProduct(
            popularProductId = popularProductId,
            orderCount = orderCount - cancelSaleAmount
        )
    }
}

@Embeddable
class PopularProductId(
    val productId: Long,
    val dateTime: LocalDate
)