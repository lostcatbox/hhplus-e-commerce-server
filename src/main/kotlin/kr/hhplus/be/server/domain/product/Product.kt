package kr.hhplus.be.server.domain.product

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate

// 순수 도메인 모델로 변경

class Product(
    val id: Long = 0,
    val name: String,
    val price: Long,
    val stock: Long
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

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
class PopularProduct(
    @JsonProperty("popularProductId") val popularProductId: PopularProductId,
    @JsonProperty("orderCount") val orderCount: Long, // 하루당 총 주문량
)

// 순수 도메인 모델로 변경
class PopularProductId(
    val productId: Long,
    val dateTime: LocalDate
)