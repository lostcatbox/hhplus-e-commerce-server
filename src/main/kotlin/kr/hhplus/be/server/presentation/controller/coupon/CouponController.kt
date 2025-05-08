package kr.hhplus.be.server.presentation.controller.coupon

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.presentation.controller.coupon.dto.request.CouponIssueRequest
import kr.hhplus.be.server.presentation.controller.coupon.dto.response.CouponListResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/coupons")
class CouponController(
    private val couponFacade: CouponFacade
) {
    @Operation(summary = "전체 발급 가능한 쿠폰 리스트 조회", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @GetMapping("")
    fun getCouponList(): CouponListResponse {
        // 모든 쿠폰 조회 구현 필요 (현재는 간단한 목업 데이터 반환)
        // 실제로는 couponService의 적절한 메서드를 호출해야 함
        return CouponListResponse(
            listOf(
                kr.hhplus.be.server.presentation.controller.coupon.dto.response.CouponResponse(
                    couponId = 1,
                    name = "신규가입 금액 할인쿠폰",
                    type = kr.hhplus.be.server.presentation.controller.coupon.dto.response.CouponType.AMOUNT,
                    discountValue = 10000.0,
                    isActive = true,
                    remainingStock = 1
                ),
                kr.hhplus.be.server.presentation.controller.coupon.dto.response.CouponResponse(
                    couponId = 2,
                    name = "신규가입 퍼센트할인쿠폰",
                    type = kr.hhplus.be.server.presentation.controller.coupon.dto.response.CouponType.PERCENTAGE,
                    discountValue = 10.0,
                    isActive = true,
                    remainingStock = 1
                )
            )
        )
    }

    @Operation(summary = "사용자의 쿠폰 목록 조회", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @GetMapping("/users/{userId}")
    fun getUserCoupons(@PathVariable userId: Long): CouponListResponse {
        val coupons = couponFacade.getUserCouponList(userId)
        return CouponListResponse.of(coupons)
    }

    @Operation(summary = "쿠폰 발급 요청", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @PostMapping("")
    fun issueCoupon(@RequestBody @Valid request: CouponIssueRequest): ResponseEntity<Unit> {
        val command = request.toCommand()
        couponFacade.issuedCouponTo(command.userId, command.couponId)
        return ResponseEntity.ok().build()
    }
}