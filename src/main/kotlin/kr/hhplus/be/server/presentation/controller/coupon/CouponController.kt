package kr.hhplus.be.server.presentation.controller.coupon

import CouponListResponse
import CouponResponse
import CouponType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import kr.hhplus.be.server.presentation.controller.coupon.dto.request.CouponIssueRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/coupons")
class CouponController {
    @Operation(summary = "전체 발급 가능한 쿠폰 리스트 조회", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @GetMapping("")
    fun getCouponList(): CouponListResponse {
        return CouponListResponse(
            listOf(
                CouponResponse(
                    couponId = 1,
                    name = "신규가입 금액 할인쿠폰",
                    type = CouponType.AMOUNT,
                    discountValue = 10000,
                    isActive = true,
                    remainingStock = 1
                ),
                CouponResponse(
                    couponId = 2,
                    name = "신규가입 퍼센트할인쿠폰",
                    type = CouponType.PERCENTAGE,
                    discountValue = 10,
                    isActive = true,
                    remainingStock = 1
                )
            )
        )
    }

    @Operation(summary = "쿠폰 발급 요청", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @PostMapping("")
    fun issueCoupon(@RequestBody @Valid request: CouponIssueRequest): ResponseEntity<Unit> {
        return ResponseEntity.ok().build()
    }
}