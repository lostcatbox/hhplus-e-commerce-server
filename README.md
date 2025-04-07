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
