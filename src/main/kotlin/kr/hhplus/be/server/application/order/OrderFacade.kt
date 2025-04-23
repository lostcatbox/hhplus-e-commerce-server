package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.order.OrderCriteria
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
    fun processOrder(orderCriteria: OrderCriteria) {
        // 1. 유저 검증
        userService.checkActiveUser(orderCriteria.userId)

        // 2. 주문 생성
        val order = orderService.createOrder(orderCriteria)

        // 3. 상품 준비중 상태로 변경
        val productReadyOrder = orderService.changeProductReady(order)

        // 4. 상품 재고 확인 및 차감
        productService.saleOrderProducts(orderCriteria.orderLines)

        // 5. 발급된 쿠폰 사용 및 쿠폰 정보 조회
        val coupon = couponService.useIssuedCoupon(orderCriteria.issuedCouponId)

        // 6. 결제 대기 상태로 변경
        val readyForPaymentOrder = orderService.changePaymentReady(productReadyOrder)

        // 7. 결제 처리
        paymentService.pay(readyForPaymentOrder, coupon)

        // 8. 결제 성공 상태로 변경
        orderService.changePaymentComplete(readyForPaymentOrder)
    }
} 