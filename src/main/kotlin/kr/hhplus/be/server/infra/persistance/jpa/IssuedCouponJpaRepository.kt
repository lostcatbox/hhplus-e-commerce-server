package kr.hhplus.be.server.infra.persistance.jpa

import jakarta.persistence.LockModeType
import kr.hhplus.be.server.domain.coupon.IssuedCoupon
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface IssuedCouponJpaRepository : JpaRepository<IssuedCoupon, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ic FROM issued_coupons ic WHERE ic.id = :issuedCouponId")
    fun findByIdWithPessimisticLock(@Param("issuedCouponId") issuedCouponId: Long): IssuedCoupon?
    fun findAllByUserId(userId: Long): List<IssuedCoupon>
    fun findOneByUserIdAndCouponId(userId: Long, couponId: Long): IssuedCoupon
}