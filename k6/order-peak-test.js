import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 커스텀 메트릭 정의
const orderSuccessRate = new Rate('order_success_rate');
const orderResponseTime = new Trend('order_response_time');

const BASE_URL = 'http://localhost:8080';

// 테스트 설정: Peak Test (200 TPS, 30초간)
export const options = {
    scenarios: {
        peak_test: {
            executor: 'constant-arrival-rate',
            rate: 200, // 200 requests per second
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 400,
            maxVUs: 500,
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<3000', 'p(99)<8000'], // Peak 환경에서는 임계치 더 완화
        http_req_failed: ['rate<0.10'], // 에러율 10% 미만 (피크 환경)
        order_success_rate: ['rate>0.90'], // 주문 성공률 90% 이상
        order_response_time: ['p(95)<3000'], // 주문 응답시간 95% < 3s
    },
};

// 테스트 데이터 생성 함수들
function getRandomUserId() {
    return Math.floor(Math.random() * 1000) + 1; // 1-1000
}

function getRandomProductId() {
    // Peak 테스트에서는 인기 상품에 집중
    const popularProducts = [1, 2, 3, 4, 5]; // 인기 상품 5개
    const randomProducts = Array.from({length: 95}, (_, i) => i + 6); // 나머지 상품들
    
    // 70% 확률로 인기 상품, 30% 확률로 일반 상품
    if (Math.random() < 0.7) {
        return popularProducts[Math.floor(Math.random() * popularProducts.length)];
    } else {
        return randomProducts[Math.floor(Math.random() * randomProducts.length)];
    }
}

function getRandomQuantity() {
    // Peak 시간에는 대량 주문 경향
    return Math.floor(Math.random() * 10) + 1; // 1-10
}

function getRandomCouponId() {
    // Peak 시간에는 쿠폰 사용률 높음
    return Math.random() < 0.5 ? Math.floor(Math.random() * 10) + 1 : null;
}

function generateOrderRequest() {
    const orderLineCount = Math.floor(Math.random() * 2) + 1; // 1-2개 상품 (빠른 결제 중심)
    const orderLines = [];
    
    for (let i = 0; i < orderLineCount; i++) {
        orderLines.push({
            productId: getRandomProductId(),
            quantity: getRandomQuantity()
        });
    }
    
    return {
        userId: getRandomUserId(),
        couponId: getRandomCouponId(),
        orderLines: orderLines
    };
}

export default function () {
    // 랜덤 주문 요청 생성
    const orderPayload = generateOrderRequest();
    
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: {
            test_type: 'peak_test',
            api: 'order'
        }
    };
    
    // 주문 요청 전송
    const startTime = Date.now();
    const response = http.post(
        `${BASE_URL}/v1/orders`,
        JSON.stringify(orderPayload),
        params
    );
    const endTime = Date.now();
    
    // 응답 검증
    const isSuccess = check(response, {
        '서버 응답 수신': (r) => r.status !== 0,
        '서버 다운 아님': (r) => r.status !== 502 && r.status !== 503,
        '응답 시간 < 15초': (r) => r.timings.duration < 15000, // 피크 환경에서는 매우 완화
    });
    
    // 성공/실패 판정
    const isBusinessSuccess = response.status === 200;
    
    // 커스텀 메트릭 기록
    orderSuccessRate.add(isBusinessSuccess);
    orderResponseTime.add(endTime - startTime);
    
    // 중요한 에러만 로깅 (서버 다운이나 타임아웃)
    if (response.status === 0 || response.status >= 502) {
        console.error(`시스템 장애: 사용자=${orderPayload.userId}, 상태=${response.status}`);
    }
    
    // Peak 테스트에서는 대기 시간 최소화
    // sleep(0.001); // 거의 대기하지 않음
}

// 테스트 시작 시 실행
export function setup() {
    console.log('Peak Test 시작 - 200 TPS 순간 고부하 30초간');
    console.log('⚠️  주의: 이 테스트는 시스템에 매우 높은 부하를 가합니다');
    
    // 서버 헬스체크
    const healthResponse = http.get(`${BASE_URL}/actuator/health`);
    if (healthResponse.status !== 200) {
        throw new Error('서버가 준비되지 않았습니다');
    }
    
    console.log('서버 상태: 정상');
    console.log('피크 테스트 특징:');
    console.log('- 인기 상품 중심 주문 (70%)');
    console.log('- 대량 주문 시뮬레이션 (1-10개)');
    console.log('- 높은 쿠폰 사용률 (50%)');
    console.log('- 즉시 주문 패턴 (대기시간 최소)');
    
    return {};
}

// 테스트 종료 시 실행
export function teardown(data) {
    console.log('Peak Test 완료');
    console.log('피크 부하 분석 포인트:');
    console.log('- 시스템이 순간 고부하를 견뎠는가?');
    console.log('- 응답 시간 급증 패턴은?');
    console.log('- 에러율 급상승 지점은?');
    console.log('- 시스템 복구 시간은?');
    console.log('- 인기 상품 재고 고갈 패턴은?');
} 