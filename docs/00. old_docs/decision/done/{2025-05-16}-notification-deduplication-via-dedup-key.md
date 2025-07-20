---
title: "notification-deduplication-via-dedup-key"
date: "2025-05-16"
status: "done" # [in-progress|done|canceled]
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ✅ 적용 완료
    - 작업자 : 이리희
    - 완료 일자 : 2025-05-16 
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : [architecture.md](architecture.md)

---

## 1. 주제(Title)

Kafka 기반 Notification 전송 흐름과 `dedup_key` 중복 억제 전략 적용

---

## 2. 문제 인식(Problem Recognition)

Alertmanager 장애 또는 전송 실패 시, Wrapping Service는 Fallback 경로로 알림을 전송해야 한다.  
이때 여러 Wrapping 인스턴스가 동일한 장애에 대해 동시에 fallback 전송할 경우, **Notification 채널(Email, Slack, SMS 등)에서 동일 알림이 중복 발송되는 문제**가 발생할 수 있다.  
또한 direct call 방식은 장애 시 유실 위험이 크기 때문에 **비동기 큐 기반 구조로 전환이 필요하다.**

---

## 3. 고려사항(Considerations)

- **Option 1: 별도 dedup Key 없이 Kafka에서 그대로 수신 처리**
    - 장점: 구성 단순
    - 단점: 중복 메시지 전송 불가피, fallback 구조 부재

- **Option 2: Redis 등 외부 캐시에서 전송 이력 기록 후 중복 차단**
    - 장점: 실용적 수준의 중복 억제 가능
    - 단점: Redis 도입 필요, TTL 정책 유지 필요, 완전한 동기화 보장 어려움

- **Option 3: Kafka 기반 전송 + Notification Service 내 `dedup_key` 기반 in-memory TTL 캐시 활용**
    - 장점: 메시지 유실 없음, 구조 단순, Alertmanager와 dedup 전략 일치
    - 단점: Notification 서버 재시작 시 이력 초기화

---

## 4. 최종 결정(Final Decision)

Wrapping Service는 fallback 알림 발생 시 `dedup_key`가 포함된 알림 메시지를 Kafka의 전용 토픽(`alert-notify-topic`)에 전송한다.  
Notification Service는 이 토픽을 구독하여 메시지를 수신하고, 동일 `dedup_key`에 대해 TTL 내 1회만 전송하도록 in-memory 캐시를 기반으로 중복 여부를 판단한다.

메시지는 Kafka를 통해 비동기적으로 전송되므로 Notification Service의 일시적 장애에도 유실 없이 복구 가능하며,  
dedup_key는 Alertmanager와 동일한 기준(`alert_type + 대상 + 시간`)으로 생성된다.

---

## 5. 기대효과(Expected Benefits)

- Alertmanager가 장애나 통신 실패 시에도 **Kafka 기반 fallback 경로를 통해 알림 유실 없이 전송 가능**
- Slack, Email, SMS 등의 알림 채널에서 **불필요한 중복 알림 제거**
- Alertmanager와 `dedup_key` 전략을 공유함으로써 **알림 흐름 전반의 중복 억제 일관성 확보**
- 추후 Kafka consumer를 분리하여 Slack, Email, SMS 알림을 **비동기적으로 독립 확장 가능**

---

## 6. 계속 고민할 사항(Still Open Issues)

- `dedup_key` TTL 기준 시간 조정 (운영 환경 기준 5분 or 10분?)
- Kafka 메시지 처리 실패 시 dedup 판단과 전송 보장 처리 전략
- Notification Service 재시작 시 dedup 상태 복원 방안 (추후 Redis 기반 확장 고려)
- 알림 전송 채널(Slack, Email, SMS 등)별로 Kafka 토픽을 분리할지 여부
- Kafka Streams 또는 compacted topic을 활용한 deduplication 방식 도입 가능성 검토

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
