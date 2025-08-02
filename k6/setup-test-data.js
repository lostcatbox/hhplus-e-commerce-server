import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'http://localhost:8080';

// 테스트 데이터 생성 함수들
export function setupTestData() {
    console.log('테스트 데이터 생성 시작...');
    
    // 1. 상품 데이터 생성 (100개)
    console.log('상품 데이터 생성 중...');
    for (let i = 1; i <= 100; i++) {
        const productPayload = {
            name: `테스트 상품 ${i}`,
            price: Math.floor(Math.random() * 90000) + 10000, // 10,000 ~ 100,000원
            stock: Math.floor(Math.random() * 900) + 100 // 100 ~ 1000개
        };
        
        const productResponse = http.post(
            `${BASE_URL}/v1/products`,
            JSON.stringify(productPayload),
            {
                headers: { 'Content-Type': 'application/json' }
            }
        );
        
        if (i % 20 === 0) {
            console.log(`상품 생성 진행: ${i}/100`);
        }
    }
    
    // 2. 사용자 포인트 충전 (1000명)
    console.log('사용자 포인트 충전 중...');
    for (let i = 1; i <= 1000; i++) {
        const chargePayload = {
            amount: 100000 // 10만원
        };
        
        const chargeResponse = http.post(
            `${BASE_URL}/v1/users/${i}/points`,
            JSON.stringify(chargePayload),
            {
                headers: { 'Content-Type': 'application/json' }
            }
        );
        
        if (i % 100 === 0) {
            console.log(`포인트 충전 진행: ${i}/1000`);
        }
    }
    
    // 3. 쿠폰 데이터 생성 (10개)
    console.log('쿠폰 데이터 생성 중...');
    for (let i = 1; i <= 10; i++) {
        const couponPayload = {
            name: `테스트 쿠폰 ${i}`,
            discountType: i % 2 === 0 ? 'AMOUNT' : 'PERCENTAGE',
            discountValue: i % 2 === 0 ? 5000 : 10, // 5000원 할인 또는 10% 할인
            stock: 1000
        };
        
        const couponResponse = http.post(
            `${BASE_URL}/v1/coupons`,
            JSON.stringify(couponPayload),
            {
                headers: { 'Content-Type': 'application/json' }
            }
        );
        
        console.log(`쿠폰 생성 진행: ${i}/10`);
    }
    
    console.log('테스트 데이터 생성 완료');
}

// 기본 실행 옵션
export const options = {
    scenarios: {
        setup_data: {
            executor: 'shared-iterations',
            vus: 1,
            iterations: 1,
            maxDuration: '10m',
        },
    },
};

export default function () {
    setupTestData();
} 