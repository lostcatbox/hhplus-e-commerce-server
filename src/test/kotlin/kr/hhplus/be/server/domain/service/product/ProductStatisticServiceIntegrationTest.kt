package kr.hhplus.be.server.domain.service.product

import kr.hhplus.be.server.domain.product.ProductStatisticRepository
import kr.hhplus.be.server.domain.product.ProductStatisticService
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class ProductStatisticServiceIntegrationTest {

    @Autowired
    private lateinit var productStatisticService: ProductStatisticService

    @Autowired
    private lateinit var productStatisticRepository: ProductStatisticRepository

    @BeforeEach
    fun setup() {
        // 통계 데이터 설정 로직이 필요하다면 여기에 구현
    }

    @Test
    fun `findAll - 인기 상품 목록 조회`() {
        // given
        // 실제 구현에 따라 테스트 데이터 설정 필요

        // when
        val popularProducts = productStatisticService.findAll()

        // then
        // 실제 동작에 따라 검증 로직 구현
        assertNotNull(popularProducts)
    }
} 