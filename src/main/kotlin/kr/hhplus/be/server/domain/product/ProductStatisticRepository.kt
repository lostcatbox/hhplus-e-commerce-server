package kr.hhplus.be.server.domain.product

import java.time.LocalDate

interface ProductStatisticRepository {
    fun findAll(): List<PopularProduct>
    fun findAllByDate(date: LocalDate = LocalDate.now()): List<PopularProduct>
    fun incrementOrderCount(productId: Long)
}