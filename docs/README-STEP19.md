# STEP 19: OrderFacade 부하 테스트 구현 완료

## 📋 구현 내용 요약

### 1. 부하 테스트 계획서 작성

- **파일**: `docs/STEP19-load-test-plan.md`
- **내용**: OrderFacade API 성능 테스트를 위한 상세한 계획
- **주요 특징**:
    - 4가지 테스트 시나리오 (Load, Stress, Peak, 동시성)
    - 예상 병목 지점 분석
    - 목표 성능 지표 설정 (50 TPS, 응답시간 < 500ms)

### 2. k6 테스트 스크립트 구현

#### 2.1 Load Test (`k6/order-load-test.js`)

- **목표**: 50 TPS로 5분간 안정적 처리 검증
- **특징**: 랜덤 사용자/상품 조합으로 현실적인 트래픽 시뮬레이션

#### 2.2 Stress Test (`k6/order-stress-test.js`)

- **목표**: 10 TPS → 100 TPS 점진적 증가로 한계점 탐색
- **특징**: 10분간 단계적 부하 증가로 시스템 임계점 식별

#### 2.3 Peak Test (`k6/order-peak-test.js`)

- **목표**: 200 TPS 순간 고부하로 이벤트성 트래픽 대응 능력 검증
- **특징**: 인기 상품 중심 주문 패턴 (70%) 구현

#### 2.4 동시성 Test (`k6/order-concurrency-test.js`)

- **목표**: 재고 10개 상품에 100명 동시 주문으로 정합성 검증
- **특징**: 재고 정합성과 데드락 방지 메커니즘 검증

### 3. 테스트 데이터 준비

- **파일**: `k6/setup-test-data.js`
- **내용**: 1000명 사용자 포인트 충전 자동화

### 4. 테스트 실행 자동화

- **파일**: `k6/run-tests.sh`
- **특징**:
    - 모든 테스트 순차 실행
    - 결과 파일 자동 저장
    - 실행 전 서버 상태 검증

### 5. 실행 가이드 문서

- **파일**: `docs/STEP19-test-execution-guide.md`
- **내용**:
    - 사전 준비부터 결과 분석까지 전체 프로세스
    - 문제 해결 가이드
    - 성능 지표 분석 방법

## 🎯 테스트 시나리오별 특징

### Load Test (정상 부하)

```javascript
// 50 TPS, 5분간
rate: 50, // requests per second
    duration
:
'5m',
// 랜덤 데이터 생성으로 현실적인 트래픽 모방
```

### Stress Test (점진적 부하 증가)

```javascript
// 10 → 100 TPS로 점진 증가
stages: [
    {duration: '2m', target: 20},
    {duration: '2m', target: 40},
    {duration: '2m', target: 60},
    {duration: '2m', target: 80},
    {duration: '2m', target: 100},
]
```

### Peak Test (순간 고부하)

```javascript
// 200 TPS, 30초간
rate: 200,
    duration
:
'30s',
// 인기 상품 중심 주문 (70% 집중)
```

### 동시성 Test (재고 정합성)

```javascript
// 100명이 동시에 재고 10개 상품 주문
vus: 100,
    iterations
:
100,
// 정확히 10개만 성공해야 함
```

## 📊 성능 목표 지표

| 지표              | Load Test | Stress Test | Peak Test |
|-----------------|-----------|-------------|-----------|
| 평균 응답시간         | < 500ms   | < 1s        | < 2s      |
| 95th Percentile | < 1s      | < 2s        | < 3s      |
| 99th Percentile | < 2s      | < 5s        | < 8s      |
| 에러율             | < 1%      | < 5%        | < 10%     |

## 🔧 사용 방법

### 1. 사전 준비

```bash
# k6 설치
brew install k6

# 서버 실행
./gradlew bootRun
```

### 2. 테스트 실행

```bash
# 모든 테스트 자동 실행
./k6/run-tests.sh

# 개별 테스트 실행
k6 run k6/order-load-test.js
```

### 3. 결과 확인

```bash
# 결과 파일 위치
ls -la k6-results/
```

## 🏗️ 파일 구조

```
k6/
├── order-load-test.js       # Load Test (50 TPS, 5분)
├── order-stress-test.js     # Stress Test (10→100 TPS, 10분)
├── order-peak-test.js       # Peak Test (200 TPS, 30초)
├── order-concurrency-test.js # 동시성 Test (100명 동시)
├── setup-test-data.js       # 테스트 데이터 준비
└── run-tests.sh            # 자동 실행 스크립트

docs/
├── STEP19-load-test-plan.md         # 부하 테스트 계획서
└── STEP19-test-execution-guide.md   # 실행 가이드
```

## 🎭 테스트에서 다루는 시나리오

### 1. 일반적인 쇼핑 패턴

- 랜덤 사용자가 1-3개 상품을 1-5개씩 주문
- 30% 확률로 쿠폰 사용
- 실제 사용자 행동 패턴 모방

### 2. 이벤트성 트래픽

- 인기 상품 집중 주문 (70%)
- 높은 쿠폰 사용률 (50%)
- 대량 주문 시뮬레이션 (1-10개)

### 3. 동시성 경합 상황

- 한정 상품에 대한 동시 주문
- 재고 정합성 검증
- 데드락 방지 확인

## 🔍 모니터링 포인트

### 애플리케이션 레벨

- 응답 시간 분포
- 에러율 및 에러 유형
- 주문 성공률

### 시스템 레벨

- CPU/메모리 사용률
- 데이터베이스 커넥션 풀
- JVM GC 패턴

### 비즈니스 레벨

- 재고 정합성
- 트랜잭션 일관성
- 주문 처리 순서

## 🛠️ 기술 스택

- **테스트 도구**: k6 (JavaScript 기반)
- **대상 API**: OrderFacade.processOrder()
- **모니터링**: 커스텀 메트릭 + k6 내장 지표
- **결과 저장**: JSON + Summary 텍스트

---

✅ **STEP 19 완료**: OrderFacade API에 대한 체계적인 부하 테스트 환경이 구축되었습니다. 