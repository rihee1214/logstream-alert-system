---
title: "prometheus-wrapping-ha-structure"
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

Prometheus 및 Wrapping Service의 HA 구성 전략

---

## 2. 문제 인식(Problem Recognition)

Prometheus와 Wrapping Service는 모니터링 및 알림 시스템의 핵심 구성 요소이다.  
고가용성을 확보하지 않으면 메트릭 수집 및 장애 감지가 누락될 수 있으며,  
Wrapping 역시 알림 및 저장을 책임지므로 장애 시 복구 불가능한 데이터 유실이 발생할 수 있다.

---

## 3. 고려사항(Considerations)

- **Option 1: Prometheus와 Wrapping 모두 Leader-Follower 방식 구성**
    - 장점: 중복 없음
    - 단점: Prometheus는 본질적으로 A-A 구조이므로 부적합

- **Option 2: Prometheus A-A 구성, Wrapping도 A-A 구조 유지**
    - 장점: 고가용성 확보, 장애 대응 유연
    - 단점: 저장/알림 중복 이슈 발생 가능

- **Option 3: Prometheus A-A + Wrapping Passive 구조 + 외부 중복 방지 정책**
    - 장점: Prometheus는 자유롭게 구성 가능, Wrapping은 단순화
    - 단점: 중복 제어 책임이 외부로 이관됨

---

## 4. 최종 결정(Final Decision)

Prometheus는 A-A(Active-Active) 구조로 독립적으로 모든 메트릭을 scrape하도록 구성하고,  
Wrapping Service는 각각 자신이 바라보는 Prometheus만 수동적으로 query하는 passive 구조로 유지한다.  
HA는 Prometheus에서 보장되며, Wrapping은 단순히 데이터를 받아 후처리만 수행한다.

중복 저장은 ElasticSearch의 `_id` 기반 제어로 방지하고,  
중복 알림은 `dedup_key` 기반으로 Alertmanager와 Notification Service(정확히는 Kafka)에서 억제한다.

---

## 5. 기대효과(Expected Benefits)

- Prometheus HA로 수집 안정성 확보
- Wrapping이 복잡한 리더 선출이나 락 없이 유지 가능
- 저장/알림은 외부 시스템에서 정제된 방식으로 중복 제어 가능
- 구조 간섭 최소화, 장애 시 영향 범위 최소화

---

## 6. 계속 고민할 사항(Still Open Issues)

- Prometheus 인스턴스 수 증가 시 Wrapping 분산 전략 정교화 필요
- Wrapping 동기화 타이밍 이슈
- Alertmanager deduplication 동작 시점과 Notification fallback window 최적화

- Prometheus A-A 구조에서 동일한 서비스의 `/metrics` endpoint를 여러 인스턴스가 동시에 scrape하게 되므로, 서비스 입장에서는 scrape 요청이 N배로 증가할 수 있다.
- 이로 인해 네트워크 트래픽 및 /metrics endpoint 응답 부하가 증가할 수 있으며,
  향후 시스템 규모 확대 시 scrape interval 조정 또는 Proxy 계층(예: Nginx 캐싱) 도입 여부를 재검토할 필요가 있다.

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
