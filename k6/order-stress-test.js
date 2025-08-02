import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 커스텀 메트릭 정의
const orderSuccessRate = new Rate('order_success_rate');
const orderResponseTime = new Trend('order_response_time');

const BASE_URL = 'http://localhost:8080';

// 테스트 설정: Stress Test (10 TPS → 100 TPS, 10분간)
export const options = {
    scenarios: {
        stress_test: {
            executor: 'ramping-arrival-rate',
            startRate: 10, // 10 requests per second
            stages: [
                { duration: '2m', target: 20 }, // 2분간 20 TPS까지 증가
                { duration: '2m', target: 40 }, // 2분간 40 TPS까지 증가
                { duration: '2m', target: 60 }, // 2분간 60 TPS까지 증가
                { duration: '2m', target: 80 }, // 2분간 80 TPS까지 증가
                { duration: '2m', target: 100 }, // 2분간 100 TPS까지 증가
            ],
            preAllocatedVUs: 200,
            maxVUs: 300,
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<2000', 'p(99)<5000'], // Stress 환경에서는 임계치 완화
        http_req_failed: ['rate<0.05'], // 에러율 5% 미만 (스트레스 환경)
        order_success_rate: ['rate>0.95'], // 주문 성공률 95% 이상
        order_response_time: ['p(95)<2000'], // 주문 응답시간 95% < 2s
    },
};

// 테스트 데이터 생성 함수들
function getRandomUserId() {
    return Math.floor(Math.random() * 1000) + 1; // 1-1000
}

function getRandomProductId() {
    return Math.floor(Math.random() * 100) + 1; // 1-100
}

function getRandomQuantity() {
    return Math.floor(Math.random() * 5) + 1; // 1-5
}

function getRandomCouponId() {
    // 30% 확률로 쿠폰 사용
    return Math.random() < 0.3 ? Math.floor(Math.random() * 10) + 1 : null;
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
            test_type: 'stress_test',
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
        '주문 요청 응답': (r) => r.status !== 0, // 0이 아니면 응답은 받음
        '서버 에러 아님': (r) => r.status !== 500,
        '응답 시간 < 10초': (r) => r.timings.duration < 10000, // 스트레스 환경에서는 완화
    });
    
    // 성공/실패 판정 (재고 부족은 정상적인 비즈니스 로직)
    const isBusinessSuccess = response.status === 200;
    
    // 커스텀 메트릭 기록
    orderSuccessRate.add(isBusinessSuccess);
    orderResponseTime.add(endTime - startTime);
    
    // 에러 로깅 (500 에러나 타임아웃만 로깅)
    if (response.status >= 500 || response.status === 0) {
        console.error(`심각한 에러: 사용자=${orderPayload.userId}, 상태=${response.status}, 응답=${response.body}`);
    }
    
    // 요청 간 간격 (스트레스 환경에서는 더 짧게)
    sleep(Math.random() * 0.2); // 0-0.2초 랜덤 대기
}

// 테스트 시작 시 실행
export function setup() {
    console.log('Stress Test 시작 - 10 TPS → 100 TPS로 점진 증가');
    
    // 서버 헬스체크
    const healthResponse = http.get(`${BASE_URL}/actuator/health`);
    if (healthResponse.status !== 200) {
        throw new Error('서버가 준비되지 않았습니다');
    }
    
    console.log('서버 상태: 정상');
    console.log('스트레스 테스트 단계:');
    console.log('- 0-2분: 10 → 20 TPS');
    console.log('- 2-4분: 20 → 40 TPS');
    console.log('- 4-6분: 40 → 60 TPS');
    console.log('- 6-8분: 60 → 80 TPS');
    console.log('- 8-10분: 80 → 100 TPS');
    
    return {};
}

// 테스트 종료 시 실행
export function teardown(data) {
    console.log('Stress Test 완료');
    console.log('시스템 한계점 분석:');
    console.log('- 응답 시간 증가 패턴 확인');
    console.log('- 에러율 급증 구간 확인');
    console.log('- 시스템 리소스 사용량 확인');
} 