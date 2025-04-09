package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.model.Order
import kr.hhplus.be.server.domain.service.coupon.CouponService
import kr.hhplus.be.server.domain.service.order.OrderService
import kr.hhplus.be.server.domain.service.payment.PayCalculator
import kr.hhplus.be.server.domain.service.payment.PaymentService
import kr.hhplus.be.server.domain.service.point.PointService
import kr.hhplus.be.server.domain.service.product.ProductService
import kr.hhplus.be.server.domain.service.user.UserService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OrderFacade(
    private val orderService: OrderService,
    private val userService: UserService,
    private val productService: ProductService,
    private val paymentService: PaymentService,
    private val pointService: PointService,
    private val payCalculator: PayCalculator,
    private val couponService: CouponService
) {
    @Transactional
    fun processOrder(order: Order) {
        // 1. user 검증
        userService.checkActiveUser(order.userId)

        // 2. 상품 준비 상태로 전환
        val orderInPreparation = orderService.transitionToProductReady(order)

        // 3. 상품 재고 확인 및 차감
        productService.saleProcessBy(order.orderLines)

        // 4. 결제 필요 금액 계산
        val finalAmount = order.issuedCouponId.let { couponId ->
            val issuedCouponAndCoupon = couponService.findByIssuedCouponId(order.issuedCouponId)
            payCalculator.calculateFinalAmount(order, issuedCouponAndCoupon)
        }

        // 5. 결제 대기 상태로 전환
        val orderReadyForPayment = orderService.transitionToPaymentReady(orderInPreparation)

        // 6. 포인트 조회 및 결제 처리
        val point = pointService.getPoint(order.userId)
        val paymentResult = paymentService.processPayment(orderReadyForPayment, point, finalAmount)

        // 7. 결제 결과에 따른 처리
        val completedOrder = orderService.transitionToPaymentComplete(orderReadyForPayment)
    }
} 