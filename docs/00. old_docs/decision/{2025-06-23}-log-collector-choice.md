---
title: "log-collector-choice"
date: "2025-06-23"
status: "in-progress" # [in-progress|done|canceled]
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- 🔁 적용 예정
    - 작업자 : 이리희
    - 작업 완료 예정일 : ASAP
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : n/a

---

## 1. 주제(Title)

경량 로그 수집기를 무엇으로 선택할 것인가

---

## 2. 문제 인식(Problem Recognition)

초기에는 ELK 기반 구성(Logstash 또는 Filebeat)을 고려했으나,  
구조화 로그(JSON) 기반 수집을 중심으로 하며, Kafka 연동이 필수인 구조에서는  
기존 솔루션들이 과하게 무겁고, 설정/리소스 측면에서 부적절하다는 판단이 들었다.

특히 **중~대규모 플랫폼의 기반 솔루션**을 구성하기 위한 재검토 과정에서,  
기존의 `EFK(Elasticsearch + Fluentd/Filebeat + Kibana)` 조합 중  
**Elasticsearch(Kibana 포함)**가 **고비용 구조 및 복잡도 이슈로 제거**되면서  
남은 `Filebeat` 역시 **재검토가 불가피**해졌다.

즉, EFK 중 EK의 탈락은 곧 Filebeat의 위치 또한 애매하게 만들었고,  
Kafka 중심의 보다 단순하고 유연한 구조가 필요하다는 판단 하에  
Fluent Bit, Vector 등을 포함한 대체제를 적극 탐색하게 되었다.

---

## 3. 고려사항(Considerations)

- **Option 1: Filebeat**
    - 장점
        - 설정 쉬움, Elasticsearch와 강한 통합
    - 단점
        - Kafka 연동이 제한적이고, 구조화 로그 활용도 낮음

- **Option 2: Fluent Bit**
    - 장점
        - 매우 경량, Kafka 연동 최적화, 빠른 속도, CNCF 후원
    - 단점
        - 복잡한 변형 로직 구현은 어려움

- **Option 3: Logstash**
    - 장점
        - 강력한 파이프라인 구성 가능, 다양한 필터 제공
    - 단점
        - 리소스 많이 사용하고, 설정 복잡

- **Option 4: Vector**
    - 장점
        - Rust 기반 고성능, 유연한 구성
    - 단점
        - 문서 부족, 실사용 사례 적음

---

## 4. 최종 결정(Final Decision)

**Fluent Bit**을 로그 수집기 도구로 최종 채택한다.

- Kafka 연동이 핵심이므로, output plugin 지원이 탄탄한 Fluent Bit이 가장 적합
- 이미 구조화된 JSON 로그를 그대로 Kafka로 전달하는 간단한 요구에 가장 적합
- 매우 경량이며 Filebeat보다 리소스 소비가 적어, 비즈니스 서비스와 병행 구동에 유리
- 공식 문서 및 커뮤니티가 활성화되어 있어, 유지보수 및 운영 안정성도 높음

---

## 5. 기대효과(Expected Benefits)

- Kafka 기반 로그 수집 구조를 가볍게 구축할 수 있음
- 비즈니스 애플리케이션의 성능에 영향을 최소화하면서도 로그 수집이 가능
- 유지보수가 간단하고, 향후 Kafka 메시지 기반 알림/분석 시스템과도 쉽게 연계 가능

---

## 6. 계속 고민할 사항(Still Open Issues)

- Fluent Bit 내에서 구성 가능한 로그 전송 실패 대비 백오프/리트라이 정책의 실제 적용 검토
- Kafka 메시지 스키마(contract)에 대한 확정 및 Schema Registry 도입 여부
- Fluent Bit의 로그 수준 필터링, 로그 압축 설정 등에 대한 추가 실험 필요

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
