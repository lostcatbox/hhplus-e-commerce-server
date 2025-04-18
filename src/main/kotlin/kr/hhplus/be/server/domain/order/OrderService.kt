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
        order.readyProduct()
        val savedOrder = orderRepository.save(order)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

    @Transactional
    fun changePaymentReady(order: Order): Order {
        order.readyPay()
        val savedOrder = orderRepository.save(order)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

    @Transactional
    fun changePaymentComplete(order: Order): Order {
        order.finishPay()
        val savedOrder = orderRepository.save(order)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

    @Transactional
    fun changeOrderFailed(order: Order): Order {
        order.failOrder()
        val savedOrder = orderRepository.save(order)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

    @Transactional
    fun saveOrderHistory(order: Order) {
        val orderHistory = OrderHistory(
            orderId = order.id,
            userId = order.userId,
            issuedCouponId = order.issuedCouponId,
            orderDateTime = order.orderDateTime,
            totalPrice = order.totalPrice,
            orderStatus = order.orderStatus
        )
        orderHistoryRepository.save(orderHistory)
    }
} 