---
title: "metric-storage-deduplication-via-id"
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
    - 관련 문서 : [architecture.md](../../architecture/architecture.md)

---

## 1. 주제(Title)

Elasticsearch 메트릭 저장 시 `_id` 기반 중복 제어 적용 결정

---

## 2. 문제 인식(Problem Recognition)

Prometheus는 A-A 구조로 모든 인스턴스가 동일한 대상에 대해 메트릭을 scrape한다.  
Wrapping Service도 각자의 Prometheus를 바라보고 데이터를 수집함에 따라 동일한 메트릭이 여러 Wrapping 인스턴스에 의해 중복 저장될 수 있는 문제가 발생한다.

---

## 3. 고려사항(Considerations)

- **Option 1: Redis 또는 DB 기반 분산 락 도입**
    - 장점: 중복 방지 가능
    - 단점: 락 구현 복잡, 네트워크 오류에 민감, 결합도 증가

- **Option 2: Kafka를 통한 메시지 중앙 처리**
    - 장점: 저장과 처리를 분리하고, 중복 전송 억제를 중앙에서 제어할 수 있음
    - 단점: **Kafka 자체는 exactly-once를 보장하지 않으며**, 애플리케이션에서 별도 중복 제어 로직을 구현해야 함

- **Option 3: Elasticsearch `_id` 기반 저장 중복 제어**
    - 장점: 외부 락 없이 idempotent 저장 가능, Wrapping 간 독립성 유지
    - 단점: 고유 키 생성 로직 설계 필요, 사용하는 저장소의 중복 제어 메커니즘에 일부 의존

---

## 4. 최종 결정(Final Decision)

Elasticsearch의 `_id` 필드를 활용하여 중복 저장을 방지한다.  
Wrapping Service는 각 메트릭에 대해 특정 조합을 이용한 해시를 생성하여 `_id`로 사용한다.  
이로써 여러 Wrapping 인스턴스가 동일 데이터를 수집해도 Elastic에서는 1회만 저장된다.

---

## 5. 기대효과(Expected Benefits)

- 분산 락 없이도 중복 저장 방지
- Wrapping 인스턴스 간 독립성 유지
- 장애 대응 로직 간결화
- Kibana/ES에서 중복 없는 정제된 시계열 데이터 조회 가능

---

## 6. 계속 고민할 사항(Still Open Issues)

- `_id` 생성 충돌 최소화를 위한 해시 정책 세분화
- Elastic에 저장 실패 시 fallback 처리를 어떻게 할 것인가
- 향후 Kafka 등과의 연계 시 메시지 키 전략 조정 여부


---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
