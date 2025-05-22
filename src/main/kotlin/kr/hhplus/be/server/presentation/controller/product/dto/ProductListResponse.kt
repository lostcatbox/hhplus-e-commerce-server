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

data class ProductResponse(
    val productId: Long,
    val name: String,
    val price: Long,
    val stock: Int
) {
    companion object {
        fun of(product: Product): ProductResponse {
            return ProductResponse(
                productId = product.id,
                name = product.name,
                price = product.price,
                stock = product.stock.toInt()
            )
        }
    }
}