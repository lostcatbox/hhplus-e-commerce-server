package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.coupon.IssuedCoupon
import kr.hhplus.be.server.domain.coupon.IssuedCouponRepository
import kr.hhplus.be.server.infra.persistance.jpa.IssuedCouponJpaRepository
import kr.hhplus.be.server.infra.persistance.model.IssuedCouponEntity
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull


@Repository
class IssuedCouponRepositoryImpl(
    private val jpaRepository: IssuedCouponJpaRepository
) : IssuedCouponRepository {

    override fun save(issuedCoupon: IssuedCoupon): IssuedCoupon {
        return jpaRepository.save(IssuedCouponEntity.from(issuedCoupon)).toDomain()
    }

    override fun findById(id: Long): IssuedCoupon? {
        return jpaRepository.findById(id).getOrNull()?.toDomain()
    }
} 