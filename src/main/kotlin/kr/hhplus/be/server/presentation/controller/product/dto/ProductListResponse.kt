package kr.hhplus.be.server.presentation.controller.product.dto

import kr.hhplus.be.server.domain.product.Product

data class ProductListResponse(
    val products: List<ProductResponse>
) {
    companion object {
        fun of(products: List<Product>): ProductListResponse {
            return ProductListResponse(
                products = products.map { ProductResponse.of(it) }
            )
        }
    }
} 