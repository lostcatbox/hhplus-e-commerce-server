package kr.hhplus.be.server.infra.persistance.jpa

import jakarta.persistence.LockModeType
import kr.hhplus.be.server.domain.coupon.Coupon
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CouponJpaRepository : JpaRepository<Coupon, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM coupons c WHERE c.id = :couponId")
    fun findByIdWithPessimisticLock(@Param("couponId") couponId: Long): Coupon
    fun findAllByIdIn(ids: List<Long>): List<Coupon>
}