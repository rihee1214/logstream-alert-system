---
title: "configmap-reload-polling"
date: "2025-05-13"
status: "done" # [in-progress|done|canceled]
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ✅ 적용 완료
    - 작업자 : 이리희
    - 완료 일자 : 2025-05-13 
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : n/a

---

## 1. 주제(Title)

Actuator 대상 로깅 설정 파일을 ConfigMap으로 마운트 시 사용할 리로드 방식 결정

---

## 2. 문제 인식(Problem Recognition)

Actuator에서 어떤 메트릭 정보를 수집/로깅할지 지정하는 설정 파일을 ConfigMap으로 제공하고, 이를 마운트하여 사용할 계획이다. 해당 설정 파일의 변경 사항을 애플리케이션에서 반영하기 위해 다음 두 가지 방식 중 선택이 필요했다:

- WatchEvent를 사용한 파일 변경 감지
- 주기적 Polling 방식

---

## 3. 고려사항(Considerations)

- **Option 1: WatchEvent 기반 감지**
  - 장점
    - 파일 변경 시 즉각적으로 감지 가능
    - 이벤트 기반으로 불필요한 리소스 사용 없음
  - 단점
    - 가상 컨테이너 환경(Kubernetes 등)에서는 심볼릭 링크, OverlayFS 등으로 인해 Watch 이벤트가 제대로 동작하지 않는 경우가 있음
    - 파일 시스템의 특성상 예상치 못한 방식으로 동작할 가능성이 있음

- **Option 2: Polling 기반 감지**
  - 장점
    - 컨테이너 환경에서도 안정적으로 동작
    - 파일 크기가 작기 때문에 리소스 부담이 거의 없음
  - 단점
    - 파일 변경이 없더라도 지속적으로 파일 접근이 발생

---

## 4. 최종 결정(Final Decision)

_**Options 2: Polling 방식을 사용하기로 결정함.**

가장 핵심적인 이유는 WatchEvent가 **컨테이너 환경(Kubernetes 등)의 파일 시스템 특성상 신뢰할 수 없는 동작을 보일 가능성이 크다는 점**이다. 예를 들어, ConfigMap은 종종 심볼릭 링크나 임시 파일 시스템으로 마운트되며, 이로 인해 WatchService가 변경을 감지하지 못하는 사례가 있다. 실제로 현업 사례에서도 이로 인해 이벤트가 발생하지 않아 설정 변경이 반영되지 않는 문제가 발생했다는 의견을 접했다.

파일 크기가 작고 접근 주기가 짧기 때문에 Polling 방식의 리소스 부담은 거의 없으며, 신뢰성과 구현의 단순함 측면에서도 적합한 선택이라 판단하였다.

---

## 5. 기대효과(Expected Benefits)

- 컨테이너 환경에서도 **일관되고 안정적인 리로드** 가능
- Watch 이벤트 누락으로 인한 설정 미반영 문제 방지
- Polling 방식으로도 충분한 성능 확보 가능

---

## 6. 계속 고민할 사항(Still Open Issues)

- 설정 변경 사항을 반영하는 방식(캐싱 여부 등)과의 연계 최적화

---

# ✨ 추가 확장 항목 (Optional)

## 관련 코드(Linked Code)

| 모듈(Module) | 소스 경로(Source Path) | 클래스명 (Package 포함)                                            | 비고  |
| ---------- | ------------------ | ------------------------------------------------------------ | --- |
| Common     | src/main/java      | com.rihee.alerting.common.actuator.CommonMonitoringScheduler | 수정  |

## 대안 방안(Alternative Options)

- WatchEvent 방식은 구현 간결성과 즉각성 면에서 매력적이나, 가상화된 파일 시스템 환경에서의 불확실성으로 인해 제외함.

## 리스크 및 대응(Risks & Mitigation)

- **리스크**: Polling 주기가 너무 짧을 경우 불필요한 I/O 증가
	- **대응**: Actuator Call 주기를 너무 짧게 만들지 않음.

## 추후 개정 방향(Future Improvements)

- JVM 외부 설정을 동적으로 반영할 수 있는 표준화된 HotReload 프레임워크 도입 검토
	- 설정 변경 감지 → 로직 재 초기화 방식의 decoupling

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
