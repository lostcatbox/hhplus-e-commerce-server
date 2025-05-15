package kr.hhplus.be.server.presentation.controller.coupon

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.presentation.controller.coupon.dto.CouponIssueResponse
import kr.hhplus.be.server.presentation.controller.coupon.dto.CouponResponse
import kr.hhplus.be.server.presentation.controller.coupon.dto.CreateCouponRequest
import kr.hhplus.be.server.presentation.controller.coupon.dto.response.CouponListResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/coupons")
class CouponController(
    private val couponFacade: CouponFacade,
    private val couponService: CouponService
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

    /**
     * 신규 쿠폰 생성 엔드포인트
     */
    @PostMapping
    fun createCoupon(@RequestBody request: CreateCouponRequest): ResponseEntity<CouponResponse> {
        val coupon = couponService.createCoupon(
            name = request.name,
            stock = request.stock,
            amount = request.amount,
            percent = request.percent
        )

        return ResponseEntity.ok(CouponResponse.from(coupon))
    }

    /**
     * 선착순 쿠폰 발급 요청 엔드포인트
     */
    @PostMapping("/issue")
    fun requestCouponIssue(@RequestBody request: kr.hhplus.be.server.presentation.controller.coupon.dto.CouponIssueRequest): ResponseEntity<CouponIssueResponse> {
        val couponId = request.couponId
        val userId = request.userId

        // 쿠폰 발급 가능 여부 확인
        if (!couponService.canIssueCoupon(couponId, userId)) {
            return ResponseEntity.badRequest().body(
                CouponIssueResponse(
                    success = false,
                    message = "쿠폰을 발급할 수 없습니다. 재고가 부족하거나 이미 발급받은 쿠폰입니다."
                )
            )
        }

        // 쿠폰 발급 요청 처리
        val success = couponService.requestCouponIssue(couponId, userId)

        return if (success) {
            ResponseEntity.ok(
                CouponIssueResponse(
                    success = true,
                    message = "쿠폰 발급 요청이 성공적으로 등록되었습니다. 잠시 후 발급 결과를 확인해주세요."
                )
            )
        } else {
            ResponseEntity.badRequest().body(
                CouponIssueResponse(
                    success = false,
                    message = "쿠폰 발급 요청에 실패했습니다. 재고가 부족하거나 이미 발급받은 쿠폰입니다."
                )
            )
        }
    }

    /**
     * 사용자별 발급된 쿠폰 목록 조회
     */
    @GetMapping("/users/{userId}")
    fun getUserCoupons(@PathVariable userId: Long): ResponseEntity<List<CouponResponse>> {
        val coupons = couponService.getUserCouponList(userId)
        return ResponseEntity.ok(coupons.map { CouponResponse.from(it) })
    }

    /**
     * 쿠폰 재고 조회
     */
    @GetMapping("/{couponId}/stock")
    fun getCouponStock(@PathVariable couponId: Long): ResponseEntity<Map<String, Long>> {
        val stock = couponService.getCouponStock(couponId)
        return ResponseEntity.ok(mapOf("stock" to stock))
    }
}