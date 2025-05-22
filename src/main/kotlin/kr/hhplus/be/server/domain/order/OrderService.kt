package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStatisticRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderHistoryRepository: OrderHistoryRepository,
    private val productService: ProductService,
    private val productStatisticRepository: ProductStatisticRepository
) {
    @Transactional
    fun createOrder(orderCriteria: OrderCriteria): Order {
        // 주문 라인에 상품 가격 정보 설정
        val orderLinesWithPrice = orderCriteria.orderLines.map { lineCriteria ->
            val product = productService.findById(lineCriteria.productId)
            OrderLine(
                productId = lineCriteria.productId,
                productPrice = product.price,
                quantity = lineCriteria.quantity
            )
        }

        // 주문 생성
        val order = Order(
            userId = orderCriteria.userId,
            issuedCouponId = orderCriteria.issuedCouponId,
            orderLines = orderLinesWithPrice,
            orderDateTime = orderCriteria.orderDateTime,
            orderStatus = OrderStatus.주문_요청됨
        )

        // 주문 저장 및 반환
        val savedOrder = orderRepository.save(order)
        saveOrderHistory(savedOrder)
        return savedOrder
    }

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
        //제품 분석 서비스에서 판매량 증가 로직실행
        productStatisticRepository.incrementOrderCount(order.orderLines[0].productId)
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