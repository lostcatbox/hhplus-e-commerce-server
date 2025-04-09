package kr.hhplus.be.server.domain.model

import java.time.LocalDateTime

abstract class Coupon(
    val couponId: Long,
    val name: String,
    val stock: Long, //잔여 수량
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val active: Boolean
) {
    init {
        require(stock >= 0) { "재고는 0 이상이어야 합니다" }
        require(startDate <= endDate) { "시작일이 종료일보다 늦을 수 없습니다" }
    }

    fun isAvailable(): Boolean {
        val now = LocalDateTime.now()
        return active &&
                stock > 0 &&
                now.isAfter(startDate) &&
                now.isBefore(endDate)
    }

    fun isActive(): Boolean {
        return active
    }

    abstract fun discountAmount(originAmount: Long): Long

    fun issueTo(userId: Long): IssueCouponAndIssuedCoupon {
        require(isAvailable()) { "사용 불가능한 쿠폰입니다." }
        require(stock > 0) { "쿠폰 재고가 없습니다." }

        return IssueCouponAndIssuedCoupon(
            issuedCoupon = IssuedCoupon(
                couponId = couponId,
                userId = userId,
                isUsed = false
            ),
            remainingCoupon = createWithDecreasedStock()
        )
    }

    abstract fun createWithDecreasedStock(): Coupon
}


class AmountCoupon(
    couponId: Long,
    name: String,
    stock: Long,
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    active: Boolean,
    val amount: Long
) : Coupon(
    couponId, name, stock, startDate, endDate, active
) {
    init {
        require(amount > 0) { "할인 금액은 0보다 커야 합니다" }
    }

    override fun discountAmount(originAmount: Long): Long {
        val resultAmount = originAmount - amount
        if (resultAmount < 0) {
            throw IllegalArgumentException("Amount는 0이상이여야합니다.")
        }
        return resultAmount
    }

    override fun createWithDecreasedStock(): Coupon {
        return AmountCoupon(
            couponId = couponId,
            name = name,
            stock = stock - 1,
            startDate = startDate,
            endDate = endDate,
            amount = amount,
            active = active
        )
    }
}

class PercentageCoupon(
    couponId: Long,
    name: String,
    stock: Long,
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    active: Boolean,
    val percent: Double
) : Coupon(
    couponId, name, stock, startDate, endDate, active
) {
    init {
        require(percent > 0 && percent <= 100) { "할인율은 0%초과 100%이하여야 합니다" }
    }

    override fun discountAmount(originAmount: Long): Long {
        return (originAmount * (1 - percent / 100)).toLong()
    }

    override fun createWithDecreasedStock(): Coupon {
        return PercentageCoupon(
            couponId = couponId,
            name = name,
            stock = stock - 1,
            startDate = startDate,
            endDate = endDate,
            percent = percent,
            active = active
        )
    }

}

data class IssuedCoupon(
    val couponId: Long,
    val userId: Long,
    val isUsed: Boolean
) {

    fun canBeUsed(): Boolean {
        return !isUsed
    }

    fun useCoupon(): IssuedCoupon {
        require(!isUsed) { "이미 사용된 쿠폰압니다. IssuedCoupon.couponId: $couponId" }
        return this.copy(isUsed = true)
    }

}

// 발급 결과를 담는 데이터 클래스
data class IssueCouponAndIssuedCoupon(
    val issuedCoupon: IssuedCoupon,
    val remainingCoupon: Coupon
)