package kr.hhplus.be.server.presentation.controller.product.dto

import kr.hhplus.be.server.domain.product.PopularProduct

data class PopularProductListResponse(
    val products: List<PopularProductResponse>
) {
    companion object {
        fun of(products: List<PopularProduct>): PopularProductListResponse {
            return PopularProductListResponse(
                products = products.map { PopularProductResponse.of(it) }
            )
        }
    }
}

data class PopularProductResponse(
    val productId: Long,
    val orderCount: Long
) {
    companion object {
        fun of(it: PopularProduct): PopularProductResponse {
            return PopularProductResponse(
                productId = it.popularProductId.productId,
                orderCount = it.orderCount
            )
        }
    }
}
