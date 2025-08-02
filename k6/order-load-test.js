import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 커스텀 메트릭 정의
const orderSuccessRate = new Rate('order_success_rate');
const orderResponseTime = new Trend('order_response_time');

const BASE_URL = 'http://localhost:8080';

// 테스트 설정: Load Test (50 TPS, 1분)
export const options = {
    scenarios: {
        load_test: {
            executor: 'constant-arrival-rate',
            rate: 50, // 50 requests per second
            timeUnit: '1s',
            duration: '1m',
            preAllocatedVUs: 100,
            maxVUs: 200,
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<1000', 'p(99)<2000'], // 95% < 1s, 99% < 2s
        http_req_failed: ['rate<0.01'], // 에러율 1% 미만 (재고 부족 제외)
        order_success_rate: ['rate>0.99'], // 주문 성공률 99% 이상
        order_response_time: ['p(95)<1000'], // 주문 응답시간 95% < 1s
    },
};

// 테스트 데이터 생성 함수들
function getRandomUserId() {
    return Math.floor(Math.random() * 1000) + 1; // 1-1000
}

function getRandomProductId() {
    return Math.floor(Math.random() * 5) + 1; // 1-5 (더 적은 범위로 테스트)
}

function getRandomQuantity() {
    return Math.floor(Math.random() * 5) + 1; // 1-5
}

function getRandomCouponId() {
    // 쿠폰 사용 안 함 (우선 상품 문제 해결)
    return null;
}

function generateOrderRequest() {
    const orderLineCount = Math.floor(Math.random() * 3) + 1; // 1-3개 상품
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
            test_type: 'load_test',
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
        '주문 요청 성공': (r) => r.status === 200,
        '응답 시간 < 5초': (r) => r.timings.duration < 5000,
        '응답 데이터 존재': (r) => r.body && r.body.length > 0,
    });
    
    // 커스텀 메트릭 기록
    orderSuccessRate.add(isSuccess);
    orderResponseTime.add(endTime - startTime);
    
    // 에러 로깅
    if (response.status !== 200) {
        console.error(`주문 실패: 사용자=${orderPayload.userId}, 상태=${response.status}, 응답=${response.body}`);
    }
    
    // 요청 간 간격 (약간의 랜덤성 추가)
    sleep(Math.random() * 0.5); // 0-0.5초 랜덤 대기
}

// 테스트 시작 시 실행
export function setup() {
    console.log('Load Test 시작 - 목표: 50 TPS, 1분간');
    
    // 서버 헬스체크
    const healthResponse = http.get(`${BASE_URL}/actuator/health`);
    if (healthResponse.status !== 200) {
        throw new Error('서버가 준비되지 않았습니다');
    }
    
    console.log('서버 상태: 정상');
    return {};
}

// 테스트 종료 시 실행
export function teardown(data) {
    console.log('Load Test 완료');
    console.log('결과 분석을 위해 메트릭을 확인하세요');
} 