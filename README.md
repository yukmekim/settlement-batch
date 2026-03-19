# settlement-batch

Spring Batch를 활용한 일별 매출 정산 시스템

## 프로젝트 소개

주문 데이터를 기반으로 일별 매출 통계를 집계하고 정산하는 배치 애플리케이션이다.
매일 특정 시간에 전일 주문 데이터를 읽어 매출 통계를 생성하고, 정산 결과를 저장한다.

Spring Batch의 핵심 개념(Job, Step, Chunk, Reader/Processor/Writer)을 실제 정산 도메인에 적용하는 것을 목적으로 한다.

## 기술 스택

- Java 21
- Spring Boot 3.5.0
- Spring Batch 5.x
- Spring Data JPA
- H2 (개발) / MySQL (운영)
- Gradle

## 프로젝트 구조

```
settlement-batch
├── src/main/java/dev/yukmekim/settlement
│   ├── SettlementBatchApplication.java
│   ├── common
│   │   ├── exception
│   │   │   ├── BusinessException.java       # 비즈니스 예외
│   │   │   └── ErrorCode.java               # 에러 코드 enum
│   │   └── response
│   │       ├── Response.java                # 공통 응답 형식
│   │       └── PageResponse.java            # 페이지 응답 형식
│   ├── config
│   │   └── JpaConfig.java                   # JPA Auditing 설정
│   ├── domain
│   │   ├── common
│   │   │   └── BaseTime.java                # createdAt, updatedAt 공통 엔티티
│   │   ├── order
│   │   │   ├── Order.java                   # 주문 엔티티
│   │   │   ├── OrderStatus.java             # 주문 상태 enum
│   │   │   └── OrderRepository.java
│   │   └── settlement
│   │       ├── DailySettlement.java          # 일별 정산 결과 엔티티
│   │       ├── DailySettlementRepository.java
│   │       └── SettlementStatus.java         # 정산 상태 enum
│   ├── batch
│   │   ├── job
│   │   │   └── DailySettlementJobConfig.java
│   │   ├── reader
│   │   │   └── OrderReader.java              # 전일 주문 데이터 조회
│   │   ├── processor
│   │   │   └── SettlementProcessor.java      # 매출 집계 처리
│   │   ├── writer
│   │   │   └── SettlementWriter.java         # 정산 결과 저장
│   │   └── listener
│   │       └── SettlementJobListener.java    # Job 실행 전후 로깅
│   ├── controller
│   ├── service
│   ├── repository
│   ├── dto
│   │   ├── request
│   │   └── response
│   └── scheduler
│       └── SettlementScheduler.java          # 배치 스케줄링
└── src/main/resources
    ├── application.yml          # 공통 설정
    ├── application-local.yml    # 로컬 개발 환경
    ├── application-dev.yml      # 개발 서버 환경
    └── application-prod.yml     # 운영 환경
```

## 도메인 설계

### Order (주문)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| orderNo | String | 주문번호 |
| productName | String | 상품명 |
| amount | Long | 주문 금액 |
| status | OrderStatus | COMPLETED, CANCELLED, REFUNDED |
| orderedAt | LocalDateTime | 주문 일시 |

### DailySettlement (일별 정산)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| settlementDate | LocalDate | 정산 대상 일자 |
| totalOrderCount | Long | 총 주문 건수 |
| totalAmount | Long | 총 매출 금액 |
| cancelledCount | Long | 취소 건수 |
| cancelledAmount | Long | 취소 금액 |
| netAmount | Long | 순매출 (총매출 - 취소금액) |
| status | SettlementStatus | IN_PROGRESS, COMPLETED, FAILED |
| settledAt | LocalDateTime | 정산 완료 시각 |

## 배치 흐름

```
[Scheduler] 매일 새벽 2시 실행
       |
       v
[DailySettlementJob]
       |
       v
[Step 1: 정산 처리]
  Reader   → 전일 주문 데이터를 chunk 단위로 조회
  Processor → 주문 상태별 집계 (완료/취소/환불)
  Writer   → DailySettlement 테이블에 정산 결과 저장
       |
       v
[JobListener] 성공/실패 로깅
```

## 주요 설정

### application.yml (공통)

```yaml
spring:
  batch:
    jdbc:
      initialize-schema: always    # 배치 메타 테이블 자동 생성
    job:
      enabled: false               # 앱 시작 시 자동 실행 방지 (스케줄러가 제어)

settlement:
  schedule:
    cron: "0 0 2 * * *"           # 매일 새벽 2시
  chunk-size: 500
```

### application-local.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:settlement;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

## 실행 방법

### 로컬 환경

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 테스트

```bash
./gradlew test
```

### 수동 실행 (특정 날짜 정산)

```bash
./gradlew bootRun --args='--spring.profiles.active=local --settlement.target-date=2025-03-13'
```

## 배치 설계 시 고려사항

### 멱등성

동일한 날짜에 대해 배치를 여러 번 실행해도 결과가 동일해야 한다.
기존 정산 데이터가 있으면 삭제 후 재생성하거나, 이미 정산 완료 상태이면 Skip하는 방식으로 처리한다.

### 실패 복구

Step 실패 시 Spring Batch의 재시작 기능을 활용한다.
JobParameter로 정산 대상 일자를 넘기면 실패한 Job을 동일 파라미터로 재실행할 수 있다.

### chunk size

chunk-size는 한 트랜잭션에서 처리할 건수를 의미한다.
너무 작으면 DB 호출이 잦아지고, 너무 크면 트랜잭션이 길어진다.
주문 데이터 규모에 따라 100~1000 사이에서 조정한다.

## 확장 포인트

프로젝트를 발전시킬 때 아래 방향을 고려할 수 있다.

- 정산 결과 알림 (Slack, 이메일 등) Step 추가
- 주간/월간 정산 Job 분리
- 파티셔닝을 통한 병렬 처리
- 정산 결과 조회용 API 모듈 분리 (멀티 모듈)
- Jenkins 또는 Spring Cloud Data Flow 연동

## 참고 자료

- [Spring Batch 공식 문서](https://docs.spring.io/spring-batch/reference/)
- [Spring Batch 5.x Migration Guide](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-5.0-Migration-Guide)
