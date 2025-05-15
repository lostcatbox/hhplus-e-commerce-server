package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.coupon.IssuedCoupon
import kr.hhplus.be.server.domain.coupon.IssuedCouponRepository
import kr.hhplus.be.server.infra.persistance.jpa.IssuedCouponJpaRepository
import org.springframework.stereotype.Repository


@Repository
class IssuedCouponRepositoryImpl(
    private val jpaRepository: IssuedCouponJpaRepository
) : IssuedCouponRepository {

    override fun save(issuedCoupon: IssuedCoupon): IssuedCoupon {
        return jpaRepository.save(issuedCoupon)
    }

    override fun findById(id: Long): IssuedCoupon? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByUserId(userId: Long): List<IssuedCoupon> {
        return jpaRepository.findByUserId(userId)
    }

    override fun findByCouponId(couponId: Long): List<IssuedCoupon> {
        return jpaRepository.findByCouponId(couponId)
    }

    override fun findByCouponIdAndUserId(couponId: Long, userId: Long): IssuedCoupon? {
        return jpaRepository.findByCouponIdAndUserId(couponId, userId)
    }
} 