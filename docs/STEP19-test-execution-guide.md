# STEP 19: 부하 테스트 실행 가이드

## 1. 사전 준비

### 1.1 k6 설치
```bash
# macOS
brew install k6

# Ubuntu/Debian
sudo apt update && sudo apt install k6

# Windows
winget install k6
```

### 1.2 서버 실행
```bash
# Spring Boot 애플리케이션 시작
./gradlew bootRun

# 또는 Docker로 실행
docker-compose up -d
```

### 1.3 서버 상태 확인
```bash
curl http://localhost:8080/actuator/health
```

## 2. 테스트 데이터 준비

### 2.1 기본 데이터 확인
- 사용자 데이터 (1~1000번 사용자)
- 상품 데이터 (1~100번 상품, 충분한 재고)
- 포인트 데이터 (사용자별 100만원)

### 2.2 테스트용 상품 생성 (동시성 테스트용)
```sql
-- 한정 상품 생성 (재고 10개)
INSERT INTO products (id, name, price, stock) 
VALUES (999, '한정상품_동시성테스트', 10000, 10);
```

## 3. 테스트 실행

### 3.1 자동 실행 (권장)
```bash
# 모든 테스트 자동 실행
./k6/run-tests.sh
```

### 3.2 개별 테스트 실행

#### Load Test (50 TPS, 5분)
```bash
k6 run k6/order-load-test.js \
    --out json=results/load-test.json \
    --out summary=results/load-test-summary.txt
```

#### Stress Test (10→100 TPS, 10분)
```bash
k6 run k6/order-stress-test.js \
    --out json=results/stress-test.json \
    --out summary=results/stress-test-summary.txt
```

#### Peak Test (200 TPS, 30초)
```bash
k6 run k6/order-peak-test.js \
    --out json=results/peak-test.json \
    --out summary=results/peak-test-summary.txt
```

#### 동시성 Test (100명 동시 주문)
```bash
k6 run k6/order-concurrency-test.js \
    --out json=results/concurrency-test.json \
    --out summary=results/concurrency-test-summary.txt
```

## 4. 결과 분석

### 4.1 주요 지표 확인

#### 성능 지표
- **평균 응답 시간**: 500ms 이하 목표
- **95th Percentile**: 1초 이하 목표
- **99th Percentile**: 2초 이하 목표
- **TPS**: 목표치 달성 여부
- **에러율**: 1% 이하 목표 (재고 부족 제외)

#### 시스템 지표
```bash
# CPU, 메모리 사용률 모니터링
top -p $(pgrep java)

# 네트워크 연결 상태
netstat -an | grep 8080

# 데이터베이스 연결 상태
# MySQL 프로세스 리스트 확인
```

### 4.2 Summary 파일 분석 예시
```
     ✓ 주문 요청 성공
     ✓ 응답 시간 < 5초
     ✓ 응답 데이터 존재

     checks.........................: 100.00% ✓ 15000      ✗ 0
     data_received..................: 4.2 MB  13 kB/s
     data_sent......................: 3.8 MB  12 kB/s
     http_req_duration..............: avg=245ms    min=12ms     med=180ms    max=2.1s     p(90)=456ms    p(95)=612ms
     http_req_failed................: 0.00%   ✓ 0          ✗ 15000
     http_reqs......................: 15000   49.83/s
     order_response_time............: avg=245ms    min=12ms     med=180ms    max=2.1s     p(90)=456ms    p(95)=612ms
     order_success_rate.............: 100.00% ✓ 15000      ✗ 0
```

### 4.3 에러 패턴 분석

#### 일반적인 에러 유형
1. **재고 부족 (400)**: 정상적인 비즈니스 로직
2. **서버 에러 (500)**: 예상치 못한 애플리케이션 에러
3. **타임아웃 (0)**: 네트워크나 서버 응답 지연
4. **서비스 불가 (503)**: 서버 리소스 부족

#### 로그 분석 방법
```bash
# 애플리케이션 로그에서 에러 패턴 확인
tail -f logs/application.log | grep ERROR

# 특정 시간대 에러 로그 추출
grep "2024-01-15 14:" logs/application.log | grep ERROR
```

## 5. 병목 지점 식별

