package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// OrderFacade의 가장 DownStream = OrderService <-- UpStream = user, product, coupon 등
// 현실에서의 도메인 해결 과정과 매우 흡사하다.
@Service
class OrderFacade(
    private val orderService: OrderService,
    private val userService: UserService,
    private val productService: ProductService,
    private val paymentService: PaymentService,
    private val couponService: CouponService
) {
    @Transactional
    fun processOrder(order: Order) {
        // 1. 유저 검증
        userService.checkActiveUser(order.userId)
        // 2. 유저 검증
        val productReadyOrder = orderService.changeProductReady(order)
        // 3. 상품 재고 확인 및 차감
        productService.saleOrderProducts(order.orderLines)
        // 4. 발급된 쿠폰 사용 및 쿠폰 정보 조회
        val coupon = couponService.useIssuedCoupon(order.issuedCouponId)
        // 5. 결제 대기 상태로 전환
        val readyForPaymentOrder = orderService.changePaymentReady(productReadyOrder)
        // 6. 결제 처리
        paymentService.pay(readyForPaymentOrder, coupon)
        // 7. 결제 결과에 따른 처리
        orderService.changePaymentComplete(readyForPaymentOrder)
    }
} 