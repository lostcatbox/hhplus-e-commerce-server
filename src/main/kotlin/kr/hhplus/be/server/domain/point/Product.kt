package kr.hhplus.be.server.domain.point

import java.time.LocalDateTime

data class Product(
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
        return this.copy(
            stock = stock - saleAmount
        )
    }
}


data class PopularProduct(
    val productId: Long,
    val orderCount: Long, // 하루당 총 주문량
    val dateTime: LocalDateTime
) {
    fun saleCount(saleAmount: Long): PopularProduct {
        return this.copy(
            orderCount = orderCount + saleAmount
        )
    }

    fun cancelSaleCount(cancelSaleAmount: Long): PopularProduct {
        return this.copy(
            orderCount = orderCount - cancelSaleAmount
        )
    }
}