package kr.hhplus.be.server.domain.order

import org.springframework.stereotype.Repository

@Repository
interface OrderHistoryRepository {
    fun save(orderHistory: OrderHistory): OrderHistory
}