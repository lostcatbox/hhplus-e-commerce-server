package kr.hhplus.be.server.presentation.controller.order

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.domain.order.OrderCriteria
import kr.hhplus.be.server.domain.order.OrderLineCriteria
import kr.hhplus.be.server.presentation.controller.order.dto.OrderRequest
import kr.hhplus.be.server.presentation.controller.order.dto.OrderResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/orders")
class OrderController(
    private val orderFacade: OrderFacade
) {
    @Operation(summary = "주문 처리", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @PostMapping("")
    fun processOrder(@RequestBody @Valid request: OrderRequest): ResponseEntity<OrderResponse> {
        // OrderRequest를 OrderCriteria로 변환
        val orderCriteria = OrderCriteria(
            userId = request.userId,
            issuedCouponId = request.couponId,
            orderLines = request.orderLines.map { 
                OrderLineCriteria(
                    productId = it.productId,
                    quantity = it.quantity.toLong()
                )
            }
        )
        
        // OrderFacade를 통해 주문 처리
        orderFacade.processOrder(orderCriteria)
        
        return ResponseEntity.ok(
            OrderResponse(
                orderId = 1L, // 실제로는 주문 ID를 반환해야 함
                status = "결제완료"
            )
        )
    }
}