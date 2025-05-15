package kr.hhplus.be.server.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kr.hhplus.be.server.domain.product.PopularProduct
import kr.hhplus.be.server.domain.product.PopularProductId
import kr.hhplus.be.server.domain.product.Product
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.time.LocalDate
import kotlin.test.Test

class ProductTest {

    @Nested
    @DisplayName("Product 생성 테스트")
    inner class CreateProductTest {
        @Test
        fun `정상적인 상품 생성`() {
            // when
            val product = Product(
                id = 1L,
                name = "테스트 상품",
                price = 10000L,
                stock = 100L
            )

            // then
            product.id shouldBe 1L
            product.name shouldBe "테스트 상품"
            product.price shouldBe 10000L
            product.stock shouldBe 100L
        }

        @Test
        fun `가격이 0보다 작으면 생성 실패`() {
            shouldThrow<IllegalArgumentException> {
                Product(
                    id = 1L,
                    name = "테스트 상품",
                    price = -1000L,
                    stock = 100L
                )
            }.message shouldBe "가격은 0 이상이여야 합니다."
        }

        @Test
        fun `재고가 0보다 작으면 생성 실패`() {
            shouldThrow<IllegalArgumentException> {
                Product(
                    id = 1L,
                    name = "테스트 상품",
                    price = 1000L,
                    stock = -1L
                )
            }.message shouldBe "재고는 0 이상이여야 합니다."
        }

        @Test
        fun `가격이 0인 상품 생성 가능`() {
            // when
            val product = Product(
                id = 1L,
                name = "무료 상품",
                price = 0L,
                stock = 100L
            )

            // then
            product.price shouldBe 0L
        }
    }

    @Nested
    @DisplayName("상품 판매 테스트")
    inner class SaleProductTest {
        @Test
        fun `정상적인 상품 판매`() {
            // given
            val product = Product(
                id = 1L,
                name = "테스트 상품",
                price = 10000L,
                stock = 100L
            )

            // when
            val saledProduct = product.sale(10L)

            // then
            saledProduct.stock shouldBe 90L
            saledProduct.id shouldBe product.id
            saledProduct.name shouldBe product.name
            saledProduct.price shouldBe product.price

            // 원본 product는 변경되지 않음
            product.stock shouldBe 100L
        }
    }

    @Test
    fun `인기 상품 생성`() {
        val now = LocalDate.now()
        // when
        val popularProduct = PopularProduct(
            PopularProductId(
                productId = 1L,
                dateTime = now
            ),
            orderCount = 0L,

            )

        // then
        popularProduct.popularProductId.productId shouldBe 1L
        popularProduct.orderCount shouldBe 0L
        popularProduct.popularProductId.dateTime shouldBe now
    }

}