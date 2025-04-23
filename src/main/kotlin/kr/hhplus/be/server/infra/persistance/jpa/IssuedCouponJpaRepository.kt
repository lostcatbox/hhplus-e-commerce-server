package kr.hhplus.be.server.infra.persistance.jpa

import jakarta.persistence.LockModeType
import kr.hhplus.be.server.domain.coupon.IssuedCoupon
import kr.hhplus.be.server.infra.persistance.model.IssuedCouponEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface IssuedCouponJpaRepository : JpaRepository<IssuedCouponEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ic FROM issued_coupons ic WHERE ic.id = :issuedCouponId")
    fun findByIdWithPessimisticLock(@Param("issuedCouponId") issuedCouponId: Long): IssuedCouponEntity?
    fun findAllByUserId(userId: Long): List<IssuedCouponEntity>
    fun findOneByUserIdAndCouponId(userId: Long, couponId: Long): IssuedCouponEntity
}