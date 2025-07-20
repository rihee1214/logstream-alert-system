---
title: prometheus-wrapping-service-introduction
date: 2025-05-15
status: done
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ✅ 적용 완료
  - 작업자 : 이리희
  - 완료 일자 : 2025-05-15
  - 작성자 : 이리희
  - 참석자 : 이리희
  - 관련 문서 : [architecture.md](architecture.md)

---

## 1. 주제(Title)

Prometheus Wrapping Service 도입 결정

---

## 2. 문제 인식(Problem Recognition)

_Prometheus는 기본적으로 메트릭 수집 및 단순 알림 전송에는 유용하지만,
다음과 같은 기능적/운영적 한계가 존재한다:

1. Alertmanager 전송 실패에 대한 Retry 및 Failover 전략을 설정할 수 없다.
2. Elasticsearch에 직접 메트릭을 저장할 수 없어, 별도 remote_write 또는 exporter가 필요하다.
3. 각 서비스에 대해 로깅/모니터링 정책을 유연하게 적용하기 어렵다.

---

## 3. 고려사항(Considerations)

- **Option 1: Prometheus 단독 사용**
    - 장점: 구조 단순, 설정 간단
    - 단점: Alert 실패 대응 불가, Elastic 저장 불가, 정책 유연성 없음

- **Option 2: Wrapping Service 도입**
    - 장점: Alert 실패 감지 및 Fallback 가능, Elastic 저장 가능, 정책 확장성 확보
    - 단점: 구성 복잡도 약간 증가, 별도 모듈 관리 필요

---

## 4. 최종 결정(Final Decision)

Wrapping Service를 도입하여 Prometheus 외부에서 수집된 메트릭을 가공하고 저장 및 알림 전송 책임을 분리한다.  
이를 통해 Prometheus는 수집 전용, Wrapping은 후처리 및 정책 적용 주체로 역할을 분리할 수 있다.

---

## 5. 기대효과(Expected Benefits)

- Alertmanager 장애 시 fallback 알림 전송 가능
- Elasticsearch로의 메트릭 저장 및 인덱스 분리 가능
- 서비스별 모니터링 정책 적용 유연성 확보
- 운영자 입장에서 알림 흐름 및 저장 흐름의 가시성 개선

---

## 6. 계속 고민할 사항(Still Open Issues)

- Wrapping의 AlertManager 전송 정책을 어떻게 deduplication 할 것인지
- 저장 중복에 대한 일관성 확보 방식
- Notification fallback 경로와 Alert 중복 제어 방식

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
