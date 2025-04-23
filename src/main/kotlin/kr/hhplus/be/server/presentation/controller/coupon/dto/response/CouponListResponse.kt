import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "쿠폰 목록 응답")
data class CouponListResponse(
    @Schema(description = "쿠폰 목록")
    val coupons: List<CouponResponse>
)

@Schema(description = "쿠폰 상세 정보")
data class CouponResponse(
    @Schema(description = "쿠폰 ID", example = "1")
    val couponId: Int,

    @Schema(description = "쿠폰명", example = "신규가입 할인쿠폰")
    val name: String,

    @Schema(description = "쿠폰 타입 (AMOUNT: 금액할인, PERCENTAGE: 비율할인)", example = "AMOUNT")
    val type: CouponType,

    @Schema(description = "할인 값 (금액할인: 원, 비율할인: %)", example = "10000")
    val discountValue: Int,

    @Schema(description = "사용 가능 여부", example = "true")
    val isActive: Boolean,

    @Schema(description = "남은 수량", example = "100")
    val remainingStock: Int
)

@Schema(description = "쿠폰 타입")
enum class CouponType {
    @Schema(description = "금액 할인")
    AMOUNT,

    @Schema(description = "비율 할인")
    PERCENTAGE
}