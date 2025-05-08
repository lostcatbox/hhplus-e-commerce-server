package kr.hhplus.be.server.presentation.controller.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.presentation.controller.product.dto.ProductListResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/products")
class ProductController(
    private val productFacade: ProductFacade
) {
    @Operation(summary = "상품 리스트 조회", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @GetMapping("")
    fun getProductList(): ResponseEntity<ProductListResponse> {
        val products = productFacade.getAllProducts()
        return ResponseEntity.ok(ProductListResponse.of(products))
    }

    @Operation(summary = "상품 상세 조회", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
        ]
    )
    @GetMapping("/{productId}")
    fun getProduct(@PathVariable productId: Long): ResponseEntity<kr.hhplus.be.server.presentation.controller.product.dto.ProductResponse> {
        val product = productFacade.getProductById(productId)
        return ResponseEntity.ok(kr.hhplus.be.server.presentation.controller.product.dto.ProductResponse.of(product))
    }

    @Operation(summary = "인기 상품 리스트 조회", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @GetMapping("/popular")
    fun getPopularProducts(): ResponseEntity<ProductListResponse> {
        val popularProducts = productFacade.getPopularProducts()
        return ResponseEntity.ok(ProductListResponse.of(popularProducts))
    }
}