### 5.1 데이터베이스 병목
```sql
-- 실행 중인 쿼리 확인
SHOW PROCESSLIST;

-- 느린 쿼리 로그 확인
SHOW VARIABLES LIKE 'slow_query_log%';

-- 테이블 락 상태 확인
SHOW ENGINE INNODB STATUS;
```

### 5.2 애플리케이션 병목
- **Thread Pool 상태**: 활성 스레드 수
- **Memory 사용량**: Heap 메모리 부족 여부
- **GC 로그**: Garbage Collection 빈도와 시간

### 5.3 외부 연동 병목
- **Kafka 지연**: 메시지 발송 시간
- **Redis 응답**: 캐시 조회 시간

## 6. 목표 지표와 실제 결과 비교

### 6.1 목표 vs 실제 비교표

| 지표 | 목표 | Load Test | Stress Test | Peak Test |
|------|------|-----------|-------------|-----------|
| 평균 응답시간 | 500ms 이하 | ___ ms | ___ ms | ___ ms |
| 95th Percentile | 1초 이하 | ___ ms | ___ ms | ___ ms |
| 99th Percentile | 2초 이하 | ___ ms | ___ ms | ___ ms |
| TPS | 50/100/200 | ___ TPS | ___ TPS | ___ TPS |
| 에러율 | 1% 이하 | ___% | ___% | ___% |

### 6.2 동시성 테스트 결과
- **예상**: 재고 10개 상품에 100명 주문 → 10개만 성공
- **실제**: ___ 개 성공, ___ 개 실패
- **정합성**: ✅ 성공 / ❌ 실패

## 7. 개선 방향 도출

### 7.1 성능 개선 우선순위
1. **Critical**: 목표치 50% 미달 항목
2. **High**: 목표치 80% 달성 항목
3. **Medium**: 목표치 90% 달성 항목

### 7.2 일반적인 개선 방법

#### 데이터베이스 최적화
- 인덱스 추가/최적화
- 쿼리 튜닝
- 커넥션 풀 크기 조정

#### 애플리케이션 최적화
- 캐싱 도입 (Redis)
- 비동기 처리
- 트랜잭션 범위 최적화

#### 인프라 최적화
- 서버 스펙 증설
- 로드 밸런서 도입
- 데이터베이스 읽기 복제본

## 8. 다음 단계 (STEP 20)

### 8.1 장애 시나리오 작성
- 테스트에서 발견된 병목을 바탕으로 장애 시나리오 작성
- 각 시나리오별 대응 방안 수립

### 8.2 모니터링 개선
- 실시간 알림 설정
- 대시보드 구성
- 로그 분석 자동화

### 8.3 성능 개선 구현
- 우선순위에 따른 개선 작업
- 개선 후 재테스트
- 벤치마크 비교

## 9. 체크리스트

### 9.1 테스트 실행 전
- [ ] k6 설치 완료
- [ ] 서버 정상 실행 확인
- [ ] 테스트 데이터 준비 완료
- [ ] 모니터링 도구 준비

### 9.2 테스트 실행 중
- [ ] 시스템 리소스 모니터링
- [ ] 로그 실시간 확인
- [ ] 네트워크 상태 확인
- [ ] 데이터베이스 상태 확인

### 9.3 테스트 완료 후
- [ ] 결과 파일 백업
- [ ] 주요 지표 분석 완료
- [ ] 병목 지점 식별 완료
- [ ] 개선 방향 도출 완료
- [ ] STEP 20 준비 완료

## 10. 문제 해결

### 10.1 자주 발생하는 문제

#### "Connection refused" 에러
```bash
# 서버 실행 상태 확인
curl http://localhost:8080/actuator/health

# 포트 사용 상태 확인
lsof -i :8080
```

#### "Too many open files" 에러
```bash
# 파일 디스크립터 한계 확인
ulimit -n

# 한계 증가 (macOS/Linux)
ulimit -n 65536
```

#### 메모리 부족 에러
```bash
# JVM 힙 메모리 증가
export JAVA_OPTS="-Xmx4g -Xms2g"
./gradlew bootRun
```

### 10.2 문의 및 지원
- 테스트 관련 문의: [이슈 트래커 링크]
- 성능 최적화 가이드: [문서 링크]
- 모니터링 도구 사용법: [가이드 링크] 