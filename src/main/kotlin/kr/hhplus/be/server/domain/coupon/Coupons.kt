package kr.hhplus.be.server.domain.coupon

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name = "coupons")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "coupon_type")
abstract class Coupon(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long = -1L,

    open var name: String,
    open var stock: Long,

    @Column(name = "start_date")
    open var startDate: LocalDateTime,

    @Column(name = "end_date")
    open var endDate: LocalDateTime,
    @Column(name = "active")
    open var active: Boolean = false
) {
    init {
        require(stock >= 0) { "재고는 0 이상이어야 합니다" }
        require(startDate <= endDate) { "시작일이 종료일보다 늦을 수 없습니다" }
    }

    fun isAvailable(): Boolean {
        var now = LocalDateTime.now()
        return active &&
                now.isAfter(startDate) &&
                now.isBefore(endDate)
    }

    abstract fun discountAmount(originAmount: Long): Long

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

@Entity
@DiscriminatorValue("AMOUNT")
class AmountCoupon(
    id: Long = -1L,
    name: String,
    stock: Long,
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    active: Boolean,
    var amount: Long
) : Coupon(
    id, name, stock, startDate, endDate, active
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

    override fun createWithDecreasedStock(): Coupon {
        return AmountCoupon(
            id = id,
            name = name,
            stock = stock - 1,
            startDate = startDate,
            endDate = endDate,
            amount = amount,
            active = active
        )
    }
}

@Entity
@DiscriminatorValue("PERCENTAGE")
class PercentageCoupon(
    id: Long = -1L,
    name: String,
    stock: Long,
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    active: Boolean,
    var percent: Double
) : Coupon(
    id, name, stock, startDate, endDate, active
) {
    init {
        require(percent > 0 && percent <= 100) { "할인율은 0%초과 100%이하여야 합니다" }
    }

    override fun discountAmount(originAmount: Long): Long {
        return (originAmount * (1 - percent / 100)).toLong()
    }

    override fun createWithDecreasedStock(): Coupon {
        return PercentageCoupon(
            id = id,
            name = name,
            stock = stock - 1,
            startDate = startDate,
            endDate = endDate,
            percent = percent,
            active = active
        )
    }
}

@Entity(name = "issued_coupons")
data class IssuedCoupon(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1L,

    @Column(name = "coupon_id")
    var couponId: Long,

    @Column(name = "user_id")
    var userId: Long,

    @Column(name = "is_used")
    var isUsed: Boolean
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