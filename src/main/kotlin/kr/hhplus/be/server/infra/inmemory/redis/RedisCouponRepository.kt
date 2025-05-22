package kr.hhplus.be.server.infra.inmemory.redis

import kr.hhplus.be.server.domain.coupon.CouponRedisRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class RedisCouponRepository(
    private val redisTemplate: RedisTemplate<String, Any>
) : CouponRedisRepository {

    companion object {
        private const val COUPON_STOCK_KEY_PREFIX = "coupon:stock:"
        private const val COUPON_ISSUED_SET_PREFIX = "coupon:issued:"
        private const val COUPON_PENDING_LIST = "coupon:pending"
        private const val EXPIRE_DAYS = 7L // 키 만료 설정 (7일)
    }

    override fun initializeStock(couponId: Long, stock: Long) {
        val key = getCouponStockKey(couponId)
        redisTemplate.opsForValue().set(key, stock.toString())
        redisTemplate.expire(key, EXPIRE_DAYS, TimeUnit.DAYS)

        // 발급 집합 초기화
        val issuedKey = getCouponIssuedSetKey(couponId)
        redisTemplate.expire(issuedKey, EXPIRE_DAYS, TimeUnit.DAYS)
    }

    override fun decreaseStockAndRegisterUser(couponId: Long, userId: Long): Boolean {
        val stockKey = getCouponStockKey(couponId)
        val issuedSetKey = getCouponIssuedSetKey(couponId)

        // 1. 재고 확인
        val currentStock = getStock(couponId)
        if (currentStock <= 0) {
            return false
        }

        // 2. 중복 발급 확인
        if (hasUserIssuedCoupon(couponId, userId)) {
            return false
        }

        // 3. 재고 감소
        val newStock = redisTemplate.opsForValue().decrement(stockKey)
        newStock?.let {
            if (it < 0) {
                // 재고가 부족한 경우 (다른 요청이 먼저 처리됨)
                redisTemplate.opsForValue().increment(stockKey) // 원복
                return false
            }
        }

        // 4. 발급 집합에 사용자 추가
        redisTemplate.opsForSet().add(issuedSetKey, userId.toString())

        // 5. 발급 요청 대기열에 추가
        redisTemplate.opsForList().rightPush(COUPON_PENDING_LIST, "$couponId:$userId")

        return true
    }

    override fun getPendingIssueRequests(limit: Int): List<Pair<Long, Long>> {
        // 대기열에서 처리할 항목 가져오기 (삭제하지 않고 조회만)
        val pendingItems = redisTemplate.opsForList().range(COUPON_PENDING_LIST, 0, limit - 1.toLong()) ?: emptyList()

        return pendingItems.mapNotNull { item ->
            val parts = (item as String).split(":")
            if (parts.size == 2) {
                val couponId = parts[0].toLongOrNull()
                val userId = parts[1].toLongOrNull()
                if (couponId != null && userId != null) {
                    Pair(couponId, userId)
                } else null
            } else null
        }
    }

    override fun markRequestProcessed(couponId: Long, userId: Long) {
        // 대기열에서 해당 항목 제거
        redisTemplate.opsForList().remove(COUPON_PENDING_LIST, 1, "$couponId:$userId")
    }

    override fun getStock(couponId: Long): Long {
        val stockKey = getCouponStockKey(couponId)
        return redisTemplate.opsForValue().get(stockKey)?.toString()?.toLongOrNull() ?: 0
    }

    override fun hasUserIssuedCoupon(couponId: Long, userId: Long): Boolean {
        val issuedSetKey = getCouponIssuedSetKey(couponId)
        return redisTemplate.opsForSet().isMember(issuedSetKey, userId.toString()) ?: false
    }

    private fun getCouponStockKey(couponId: Long): String {
        return "$COUPON_STOCK_KEY_PREFIX$couponId"
    }

    private fun getCouponIssuedSetKey(couponId: Long): String {
        return "$COUPON_ISSUED_SET_PREFIX$couponId"
    }
} 