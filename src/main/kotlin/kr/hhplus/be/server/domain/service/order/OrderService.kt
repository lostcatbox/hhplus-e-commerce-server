package kr.hhplus.be.server.domain.service.order

import OrderHistoryRepository
import OrderRepository
import kr.hhplus.be.server.domain.model.Order
import kr.hhplus.be.server.domain.model.OrderHistory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderHistoryRepository: OrderHistoryRepository
) {
    @Transactional
    fun transitionToProductReady(order: Order): Order {
        val updatedOrder = order.readyProduct()
        val savedOrder = orderRepository.save(updatedOrder)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

    @Transactional
    fun transitionToPaymentReady(order: Order): Order {
        val updatedOrder = order.readyPay()
        val savedOrder = orderRepository.save(updatedOrder)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

    @Transactional
    fun transitionToPaymentComplete(order: Order): Order {
        val updatedOrder = order.finishPay()
        val savedOrder = orderRepository.save(updatedOrder)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

    @Transactional
    fun transitionToOrderFailed(order: Order): Order {
        val updatedOrder = order.failOrder()
        val savedOrder = orderRepository.save(updatedOrder)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

    @Transactional
    fun saveOrderHistory(order: Order) {
        val orderHistory = OrderHistory(
            id = 0L, // DB에서 생성
            orderId = order.id,
            userId = order.userId,
            issuedCouponId = order.issuedCouponId,
            orderLines = order.orderLines,
            orderDateTime = order.orderDateTime,
            totalPrice = order.totalPrice,
            orderStatus = order.orderStatus
        )
        orderHistoryRepository.save(orderHistory)
    }
} 