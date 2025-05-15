package kr.hhplus.be.server.domain.product

import java.time.LocalDate

interface PopularProductRepository {
    fun incrementOrderCount(productId: Long, date: LocalDate = LocalDate.now())
    fun findAllByDate(date: LocalDate = LocalDate.now(), limit: Int = 1000): List<PopularProduct>
    fun clearAll()
} 