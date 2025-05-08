package kr.hhplus.be.server.presentation.controller.coupon

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.presentation.controller.coupon.dto.response.CouponListResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
class UserCouponController(
    private val couponFacade: CouponFacade
) {
    @Operation(summary = "유저가 보유한 쿠폰 리스트 조회", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @GetMapping("/{userId}/coupons")
    fun getUserCouponList(@PathVariable("userId") userId: Long): CouponListResponse {
        val coupons = couponFacade.getUserCouponList(userId)
        return CouponListResponse.of(coupons)
    }
}