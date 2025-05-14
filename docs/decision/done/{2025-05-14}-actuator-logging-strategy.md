---
title: "actuator-logging-strategy"
date: "2025-05-14"
status: "done" # [in-progress|done|canceled]
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ✅ 적용 완료
    - 작업자 : 이리희
    - 완료 일자 : 2025-05-14 
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : n/a

---

## 1. 주제(Title)

공통 모듈의 actuator health 상태만 주기적으로 로깅하는 구조로 단순화

---

## 2. 문제 인식(Problem Recognition)

초기 설계에서는 공통 모듈에서 다양한 actuator metrics 정보를 수집하고 structured logging할 수 있도록 handler 기반의 확장 구조를 설계하였다.  
그러나 실제 운영을 고려했을 때, 공통 모듈에서는 단 하나의 endpoint(`/actuator/health`)만 필요하며, 나머지 metrics는 Prometheus 기반 수집으로 이관되는 것이 바람직하다는 판단이 들었다.  
각 비즈니스 모듈은 필요 시 자체 스케줄러를 구성하여 actuator 확장 호출을 구현할 수 있으므로, 공통 모듈이 이를 포괄할 필요는 없다고 판단이 들었다.
그렇기에 복잡한 구조를 유지할 필요가 없고, 책임도 명확하게 분리되어야 했다.

---

## 3. 고려사항(Considerations)

- **Option 1: 기존 구조 유지 (handler 기반 확장 로직 포함)**
	- 장점
	    - 추후 확장 가능성이 열려 있음
	    - 여러 biz 모듈에서 공통 모듈에 handler만 등록하면 확장 가능
	- 단점
	    - 설정 파일, handler 분기, DI 구조 등 복잡도 증가
	    - 실제로는 health 하나만 쓰기 때문에 과도한 구조
	    - 개발자 입장에서 불필요한 확장 지점을 오해할 수 있음

- **Option 2: 구조 단순화 + health 전용 스케줄러만 유지**
	- 장점
		- 공통 모듈이 명확히 “health 상태 로깅”만 책임짐
	    - 코드 간결, 유지보수 용이
	    - 다른 metrics 수집은 Prometheus에 위임하여 역할 분리 완성
	    - biz 영역에서 필요하면 자체 scheduler를 구성해 자유롭게 확장 가능
	- 단점
	    - 향후 공통화 필요시 재구성이 필요할 수 있음

---

## 4. 최종 결정(Final Decision)

Option 2를 선택했다.  
공통 모듈에서는 단일 `ActuatorHealthMonitoringScheduler` 클래스만 남기고,  
handler 구조 및 외부 설정(properties, URI 목록, enabled 설정 등)은 모두 제거했다.  
Prometheus를 통해 metric 수집은 처리하고, 로그 기반으로는 health 상태만 기록하기로 했다.  
비즈니스 도메인에 특화된 actuator 호출은 각 모듈이 책임지도록 위임한다.

---

## 5. 기대효과(Expected Benefits)

- 공통 모듈 코드가 단순화되고 책임이 명확해짐
- 설정 누락, handler 미등록 등의 예외 케이스 제거 
- Prometheus, Zipkin 등 외부 시스템과의 역할 분리가 더욱 명확해짐
- biz 모듈에서 자유롭게 확장 가능한 구조 확보
- 전체 시스템 관측성 구성에서 health, metrics, tracing의 책임 구분이 완성됨

---

## 6. 계속 고민할 사항(Still Open Issues)

- health 상태와 logback events 등 log-to-metrics 간의 통합은 어디까지 할지 정책 정리가 필요함
- Prometheus에서 health를 메트릭 형태로 수집하고 싶을 경우, micrometer 기반으로 별도 exporter 구성 검토 필요

---

# ✨ 추가 확장 항목 (Optional)

## 관련 코드(Linked Code)

| 모듈(Module) | 소스 경로(Source Path) | 클래스명 (Package 포함)                                                    | 비고  |
| ---------- | ------------------ | -------------------------------------------------------------------- | --- |
| common     | src/main/java      | com.rihee.alerting.common.actuator.ActuatorHealthMonitoringScheduler | 수정  |

## 대안 방안(Alternative Options)

- handler 기반 확장 구조는 삭제하지 않고 `git revert` 가능한 상태로 보관
- Prometheus scrape 구조에서 `/actuator/health`를 메트릭으로 변환하는 exporter를 직접 붙이는 방식은 기술 검토 후 제외

## 리스크 및 대응(Risks & Mitigation)

- 확장성이 줄어들 수 있음
	- 대응 방안 : biz에서 스케줄러 직접 구성 가능

## 추후 개정 방향(Future Improvements)

- health 상태의 structured log → Prometheus log exporter로 연동 검토

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
