package kr.hhplus.be.server.presentation.controller.product.dto

data class ProductResponse(
    val productId: Long,
    val name: String,
    val price: Long,
    val stock: Int
) 