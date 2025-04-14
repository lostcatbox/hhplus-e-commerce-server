package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponRepository
import kr.hhplus.be.server.domain.coupon.IssueCouponAndIssuedCoupon
import kr.hhplus.be.server.domain.coupon.IssuedCoupon
import kr.hhplus.be.server.infra.persistance.jpa.CouponJpaRepository
import kr.hhplus.be.server.infra.persistance.jpa.IssuedCouponJpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class CouponRepositoryImpl(
    private val couponJpaRepository: CouponJpaRepository,
    private val issuedCouponJpaRepository: IssuedCouponJpaRepository
) : CouponRepository {
    @Transactional
    override fun save(coupon: Coupon): Coupon {
        return couponJpaRepository.save(coupon)
    }

    @Transactional
    override fun findById(couponId: Long): Coupon {
        return couponJpaRepository.findById(couponId).orElseThrow()
    }

    @Transactional
    override fun findByIdWithPessimisticLock(couponId: Long): Coupon {
        return couponJpaRepository.findByIdWithPessimisticLock(couponId)
    }

    @Transactional
    override fun save(issuedCoupon: IssuedCoupon): IssuedCoupon {
        return issuedCouponJpaRepository.save(issuedCoupon)
    }

    @Transactional
    override fun save(issueCouponAndIssuedCoupon: IssueCouponAndIssuedCoupon) {
        val savedCoupon = save(issueCouponAndIssuedCoupon.remainingCoupon)
        val savedIssuedCoupon = save(issueCouponAndIssuedCoupon.issuedCoupon)
    }

    override fun findAllByUserId(userId: Long): List<Coupon> {
        val findAllByUserId = issuedCouponJpaRepository.findAllByUserId(userId)

        return couponJpaRepository.findAllByIdIn(findAllByUserId.map { it.couponId })
    }

    override fun findIssuedCouponById(issuedCouponId: Long): IssuedCoupon {
        return issuedCouponJpaRepository.findById(issuedCouponId).orElseThrow()
    }
}