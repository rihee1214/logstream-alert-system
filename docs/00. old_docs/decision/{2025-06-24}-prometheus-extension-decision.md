---
title: "prometheus-extension-decision"
date: "2025-06-24"
status: "done" # [in-progress|done|canceled]
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ✅ 적용 완료
    - 작업자 : 이리희
    - 완료 일자 : 2025-06-24
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : n/a

---

## 1. 주제(Title)

Prometheus의 확장 솔루션 선택

---

## 2. 문제 인식(Problem Recognition)

중~대규모 플랫폼 환경에서는 다수의 비즈니스 컨테이너로부터 메트릭을 수집하고, 그 데이터를 기반으로 장애 알림 및 시계열 분석까지 처리할 수 있는 구조가 필요하다.  
기존의 Prometheus + Alertmanager 조합은 단일 인스턴스 중심의 단순한 구성에는 적합하지만 다음과 같은 한계가 존재하였다:

- Prometheus의 로컬 저장소 기반 구조로 인해 메트릭 장기 저장이 불가능함
- 멀티 Prometheus 인스턴스를 통합하는 구조가 부재함

이에 따라 **Prometheus의 구조는 유지하면서도 장기 저장, 수평 확장, HA, 운영 효율성**을 확보할 수 있는 확장 솔루션 도입이 필요해졌다.


---

## 3. 고려사항(Considerations)

- **Option 1: Thanos**
    - 장점
        - Prometheus 구조 유지하면서 확장 가능
        - 객체 저장소(S3 등)를 통한 장기 보존
        - 구성 단순하고 문서/사례가 풍부
        - Grafana 및 PromQL 완전 호환
    - 단점
        - AlertManager 연합 구성 시 약간의 구성 부담 있음
        - 멀티 테넌시 기능은 제한적

- **Option 2: Mimir**
    - 장점
        - Cortex 기반으로 멀티 테넌시 강력
        - 수평 확장 및 고가용성 내장
        - Grafana Labs에서 적극 관리
    - 단점
        - 구성 복잡도 높음
        - 운영 사례, 문서 양은 Thanos 대비 부족

- **Option 3: 직접 수집 서비스 개발 + Notification 구성**
    - 장점
        - 모든 구조를 자율적으로 설계 가능
        - 비즈니스 맞춤 알림 정책 구현 가능
    - 단점
        - 개발 및 유지 관리 비용이 매우 큼
        - 운영 신뢰도 확보가 어려움
        - 일반화/재사용 어려움

---

## 4. 최종 결정(Final Decision)

**Thanos를 Prometheus 확장 솔루션으로 채택한다.**

Prometheus의 기본 구조를 유지하면서 객체 저장소 기반 장기 보존과 수평 확장이 가능하며, 운영 사례가 많고 구성 복잡도가 낮다.  
특히 본 프로젝트는 **단일 조직 내부에서 사용하는 플랫폼**이므로, **멀티 테넌시보다는 운영 효율성과 단순한 관리 구조**가 더 중요하다고 판단하였다.
또한, Thanos는 필요에 따라 `Query Frontend`, `Ruler` 등의 컴포넌트를 **점진적으로 확장 가능한 구조**를 제공하므로, 현재 상황에 적합한 최소 구성부터 시작해 단계적으로 확장해나갈 수 있다는 점에서 전략적으로 유리하다.

---

## 5. 기대효과(Expected Benefits)

- Prometheus 메트릭의 장기 보관 가능 (S3 기반)
- 복수의 Prometheus 인스턴스를 중앙 Query로 통합 가능
- AlertManager의 역할 분리 및 확장 유연성 확보
- 운영 부담 감소 및 장애 대응력 향상
- Grafana 기반 모니터링 그대로 유지 가능

---

## 6. 계속 고민할 사항(Still Open Issues)

- Alertmanager의 고가용성 구성
    - Prometheus 인스턴스별로 Alertmanager를 둘 경우 연합(federation) 전략 설계 필요
    - Ruler 도입 시 Alert 정책 관리 및 전달 방식 변경 가능성 존재
- Ruler 도입 여부 및 타이밍
- Query Frontend 도입에 따른 캐싱 정책 수립
- S3 버킷의 정합성 및 보존 주기 설정
- 특정 비즈니스 단위별 Alert Policy 정교화

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
