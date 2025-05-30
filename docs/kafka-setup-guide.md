# Kafka 설치 및 실습 가이드

## 1. Kafka 환경 구성

### 1.1 Docker Compose로 Kafka 실행

```bash
# Kafka 및 Zookeeper 실행
docker-compose -f docker-compose-kafka.yml up -d

# 실행 상태 확인
docker-compose -f docker-compose-kafka.yml ps

# 로그 확인
docker-compose -f docker-compose-kafka.yml logs -f kafka
```

### 1.2 Kafka UI 접속

브라우저에서 `http://localhost:8080`으로 접속하여 Kafka UI를 통해 토픽, 메시지 등을 확인할 수 있습니다.

## 2. CLI를 통한 기본 실습

### 2.1 토픽 생성

```bash
# Kafka 컨테이너 접속
docker exec -it kafka bash

# 토픽 생성
kafka-topics --create \
  --topic order-completed \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# 토픽 목록 확인
kafka-topics --list --bootstrap-server localhost:9092

# 토픽 상세 정보 확인
kafka-topics --describe \
  --topic order-completed \
  --bootstrap-server localhost:9092
```

### 2.2 메시지 발행 및 소비

```bash
# Producer로 메시지 발행
kafka-console-producer \
  --topic order-completed \
  --bootstrap-server localhost:9092 \
  --property "key.separator=:" \
  --property "parse.key=true"

# 메시지 입력 예시
user_1:{"orderId":1,"userId":1,"totalAmount":20000}
user_2:{"orderId":2,"userId":2,"totalAmount":15000}

# Consumer로 메시지 소비 (새 터미널에서)
kafka-console-consumer \
  --topic order-completed \
  --bootstrap-server localhost:9092 \
  --from-beginning \
  --property "print.key=true" \
  --property "key.separator=:"
```

### 2.3 Consumer Group 실습

```bash
# Consumer Group으로 메시지 소비
kafka-console-consumer \
  --topic order-completed \
  --bootstrap-server localhost:9092 \
  --group data-platform-service \
  --property "print.key=true"

# 다른 터미널에서 같은 그룹으로 Consumer 추가
kafka-console-consumer \
  --topic order-completed \
  --bootstrap-server localhost:9092 \
  --group data-platform-service \
  --property "print.key=true"

# Consumer Group 상태 확인
kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group data-platform-service
```

## 3. Spring Boot 애플리케이션 실행

### 3.1 애플리케이션 실행

```bash
# 애플리케이션 실행
./gradlew bootRun
```

### 3.2 주문 API 호출

```bash
# 주문 생성 API 호출
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

### 3.3 로그 확인

애플리케이션 로그에서 다음과 같은 메시지를 확인할 수 있습니다:

```
# Kafka 메시지 발행 로그
주문 완료 이벤트 발행 성공: orderId=1, userId=1, partition=0, offset=0

# Kafka 메시지 소비 로그 (데이터 플랫폼)
주문 완료 이벤트 수신 (데이터 플랫폼): topic=order-completed, partition=0, offset=0, key=user_1

# Kafka 메시지 소비 로그 (알림 서비스)
주문 완료 이벤트 수신 (알림 서비스): topic=order-completed, partition=0, offset=0, key=user_1
```

## 4. 파티션과 메시지 키 실습

### 4.1 파티션별 메시지 분배 확인

```bash
# 다양한 사용자 ID로 주문 생성
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "orderLines": [{"productId": 1, "quantity": 1}]}'

curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 2, "orderLines": [{"productId": 1, "quantity": 1}]}'

curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 3, "orderLines": [{"productId": 1, "quantity": 1}]}'
```

### 4.2 파티션별 메시지 확인

```bash
# 특정 파티션의 메시지만 소비
kafka-console-consumer \
  --topic order-completed \
  --bootstrap-server localhost:9092 \
  --partition 0 \
  --from-beginning

kafka-console-consumer \
  --topic order-completed \
  --bootstrap-server localhost:9092 \
  --partition 1 \
  --from-beginning

kafka-console-consumer \
  --topic order-completed \
  --bootstrap-server localhost:9092 \
  --partition 2 \
  --from-beginning
```

## 5. Consumer Group 동작 확인

### 5.1 여러 Consumer Group 생성

애플리케이션에서는 다음 두 개의 Consumer Group이 동작합니다:
- `data-platform-service`: 데이터 플랫폼 전송용
- `notification-service`: 알림 발송용

### 5.2 Consumer Group 상태 모니터링

```bash
# 모든 Consumer Group 목록 확인
kafka-consumer-groups --bootstrap-server localhost:9092 --list

# 특정 Consumer Group 상세 정보
kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group data-platform-service

kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group notification-service
```

## 6. 정리 및 종료

```bash
# 애플리케이션 종료
Ctrl + C

# Kafka 환경 종료
docker-compose -f docker-compose-kafka.yml down

# 볼륨까지 삭제 (데이터 완전 삭제)
docker-compose -f docker-compose-kafka.yml down -v
```

## 7. 실습 결과 확인 포인트

1. **메시지 발행**: 주문 완료 시 Kafka로 메시지가 발행되는지 확인
2. **파티션 분배**: 사용자 ID에 따라 메시지가 다른 파티션에 분배되는지 확인
3. **Consumer Group**: 두 개의 Consumer Group이 동일한 메시지를 각각 소비하는지 확인
4. **순서 보장**: 같은 사용자의 주문이 같은 파티션에서 순차적으로 처리되는지 확인
5. **장애 처리**: Consumer 장애 시 다른 Consumer가 파티션을 인수받는지 확인 