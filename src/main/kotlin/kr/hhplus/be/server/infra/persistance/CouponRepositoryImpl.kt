package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponRepository
import kr.hhplus.be.server.domain.coupon.IssueCouponAndIssuedCoupon
import kr.hhplus.be.server.domain.coupon.IssuedCoupon
import kr.hhplus.be.server.infra.persistance.jpa.CouponJpaRepository
import kr.hhplus.be.server.infra.persistance.jpa.IssuedCouponJpaRepository
import kr.hhplus.be.server.infra.persistance.model.CouponEntity
import kr.hhplus.be.server.infra.persistance.model.IssuedCouponEntity
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Repository
class CouponRepositoryImpl(
    private val couponJpaRepository: CouponJpaRepository,
    private val issuedCouponJpaRepository: IssuedCouponJpaRepository
) : CouponRepository {
    @Transactional
    override fun save(coupon: Coupon): Coupon {
        val entity = CouponEntity.from(coupon)
        val savedEntity = couponJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    @Transactional
    override fun findById(couponId: Long): Coupon {
        return couponJpaRepository.findById(couponId).getOrNull()?.toDomain()
            ?: throw EmptyResultDataAccessException("Coupon not found with id: $couponId", 1)
    }

    @Transactional
    override fun findByIdWithPessimisticLock(couponId: Long): Coupon {
        return couponJpaRepository.findByIdWithPessimisticLock(couponId).toDomain()
    }

    @Transactional
    override fun save(issuedCoupon: IssuedCoupon): IssuedCoupon {
        val entity = IssuedCouponEntity.from(issuedCoupon)
        val savedEntity = issuedCouponJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    @Transactional
    override fun save(issueCouponAndIssuedCoupon: IssueCouponAndIssuedCoupon) {
        val savedCoupon = save(issueCouponAndIssuedCoupon.remainingCoupon)
        val savedIssuedCoupon = save(issueCouponAndIssuedCoupon.issuedCoupon)
    }

    override fun findAllByUserId(userId: Long): List<Coupon> {
        val issuedCoupons = issuedCouponJpaRepository.findAllByUserId(userId)
        val couponIds = issuedCoupons.map { it.couponId }

        return couponJpaRepository.findAllByIdIn(couponIds).map { it.toDomain() }
    }

    override fun findByUserIdAndCouponId(userId: Long, couponId: Long): IssuedCoupon {
        return issuedCouponJpaRepository.findOneByUserIdAndCouponId(userId, couponId).toDomain()
    }

    override fun findIssuedCouponById(issuedCouponId: Long): IssuedCoupon {
        return issuedCouponJpaRepository.findById(issuedCouponId).getOrNull()?.toDomain()
            ?: throw EmptyResultDataAccessException("IssuedCoupon not found with id: $issuedCouponId", 1)
    }
}