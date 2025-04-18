package kr.hhplus.be.server.domain.order

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderHistoryRepository: OrderHistoryRepository
) {
    @Transactional
    fun changeProductReady(order: Order): Order {
        val updatedOrder = order.readyProduct()
        val savedOrder = orderRepository.save(updatedOrder)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

    @Transactional
    fun changePaymentReady(order: Order): Order {
        val updatedOrder = order.readyPay()
        val savedOrder = orderRepository.save(updatedOrder)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

    @Transactional
    fun changePaymentComplete(order: Order): Order {
        val updatedOrder = order.finishPay()
        val savedOrder = orderRepository.save(updatedOrder)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

    @Transactional
    fun changeOrderFailed(order: Order): Order {
        val updatedOrder = order.failOrder()
        val savedOrder = orderRepository.save(updatedOrder)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

    @Transactional
    fun saveOrderHistory(order: Order) {
        val orderHistory = OrderHistory(
            id = -1L,
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