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

공통 모듈에서는 `/actuator/health`만 로그 수집 대상으로 유지하고,  
metrics 정보는 Prometheus 구조로 위임하기로 한 결정

---

## 2. 문제 인식(Problem Recognition)

처음에는 공통 모듈에서 health와 metrics 등 다양한 actuator 정보를 structured log로 수집할 수 있도록 handler 기반 구조를 도입했었다.  
그러나 다음과 같은 문제가 발견되었다:

- metrics endpoint는 항목 수가 많고, 시간에 따라 값이 바뀌므로 로그 기반 수집에는 적합하지 않음
- Prometheus가 이미 metrics 정보를 수집하고 있고, 이중 수집이 발생함
- 구조가 복잡해지면서 오히려 공통 모듈의 역할이 불명확해짐
- 결국 health 외의 actuator 정보는 로그보다 시계열 수집이 적합하다는 판단에 도달

---

## 3. 고려사항(Considerations)

- **Option 1: 기존 구조 유지**
	- 장점
		- 각 biz 모듈은 handler를 추가하여 쉬운 확장 가능
		- 설정을 한 곳에 모아서 관리 용이
	- 단점
	    - 구조가 유연하지만 과도한 복잡도 유발
	    - Prometheus와 중복 발생

- **Option 2: 공통 모듈은 health만 담당**
	- 장점
		- 책임 분리 명확하고 유지보수성 우수
	- 단점
	    - 향후 공통화 필요시 재구성이 필요할 수 있음

---

## 4. 최종 결정(Final Decision)

- Option 2를 선택하여 공통 모듈은 health 상태만 로그로 수집하도록 함
- Prometheus가 metrics 정보를 scrape하고, exporter를 통해 ElasticSearch로 넘기도록 아키텍처를 설계
- 기존 handler 구조 및 설정 키 분기, configMap 기반 polling 구조 등은 모두 제거
- 필요 시 각 비즈니스 모듈은 자체적으로 스케줄러를 구성해 actuator metrics를 호출하면 됨

### 4-1. Metric 로그 수집 대신 Prometheus 위임을 선택한 기술적 배경

- Prometheus는 `/actuator/prometheus`를 통해 수치 데이터를 scrape할 수 있음
- 로그는 상태 변화나 이벤트 기록에 적합하지, 수치 기반 추세 분석에 적합하지 않음
- Prometheus + Elasticsearch 구조는 metric 수집과 시각화에 최적화되어 있음
- 로그 기반 metrics 수집은 분석 효용이 낮고, 오히려 로그 저장소를 오염시킬 수 있음
- health는 서비스 자체 상태 점검(UP/DOWN 등)을 위한 것이므로 structured logging으로 충분함

---

## 5. 기대효과(Expected Benefits)

- 공통 모듈 코드가 단순화되고 책임이 명확해짐
- 수집 대상이 명확히 분리되어 시스템 부담이 줄어듦
- 구조적으로 Prometheus와 Elastic 간 연계도 간결해짐
- 추후 필요 시 biz 모듈에서 자유롭게 actuator call 확장 가능

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
