package kr.hhplus.be.server.infra.inmemory.redis

import kr.hhplus.be.server.domain.product.PopularProduct
import kr.hhplus.be.server.domain.product.PopularProductRepository
import kr.hhplus.be.server.domain.product.ProductStatisticRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ProductStatisticRepositoryImpl(
    private val popularProductRepository: PopularProductRepository
) : ProductStatisticRepository {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun findAll(): List<PopularProduct> {
        return try {
            popularProductRepository.findAllByDate(LocalDate.now())
        } catch (e: Exception) {
            log.error("인기 상품 조회 중 오류 발생: ${e.message}", e)
            emptyList()
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun findAllByDate(date: LocalDate): List<PopularProduct> {
        return try {
            popularProductRepository.findAllByDate(date)
        } catch (e: Exception) {
            log.error("${date} 인기 상품 조회 중 오류 발생: ${e.message}", e)
            emptyList()
        }
    }

    // 별도의 트랜잭션으로 실행하여 주문 처리 트랜잭션에 영향을 주지 않음
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun incrementOrderCount(productId: Long) {
        try {
            popularProductRepository.incrementOrderCount(productId)
        } catch (e: Exception) {
            // 로그만 남기고 예외는 전파하지 않음
            log.error("상품 ID: ${productId} 주문 카운트 증가 중 오류 발생: ${e.message}", e)
        }
    }
}