package kr.hhplus.be.server.domain.coupon

import java.time.LocalDateTime

// 순수 도메인 모델로 변경
abstract class Coupon(
    open val id: Long = 0L,
    open val name: String,
    open val stock: Long,
    open val active: Boolean = false
) {
    init {
        require(stock >= 0) { "재고는 0 이상이어야 합니다" }
    }

    fun isAvailable(): Boolean {
        return active && stock > 0
    }

    abstract fun discountAmount(originAmount: Long): Long
    
    // 쿠폰의 할인 금액을 반환하는 추상 메서드 추가
    abstract fun getDiscountAmount(): Long

    fun issueTo(userId: Long): IssueCouponAndIssuedCoupon {
        require(isAvailable()) { "사용 불가능한 쿠폰입니다." }
        require(stock > 0) { "쿠폰 재고가 없습니다." }

        return IssueCouponAndIssuedCoupon(
            issuedCoupon = IssuedCoupon(
                couponId = id,
                userId = userId,
                isUsed = false
            ),
            remainingCoupon = createWithDecreasedStock()
        )
    }

    abstract fun createWithDecreasedStock(): Coupon
}

// 순수 도메인 모델로 변경
class AmountCoupon(
    override val id: Long = 0L,
    override val name: String,
    override val stock: Long,
    override val active: Boolean,
    val amount: Long
) : Coupon(
    id, name, stock, active
) {
    init {
        require(amount > 0) { "할인 금액은 0보다 커야 합니다" }
    }

    override fun discountAmount(originAmount: Long): Long {
        var resultAmount = originAmount - amount
        if (resultAmount < 0) {
            throw IllegalArgumentException("Amount는 0이상이여야합니다.")
        }
        return resultAmount
    }
    
    override fun getDiscountAmount(): Long {
        return amount
    }

    override fun createWithDecreasedStock(): Coupon {
        return AmountCoupon(
            id = this.id,
            name = this.name,
            stock = this.stock - 1,
            active = this.active,
            amount = this.amount
        )
    }
}

// 순수 도메인 모델로 변경
class PercentageCoupon(
    override val id: Long = 0L,
    override val name: String,
    override val stock: Long,
    override val active: Boolean,
    val percent: Double
) : Coupon(
    id, name, stock, active
) {
    init {
        require(percent > 0 && percent <= 100) { "할인율은 0%초과 100%이하여야 합니다" }
    }

    override fun discountAmount(originAmount: Long): Long {
        return (originAmount * (1 - percent / 100)).toLong()
    }
    
    override fun getDiscountAmount(): Long {
        // 퍼센트 쿠폰의 경우 고정 할인 금액이 없으므로 0을 반환하거나
        // 또는 percent 값을 Long으로 변환하여 반환 (예: 10% -> 10)
        return percent.toLong()
    }

    override fun createWithDecreasedStock(): Coupon {
        return PercentageCoupon(
            id = this.id,
            name = this.name,
            stock = this.stock - 1,
            active = this.active,
            percent = this.percent
        )
    }
}

// 순수 도메인 모델로 변경
class IssuedCoupon(
    val id: Long = 0L,
    val couponId: Long,
    val userId: Long,
    val isUsed: Boolean,
    val issuedAt: LocalDateTime = LocalDateTime.now()
) {
    fun canBeUsed(): Boolean {
        return !isUsed
    }

    fun useCoupon(): IssuedCoupon {
        require(!isUsed) { "이미 사용된 쿠폰압니다. IssuedCoupon.couponId: $couponId" }
        return IssuedCoupon(
            id = this.id,
            couponId = this.couponId,
            userId = this.userId,
            isUsed = true,
            issuedAt = this.issuedAt
        )
    }
}

// 발급 결과를 담는 데이터 클래스
data class IssueCouponAndIssuedCoupon(
    val issuedCoupon: IssuedCoupon,
    val remainingCoupon: Coupon
)