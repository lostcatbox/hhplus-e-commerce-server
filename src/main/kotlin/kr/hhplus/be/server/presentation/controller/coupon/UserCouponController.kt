package kr.hhplus.be.server.presentation.controller.coupon

import CouponListResponse
import CouponResponse
import CouponType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/users")
class UserCouponController {
    @Operation(summary = "유저가 보유한 쿠폰 리스트 조회", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @GetMapping("/{userId}/coupons")
    fun getUserCouponList(@PathVariable("userId") userId: Long): CouponListResponse {
        return CouponListResponse(
            listOf(
                CouponResponse(
                    couponId = 1,
                    name = "신규가입 금액 할인쿠폰",
                    type = CouponType.AMOUNT,
                    discountValue = 10000,
                    startDate = LocalDateTime.now(),
                    endDate = LocalDateTime.now(),
                    isActive = true,
                    remainingStock = 1
                ),
                CouponResponse(
                    couponId = 2,
                    name = "신규가입 퍼센트할인쿠폰",
                    type = CouponType.PERCENTAGE,
                    discountValue = 10,
                    startDate = LocalDateTime.now(),
                    endDate = LocalDateTime.now(),
                    isActive = true,
                    remainingStock = 1
                )
            )
        )
    }
}