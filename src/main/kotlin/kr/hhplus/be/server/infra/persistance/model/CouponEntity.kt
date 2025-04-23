package kr.hhplus.be.server.infra.persistance.model

import jakarta.persistence.*
import kr.hhplus.be.server.domain.coupon.AmountCoupon
import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.IssuedCoupon
import kr.hhplus.be.server.domain.coupon.PercentageCoupon

@Entity(name = "coupons")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "coupon_type")
abstract class CouponEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0L,

    open val name: String,

    open val stock: Long,

    @Column(name = "active")
    open val active: Boolean = false
) {
    // 도메인 모델로 변환
    abstract fun toDomain(): Coupon

    companion object {
        // 도메인 모델로부터 엔티티 생성
        fun from(domain: Coupon): CouponEntity {
            return when (domain) {
                is AmountCoupon -> AmountCouponEntity.from(domain)
                is PercentageCoupon -> PercentageCouponEntity.from(domain)
                else -> throw IllegalArgumentException("Unsupported coupon type")
            }
        }
    }
}

@Entity
@DiscriminatorValue("AMOUNT")
class AmountCouponEntity(
    id: Long = 0L,
    name: String,
    stock: Long,
    active: Boolean,
    val amount: Long
) : CouponEntity(
    id, name, stock, active
) {
    override fun toDomain(): Coupon {
        return AmountCoupon(
            id = this.id,
            name = this.name,
            stock = this.stock,
            active = this.active,
            amount = this.amount
        )
    }

    companion object {
        fun from(domain: AmountCoupon): AmountCouponEntity {
            return AmountCouponEntity(
                id = domain.id,
                name = domain.name,
                stock = domain.stock,
                active = domain.active,
                amount = domain.amount
            )
        }
    }
}

@Entity
@DiscriminatorValue("PERCENTAGE")
class PercentageCouponEntity(
    id: Long = 0L,
    name: String,
    stock: Long,
    active: Boolean,
    val percent: Double
) : CouponEntity(
    id, name, stock, active
) {
    override fun toDomain(): Coupon {
        return PercentageCoupon(
            id = this.id,
            name = this.name,
            stock = this.stock,
            active = this.active,
            percent = this.percent
        )
    }

    companion object {
        fun from(domain: PercentageCoupon): PercentageCouponEntity {
            return PercentageCouponEntity(
                id = domain.id,
                name = domain.name,
                stock = domain.stock,
                active = domain.active,
                percent = domain.percent
            )
        }
    }
}

@Entity(name = "issued_coupons")
class IssuedCouponEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "coupon_id")
    val couponId: Long,

    @Column(name = "user_id")
    val userId: Long,

    @Column(name = "is_used")
    val isUsed: Boolean
) {
    // 도메인 모델로 변환
    fun toDomain(): IssuedCoupon {
        return IssuedCoupon(
            id = this.id,
            couponId = this.couponId,
            userId = this.userId,
            isUsed = this.isUsed
        )
    }

    companion object {
        // 도메인 모델로부터 엔티티 생성
        fun from(domain: IssuedCoupon): IssuedCouponEntity {
            return IssuedCouponEntity(
                id = domain.id,
                couponId = domain.couponId,
                userId = domain.userId,
                isUsed = domain.isUsed
            )
        }
    }
} 