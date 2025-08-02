#!/bin/bash

# STEP 19: 부하 테스트 실행 스크립트
# OrderFacade API 성능 테스트 자동화

echo "======================================"
echo "STEP 19: OrderFacade 부하 테스트 시작"
echo "======================================"

# k6 설치 확인
if ! command -v k6 &> /dev/null; then
    echo "❌ k6가 설치되지 않았습니다."
    echo "📦 설치 방법:"
    echo "  macOS: brew install k6"
    echo "  Linux: sudo apt install k6"
    echo "  Windows: winget install k6"
    exit 1
fi

# 서버 상태 확인
echo "🔍 서버 상태 확인 중..."
if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "❌ 서버가 실행되지 않았습니다. 서버를 먼저 시작해주세요."
    echo "💡 서버 실행: ./gradlew bootRun"
    exit 1
fi

echo "✅ 서버 상태: 정상"

# 테스트 결과 디렉토리 생성
RESULT_DIR="k6-results/$(date +%Y%m%d-%H%M%S)"
mkdir -p $RESULT_DIR

echo "📁 테스트 결과 저장 경로: $RESULT_DIR"

# 1단계: 테스트 데이터 설정 (선택사항)
echo ""
echo "1️⃣  단계 1: 테스트 데이터 준비"
echo "💡 포인트 충전 등 기본 데이터를 준비합니다..."
# k6 run k6/setup-test-data.js --out json=$RESULT_DIR/setup-result.json

# 2단계: Load Test (50 TPS, 5분)
echo ""
echo "2️⃣  단계 2: Load Test 실행"
echo "🎯 목표: 50 TPS, 5분간 안정적 처리"
echo "⏰ 예상 소요 시간: 5분"
read -p "Load Test를 실행하시겠습니까? (y/N): " confirm
if [[ $confirm == [yY] ]]; then
    k6 run k6/order-load-test.js \
        --out json=$RESULT_DIR/load-test-result.json \
        --out summary=$RESULT_DIR/load-test-summary.txt
    echo "✅ Load Test 완료"
else
    echo "⏭️  Load Test 건너뛰기"
fi

# 3단계: Stress Test (10 TPS → 100 TPS, 10분)
echo ""
echo "3️⃣  단계 3: Stress Test 실행"
echo "🎯 목표: 10 TPS → 100 TPS 점진적 증가"
echo "⏰ 예상 소요 시간: 10분"
read -p "Stress Test를 실행하시겠습니까? (y/N): " confirm
if [[ $confirm == [yY] ]]; then
    k6 run k6/order-stress-test.js \
        --out json=$RESULT_DIR/stress-test-result.json \
        --out summary=$RESULT_DIR/stress-test-summary.txt
    echo "✅ Stress Test 완료"
else
    echo "⏭️  Stress Test 건너뛰기"
fi

# 4단계: Peak Test (200 TPS, 30초)
echo ""
echo "4️⃣  단계 4: Peak Test 실행"
echo "🎯 목표: 200 TPS 순간 고부하"
echo "⏰ 예상 소요 시간: 30초"
echo "⚠️  경고: 이 테스트는 시스템에 매우 높은 부하를 가합니다"
read -p "Peak Test를 실행하시겠습니까? (y/N): " confirm
if [[ $confirm == [yY] ]]; then
    k6 run k6/order-peak-test.js \
        --out json=$RESULT_DIR/peak-test-result.json \
        --out summary=$RESULT_DIR/peak-test-summary.txt
    echo "✅ Peak Test 완료"
else
    echo "⏭️  Peak Test 건너뛰기"
fi

# 5단계: 동시성 Test (100명 동시 주문)
echo ""
echo "5️⃣  단계 5: 동시성 Test 실행"
echo "🎯 목표: 재고 정합성 검증"
echo "⏰ 예상 소요 시간: 2분"
echo "💡 재고 10개 상품에 100명이 동시 주문"
read -p "동시성 Test를 실행하시겠습니까? (y/N): " confirm
if [[ $confirm == [yY] ]]; then
    k6 run k6/order-concurrency-test.js \
        --out json=$RESULT_DIR/concurrency-test-result.json \
        --out summary=$RESULT_DIR/concurrency-test-summary.txt
    echo "✅ 동시성 Test 완료"
else
    echo "⏭️  동시성 Test 건너뛰기"
fi

# 결과 요약
echo ""
echo "==============================="
echo "📊 테스트 완료 - 결과 요약"
echo "==============================="
echo "📁 결과 파일 위치: $RESULT_DIR"
echo ""
echo "📋 생성된 파일들:"
ls -la $RESULT_DIR/

echo ""
echo "📈 분석 방법:"
echo "1. JSON 결과 파일을 Grafana나 k6 Cloud로 시각화"
echo "2. Summary 파일에서 주요 지표 확인"
echo "3. 서버 로그에서 에러 패턴 분석"
echo "4. 시스템 리소스 사용량 모니터링"

echo ""
echo "🎯 다음 단계 (STEP 20):"
echo "- 테스트 결과 분석 및 병목 지점 식별"
echo "- 장애 대응 시나리오 문서 작성"
echo "- 성능 개선 방안 도출"

echo ""
echo "✅ 모든 테스트가 완료되었습니다!" 