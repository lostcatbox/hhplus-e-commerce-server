# HangHae Plus 이커머스 서비스

## 프로젝트 개요

대용량 트래픽을 처리할 수 있는 이커머스 서비스를 구축하는 프로젝트입니다.

## 주요 기능

- 사용자 관리 및 포인트 시스템
- 상품 관리 및 재고 관리
- 주문 처리 및 결제 시스템
- 쿠폰 발급 및 사용
- 분산락을 통한 동시성 제어
- 이벤트 기반 아키텍처
- **Kafka를 통한 비동기 메시징**

## 기술 스택

- **Language**: Kotlin
- **Framework**: Spring Boot 3.4.1
- **Database**: MySQL 8.0
- **Cache**: Redis
- **Message Queue**: Apache Kafka
- **Build Tool**: Gradle
- **Test**: JUnit 5, MockK, Testcontainers

## 아키텍처

### 이벤트 기반 아키텍처

```
주문 완료 → Application Event → Kafka Producer → Kafka Topic → Consumer Groups
                                                      ↓
                                              [데이터 플랫폼] [알림 서비스]
```

### Kafka 구성

- **Topic**: `order-completed`
- **Partitions**: 3개 (사용자별 분산 처리)
- **Consumer Groups**: 
  - `data-platform-service`: 데이터 수집
  - `notification-service`: 알림 발송

## 실행 방법

### 1. Kafka 환경 구성

```bash
# Kafka 및 Zookeeper 실행
docker-compose -f docker-compose-kafka.yml up -d

# Kafka UI 접속: http://localhost:8080
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3. API 테스트

```bash
# 주문 생성
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderLines": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

## 주요 개선사항

### STEP 15: Application Event 기반 관심사 분리
- 트랜잭션과 부가 로직 분리
- `@TransactionalEventListener(AFTER_COMMIT)` 활용
- 비동기 처리를 통한 성능 향상

### STEP 16: MSA 전환 대비 설계
- 도메인별 배포 단위 분리 설계
- SAGA 패턴을 통한 분산 트랜잭션 처리
- 보상 트랜잭션 전략 수립

### STEP 17: Kafka 기반 메시징 시스템
- 기존 Application Event를 Kafka로 확장
- Producer/Consumer 패턴 구현
- 파티션을 통한 순서 보장 및 병렬 처리
- Consumer Group을 통한 다중 서비스 지원

## 문서

- [Kafka 기초 개념](docs/kafka-basic-concepts.md)
- [Kafka 설치 및 실습 가이드](docs/kafka-setup-guide.md)
- [MSA 도메인 분리 설계](docs/msa-design.md)

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# Kafka 통합 테스트
./gradlew test --tests "*KafkaIntegrationTest"
```

## 모니터링

- **Kafka UI**: http://localhost:8080
- **Application Logs**: 콘솔 출력
- **Metrics**: Spring Actuator 엔드포인트

## 성능 최적화

1. **분산락**: Redis 기반 동시성 제어
2. **캐싱**: Redis 캐시를 통한 조회 성능 향상
3. **비동기 처리**: Kafka를 통한 부가 로직 분리
4. **파티셔닝**: 사용자별 메시지 분산 처리

## 프로젝트

## Getting Started

### Prerequisites

#### Running Docker Containers

`local` profile 로 실행하기 위하여 인프라가 설정되어 있는 Docker 컨테이너를 실행해주셔야 합니다.

```bash
docker-compose up -d
```

## 프로젝트 요구사항
- `e-커머스 상품 주문 서비스`를 구현해 봅니다.
- 상품 주문에 필요한 메뉴 정보들을 구성하고 조회가 가능해야 합니다.
- 사용자는 상품을 여러개 선택해 주문할 수 있고, 미리 충전한 잔액을 이용합니다.
- 상품 주문 내역을 통해 판매량이 가장 높은 상품을 추천합니다.


# 설계도면

## 클래스 다이어그램

- [클래스 다이어그램](https://github.com/lostcatbox/hhplus-e-commerce-server/blob/feature/mockmvctest/docs/class-diagram/class-diagram.mermaid)

## 상태 다이어그램

- [상태 다이어그램](https://github.com/lostcatbox/hhplus-e-commerce-server/blob/feature/mockmvctest/docs/status-diagram/%ED%95%AD%ED%95%B4%202%EC%A3%BC%EC%B0%A8%20%EC%9D%B4%EC%BB%A4%EB%A8%B8%EC%8A%A4%20%EC%83%81%ED%83%9C%20%EB%8B%A4%EC%9D%B4%EC%96%B4%EA%B7%B8%EB%9E%A8(2025.04.02).png)

## ERD

- [ERD](https://github.com/lostcatbox/hhplus-e-commerce-server/blob/feature/mockmvctest/docs/erd/mysqlerd.mermaid)

## 도메인 별 설계도면

### 포인트

#### 시퀀스 다이어그램

- [유저 포인트 조회](https://github.com/lostcatbox/hhplus-e-commerce-server/blob/feature/mockmvctest/docs/sequence-diagram/point-get-scenario.mermaid)
- [유저 포인트 충전](https://github.com/lostcatbox/hhplus-e-commerce-server/blob/feature/mockmvctest/docs/sequence-diagram/point-change-scenario.mermaid)

### 주문

#### 시퀀스 다이어그램

- [주문 요청](https://github.com/lostcatbox/hhplus-e-commerce-server/blob/feature/mockmvctest/docs/sequence-diagram/order-order-scenario.mermaid)

### 쿠폰

#### 시퀀스 다이어그램

- [쿠폰 조회](https://github.com/lostcatbox/hhplus-e-commerce-server/blob/feature/mockmvctest/docs/sequence-diagram/coupon-get-list-scenario.mermaid)
- [쿠폰 발급](https://github.com/lostcatbox/hhplus-e-commerce-server/blob/feature/mockmvctest/docs/sequence-diagram/coupon-issue-scenario.mermaid)

### 제품

#### 시퀀스 다이어그램

- [제품 리스트 조회](https://github.com/lostcatbox/hhplus-e-commerce-server/blob/feature/mockmvctest/docs/sequence-diagram/product-get-list-scenario.mermaid)
- [인기 제품 조회](https://github.com/lostcatbox/hhplus-e-commerce-server/blob/feature/mockmvctest/docs/sequence-diagram/product-get-top5-list-scenario.mermaid)
