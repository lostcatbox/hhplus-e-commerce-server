package kr.hhplus.be.server.infra.inmemory.redis

import kr.hhplus.be.server.domain.product.PopularProduct
import kr.hhplus.be.server.domain.product.PopularProductId
import kr.hhplus.be.server.domain.product.PopularProductRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@Repository
class RedisPopularProductRepository(
    private val redisTemplate: RedisTemplate<String, Any>
) : PopularProductRepository {

    companion object {
        private const val POPULAR_PRODUCT_PREFIX = "popular-product"
        private const val EXPIRE_DAYS = 7L // 일주일 후 만료
    }

    override fun incrementOrderCount(productId: Long, date: LocalDate) {
        val key = generateKey(date)
        redisTemplate.opsForZSet().incrementScore(key, productId.toString(), 1.0)

        // 키 만료 설정 (없는 경우에만)
        if (redisTemplate.getExpire(key) == -1L) {
            redisTemplate.expire(key, EXPIRE_DAYS, TimeUnit.DAYS)
        }
    }

    override fun findAllByDate(date: LocalDate, limit: Int): List<PopularProduct> {
        val key = generateKey(date)

        // Redis에서 인기 상품 목록 조회 (score 내림차순)
        val productEntries = redisTemplate.opsForZSet()
            .reverseRangeWithScores(key, 0, limit - 1.toLong())
            ?: return emptyList()

        return productEntries.mapNotNull { entry ->
            val productId = entry.value.toString().toLongOrNull() ?: return@mapNotNull null
            val orderCount = entry.score?.toLong() ?: 0L

            PopularProduct(
                popularProductId = PopularProductId(
                    productId = productId,
                    dateTime = date
                ),
                orderCount = orderCount
            )
        }
    }

    override fun clearAll() {
        // 테스트용으로 모든 키 삭제
        val pattern = "$POPULAR_PRODUCT_PREFIX-*"
        val keys = redisTemplate.keys(pattern)

        if (keys.isNotEmpty()) {
            redisTemplate.delete(keys)
        }
    }

    private fun generateKey(date: LocalDate): String {
        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        return "$POPULAR_PRODUCT_PREFIX-$dateStr"
    }
} 