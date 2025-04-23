package kr.hhplus.be.server.domain.product

import java.time.LocalDate

// 순수 도메인 모델로 변경
class Product(
    val id: Long = 0L,
    val name: String,
    val price: Long,
    var stock: Long //잔여 수량
) {
    init {
        require(price >= 0) { "가격은 0 이상이여야 합니다." }
        require(stock >= 0) { "재고는 0 이상이여야 합니다." }
    }

    //판매 요청 수량
    fun sale(saleAmount: Long): Product {
        require(stock - saleAmount >= 0) { "재고는 0 이상이여야 합니다." }
        return Product(
            id = this.id,
            name = this.name,
            price = this.price,
            stock = this.stock - saleAmount
        )
    }
}

// 순수 도메인 모델로 변경
class PopularProduct(
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

// 순수 도메인 모델로 변경
class PopularProductId(
    val productId: Long,
    val dateTime: LocalDate
)