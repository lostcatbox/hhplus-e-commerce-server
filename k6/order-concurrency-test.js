import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

// 커스텀 메트릭 정의
const stockConsistencySuccess = new Rate('stock_consistency_success');
const orderSuccessCount = new Counter('order_success_count');
const orderFailCount = new Counter('order_fail_count');

const BASE_URL = 'http://localhost:8080';

// 테스트 설정: 동시성 테스트 (100명이 동시에 재고 10개 상품 주문)
export const options = {
    scenarios: {
        concurrency_test: {
            executor: 'shared-iterations',
            vus: 100, // 100명의 동시 사용자
            iterations: 100, // 총 100번의 주문 시도
            maxDuration: '2m',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<5000'], // 동시성 테스트에서 응답 시간
        order_success_count: ['count<=10'], // 성공한 주문은 10개 이하여야 함
        stock_consistency_success: ['rate>0.99'], // 재고 정합성 99% 이상
    },
};

// 한정 상품 정보 (재고 10개)
const LIMITED_PRODUCT_ID = 999; // 특별히 만든 한정 상품
const EXPECTED_STOCK = 10;

// 테스트 데이터 생성 함수들
function getRandomUserId() {
    return Math.floor(Math.random() * 1000) + 1; // 1-1000
}

function generateLimitedOrderRequest() {
    // 모든 사용자가 동일한 한정 상품을 1개씩 주문
    return {
        userId: getRandomUserId(),
        couponId: null, // 간단하게 하기 위해 쿠폰 없음
        orderLines: [
            {
                productId: LIMITED_PRODUCT_ID,
                quantity: 1 // 1개씩만 주문
            }
        ]
    };
}

export default function () {
    // 한정 상품 주문 요청 생성
    const orderPayload = generateLimitedOrderRequest();
    
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: {
            test_type: 'concurrency_test',
            api: 'order',
            product_id: LIMITED_PRODUCT_ID.toString()
        }
    };
    
    // 주문 요청 전송 (정확한 타이밍을 위해 동시 실행)
    const response = http.post(
        `${BASE_URL}/v1/orders`,
        JSON.stringify(orderPayload),
        params
    );
    
    // 응답 검증
    const isOrderSuccess = response.status === 200;
    const isStockError = response.status === 400 && 
                        response.body && 
                        response.body.includes('재고'); // 재고 부족 에러 메시지 확인
    
    // 정합성 검증 (성공이거나 재고 부족 에러만 허용)
    const isConsistent = isOrderSuccess || isStockError;
    
    check(response, {
        '주문 처리 응답': (r) => r.status !== 0,
        '정합성 유지': () => isConsistent,
        '예상된 응답': (r) => r.status === 200 || (r.status === 400 && r.body.includes('재고')),
    });
    
    // 커스텀 메트릭 기록
    stockConsistencySuccess.add(isConsistent);
    
    if (isOrderSuccess) {
        orderSuccessCount.add(1);
        console.log(`✅ 주문 성공: 사용자=${orderPayload.userId}`);
    } else if (isStockError) {
        orderFailCount.add(1);
        console.log(`❌ 재고 부족: 사용자=${orderPayload.userId}`);
    } else {
        console.error(`🚨 예상치 못한 응답: 사용자=${orderPayload.userId}, 상태=${response.status}, 응답=${response.body}`);
    }
}

// 테스트 시작 시 실행
export function setup() {
    console.log('동시성 테스트 시작 - 재고 정합성 검증');
    console.log(`테스트 시나리오: 재고 ${EXPECTED_STOCK}개 상품에 100명이 동시 주문`);
    
    // 서버 헬스체크
    const healthResponse = http.get(`${BASE_URL}/actuator/health`);
    if (healthResponse.status !== 200) {
        throw new Error('서버가 준비되지 않았습니다');
    }
    
    // 한정 상품 생성 (이미 존재한다고 가정하거나, 별도로 DB에 미리 생성)
    console.log(`한정 상품 ID: ${LIMITED_PRODUCT_ID}`);
    console.log(`예상 결과: 정확히 ${EXPECTED_STOCK}개 주문만 성공`);
    console.log('⚠️  이 테스트는 동시성 제어가 올바르게 작동하는지 검증합니다');
    
    return {
        limitedProductId: LIMITED_PRODUCT_ID,
        expectedStock: EXPECTED_STOCK
    };
}

// 테스트 종료 시 실행
export function teardown(data) {
    console.log('동시성 테스트 완료');
    
    // 결과 검증을 위한 상품 재고 조회
    const productResponse = http.get(`${BASE_URL}/v1/products/${data.limitedProductId}`);
    
    if (productResponse.status === 200) {
        try {
            const product = JSON.parse(productResponse.body);
            const remainingStock = product.stock;
            const soldCount = data.expectedStock - remainingStock;
            
            console.log('=== 동시성 테스트 결과 ===');
            console.log(`초기 재고: ${data.expectedStock}개`);
            console.log(`판매된 수량: ${soldCount}개`);
            console.log(`남은 재고: ${remainingStock}개`);
            
            if (soldCount <= data.expectedStock && remainingStock >= 0) {
                console.log('✅ 재고 정합성 검증 성공');
            } else {
                console.log('❌ 재고 정합성 검증 실패 - 데이터 정합성 문제 발생');
            }
            
        } catch (e) {
            console.error('상품 정보 파싱 실패:', e);
        }
    } else {
        console.error('상품 정보 조회 실패');
    }
    
    console.log('동시성 분석 포인트:');
    console.log('- 정확히 재고 수량만큼만 주문 성공했는가?');
    console.log('- 재고 부족 에러가 적절히 발생했는가?');
    console.log('- 데드락이나 예상치 못한 에러는 없었는가?');
    console.log('- 응답 시간은 적절했는가?');
} 