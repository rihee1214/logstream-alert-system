---
title: "metric-alert-architecture-decision"
date: "2025-06-23"
status: "done" # [in-progress|done|canceled]
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ✅ 적용 완료
    - 작업자 : 이리희
    - 완료 일자 : 2025-06-23
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : n/a

---

## 1. 주제(Title)

중~대규모 플랫폼의 메트릭 수집 및 알림 전략 선택

---

## 2. 문제 인식(Problem Recognition)

_기존의 Prometheus + Alertmanager 조합은 단기적인 알림 구성에는 적합하나,  
중~대규모 플랫폼 환경에서는 다음과 같은 한계가 발견됨:

- Alert 정의가 Prometheus 인스턴스에 종속되어 있으며, 이중화와 고가용성(HA)에 약함
- 메트릭의 장기 보존이 어렵고, 운영 환경 변화에 따라 확장성이 떨어짐
- 단일 인스턴스 장애 시 알림 중단 가능성이 존재함

직접 메트릭 수집 서비스를 구축하는 방안도 고려했으나,  
운영 복잡도, 개발 부담, 알림 신뢰도 보장 측면에서 부적절함.

이에 따라, 장기 보존, 고가용성, 멀티 테넌시를 모두 고려한 **Prometheus 확장 솔루션 (Thanos / Mimir 등)** 을 적극적으로 비교 분석함.

---

## 3. 고려사항(Considerations)

- **Option 1: Prometheus + Alertmanager (단일 인스턴스)**  
  - 장점: 단순 구성, 문서 풍부, 빠른 도입  
  - 단점: 단일 장애 지점 존재, 장기 저장 불가, 대규모에 부적합

- **Option 2: 직접 개발한 메트릭 수집 + 알림 시스템**  
  - 장점: 완전한 유연성, 비즈니스 룰 기반 조건 구성 가능  
  - 단점: 개발/운영 부담 큼, 신뢰도 및 유지보수성 낮음

- **Option 3: Prometheus 확장형 아키텍처 (Thanos / Mimir)**  
  - 장점: 고가용성, 장기 저장, 멀티 테넌시, 수평 확장  
  - 단점: 도입 복잡도 높음, 별도의 Storage 요구, 구성요소 다수

---

## 4. 최종 결정(Final Decision)

**Thanos 또는 Mimir 기반의 Prometheus 확장 아키텍처 채택**

- 메트릭 수집 및 알림 시스템은 **단순 경량 구성**이 아닌  
  **플랫폼 단위 모니터링 전략**으로 진화해야 하며,
- 고가용성(HA), Alert 분산 처리, 장기 보존을 자연스럽게 지원할 수 있는 아키텍처가 필수

---

## 5. 기대효과(Expected Benefits)

- 수많은 비즈니스 컨테이너에서 발생하는 메트릭에 대해 유실 없는 고신뢰 수집 가능
- Alertmanager의 분산 구성을 통해 장애 지점 제거 및 정책 유연성 확보
- 별도의 Storage를 통한 비용 효율적 장기 저장 구조
- 모니터링 서비스에 대한 책임 분리 구조 설계 가능
- 향후 팀/조직 단위 확장 시 재활용 가능하며, 확장성 우수

---

## 6. 계속 고민할 사항(Still Open Issues)

- Alert Rule의 중앙 버전 관리 전략 (GitOps 기반 도입 여부 포함)
- Ruler의 도입 시점 및 AlertManager 구성 방식 재정립
- Grafana를 넘어서 Business Layer를 위한 Notification Visualization 방법
- Metric Query 최적화 및 Thanos Query Frontend의 실제 활용 여부

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
