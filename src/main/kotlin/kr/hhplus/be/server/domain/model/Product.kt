package kr.hhplus.be.server.domain.model

import java.time.LocalDateTime

data class Product(
    val productId: Long,
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

    fun deductStock(quantity: Int): Product {
        if (stock.toInt() < quantity) {
            throw IllegalStateException("재고가 부족합니다.")
        }
        return copy(stock = stock - quantity)
    }
}


data class PopularProduct(
    val product: Long,
    val amount: Long, // 하루당 총 주문량
    val dateTime: LocalDateTime
) {
    fun saleCount(saleAmount: Long): PopularProduct {
        return this.copy(
            amount = amount + saleAmount
        )
    }

    fun cancelSaleCount(cancelSaleAmount: Long): PopularProduct {
        return this.copy(
            amount = amount - cancelSaleAmount
        )
    }
}