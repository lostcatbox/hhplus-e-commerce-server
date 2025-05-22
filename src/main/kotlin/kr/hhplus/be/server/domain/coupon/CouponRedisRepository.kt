package kr.hhplus.be.server.domain.coupon

interface CouponRedisRepository {
    // 쿠폰 재고 초기화
    fun initializeStock(couponId: Long, stock: Long)
    
    // 쿠폰 재고 감소 및 사용자 발급 요청 등록 (원자적 연산)
    fun decreaseStockAndRegisterUser(couponId: Long, userId: Long): Boolean
    
    // 발급 대기열에서 처리되지 않은 발급 요청 가져오기
    fun getPendingIssueRequests(limit: Int = 100): List<Pair<Long, Long>> // couponId, userId
    
    // 발급 요청 처리 완료 표시
    fun markRequestProcessed(couponId: Long, userId: Long)
    
    // 쿠폰 재고 조회
    fun getStock(couponId: Long): Long
    
    // 사용자 발급 여부 확인
    fun hasUserIssuedCoupon(couponId: Long, userId: Long): Boolean
} 