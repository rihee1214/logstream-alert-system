---
title: "alertmanager-deduplication-via-dedup-key"
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

Alertmanager 중복 알림 방지를 위한 `dedup_key` 라벨 전략 적용 결정

---

## 2. 문제 인식(Problem Recognition)

Wrapping Service는 Prometheus의 메트릭 후처리를 수행하며, Alertmanager로 알림을 직접 전송한다.  
Wrapping이 A-A 구조이므로 동일한 장애 상황에 대해 **동일한 알림이 여러 인스턴스에서 중복 전송**될 수 있으며, 운영자에게 불필요한 반복 알림이 전송되는 문제가 발생한다.

---

## 3. 고려사항(Considerations)

- **Option 1: Alertmanager 기본 deduplication 기능에 의존**
    - 장점: 설정 간단
    - 단점: Prometheus에서만 효과적, Wrapping 알림은 fingerprint 기반 dedup 적용 안됨

- **Option 2: Wrapping에서 외부 상태 저장소(Redis 등) 활용**
    - 장점: 확실한 중복 방지
    - 단점: 외부 의존성 증가, 결합도 상승, 완전한 동기화 불가능

- **Option 3: `dedup_key` 라벨 기반 Alertmanager `group_by` 설정**
    - 장점: 외부 시스템 없이 그룹핑 가능, 구조 단순
    - 단점: Wrapping에서 `dedup_key` 생성 규칙 유지 필요

---

## 4. 최종 결정(Final Decision)

Wrapping Service는 모든 알림에 `dedup_key` 라벨을 포함하고, Alertmanager는
`group_by: ['dedup_key']` 설정을 통해 동일 키에 대한 알림을 그룹핑하고 `repeat_interval` 내 중복 알림을 억제한다.

Wrapping이 직접 Alertmanager에 알림을 전송하는 구조는 단순히 A-A 환경에서 중복 방지를 위함만은 아니다.  
Wrapping은 Prometheus와 달리 “단일 시스템에서 5회 이상 실패”와 같은 고급 판단 기준을 적용하여 정책 기반으로 알림을 전송해야 하며, 이는 Prometheus의 단순 rule-based alert 처리로는 대체할 수 없다.  
따라서 `dedup_key` 전략은 Wrapping이 Alert의 판단 주체가 되면서도, 다중 인스턴스 간 중복 전송을 억제하기 위한 실질적인 해결책으로 결정되었다.

---

## 5. 기대효과(Expected Benefits)

- A-A 구조에서도 단일 장애 알림만 전달됨
- 운영자 혼란 방지 및 Slack/SMS 트래픽 감소
- 외부 락 없이 경량 중복 억제 가능
- 향후 Notification Service와의 일관된 dedup 전략 연동 가능

---

## 6. 계속 고민할 사항(Still Open Issues)

- `dedup_key`의 시간 분해능: 1분 단위? 5분 단위?
- Alertmanager `repeat_interval` 및 `group_wait` 설정과의 최적 조합
- Kibana나 Grafana에서 `dedup_key` 기반 필터링 여부

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
