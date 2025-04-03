package kr.hhplus.be.server.presentation.controller.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kr.hhplus.be.server.presentation.controller.product.dto.ProductListResponse
import kr.hhplus.be.server.presentation.controller.product.dto.ProductResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/products")
class ProductController {
    @Operation(summary = "상품 리스트 조회", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @GetMapping("")
    fun getProductList(): ResponseEntity<ProductListResponse> {
        return ResponseEntity.ok(
            ProductListResponse(
                products = listOf(
                    ProductResponse(
                        productId = 1L,
                        name = "상품1",
                        price = 10000L,
                        stock = 10
                    )
                )
            )
        )
    }

    @Operation(summary = "인기 상품 리스트 조회", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @GetMapping("/popular")
    fun getPopularProducts(): ResponseEntity<ProductListResponse> {
        return ResponseEntity.ok(
            ProductListResponse(
                products = listOf(
                    ProductResponse(
                        productId = 1L,
                        name = "인기상품1",
                        price = 10000L,
                        stock = 10
                    ),
                    ProductResponse(
                        productId = 2L,
                        name = "인기상품2",
                        price = 10000L,
                        stock = 10
                    )
                )
            )
        )
    }
}