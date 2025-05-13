---
title: "LogType-and-LoggerInterface-Rework"
date: "2025-05-03"
status: "done" # [in-progress|done|canceled]
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ✅ 적용 완료
    - 작업자 : 이리희
    - 완료 일자 : 2025-05-03 
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : n/a


---

## 1. 주제(Title)

로그 타입 구조 개편 및 로거 인터페이스 리팩토링

---

## 2. 문제 인식(Problem Recognition)

기존에는 로그 타입이 `biz`, `sys`, `default`로 구성되어 있었으나, `default`는 의미 없는 분류로서 실효성이 없었고, `sys` 타입 내에 actuator 로그가 혼재되어 로그 가독성과 분류 정확도가 떨어졌다.

---

## 3. 고려사항(Considerations)

- **Option 1: 기존 구조 유지**
	- 장점  
	    - 기존 로거 및 로그 분기 로직을 그대로 유지할 수 있다.
	- 단점  
	    - actuator 로그와 시스템 로그가 혼재됨  
	    - `default` 로그의 명확한 정의 부재로 오용 위험 있음

- **Option 2: 로그 타입 재정의 및 인터페이스 리팩토링**
	-  장점  
	    - 로그의 의미와 역할이 명확해짐  
	    - actuator, 시스템, 비즈니스 로그의 분리가 가능  
	    - 인터페이스 간결화 가능
	-  단점  
	    - Logger 인터페이스 및 구현체 리팩토링 필요  
	    - 기존 메서드 구조 변경에 따른 영향도 발생

---

## 4. 최종 결정(Final Decision)

Option 2를 채택하여 로그 타입을 `biz`, `act`, `sys`로 개편하고, 로그 인터페이스를 `LogType` enum 기반으로 통합하였다.  
기존의 `infoSys`, `infoBiz` 등 분기된 메서드는 제거하고, `info(LogType type, ...)` 식으로 하나의 메서드로 통일했다.

---

## 5. 기대효과(Expected Benefits)

- 로그 타입별 구분이 명확해져 가독성과 유지보수성이 향상됨  
- actuator 로그를 별도로 관리할 수 있어 모니터링 측면에서 유리  
- 로거 인터페이스가 단순화되어 테스트, 사용성 측면에서도 이득

---

## 6. 계속 고민할 사항(Still Open Issues)

- actuator 로그의 상세 분류 필요 여부  (metric, health)
- logtype이 누락되었을 때의 예외 처리 방식의 통일 필요  

---

# ✨ 추가 확장 항목 (Optional)

## 관련 코드(Linked Code)

| 모듈(Module) | 소스 경로(Source Path) | 클래스명 (Package 포함)                                  | 비고              |
| ---------- | ------------------ | -------------------------------------------------- | --------------- |
| common     | src/main/java      | com.rihee.alerting.common.log.enum.LogType         | LogType enum 정의 |
| common     | src/main/java      | com.rihee.alerting.common.log.StructuredLogger     | 인터페이스 리팩토링      |
| common     | src/main/java      | com.rihee.alerting.common.log.StructuredLoggerImpl | 구현체 리팩토링        |
## 대안 방안(Alternative Options)

- SiftingAppender를 그대로 유지하며 기존 타입을 필터로 처리  
	  →내부 구조 제약으로 인해 appender 수 제한에 걸림

## 리스크 및 대응(Risks & Mitigation)

- **리스크** : 로그 누락 가능성  
	 → **대응** : 기본 타입을 `sys`로 지정하고 logtype 누락시 fallback 처리

- **리스크** : 기존 인터페이스 변경으로 인한 연동 모듈 영향  
	 → **대응** : 각 모듈에 대한 수정과 테스트 완료

## 추후 개정 방향(Future Improvements)

- biz 로그 외에 사용자 정의 로그 타입 확장 고려
- TPS가 많아져서 console로그 대신 파일 작성 후, 가져가는 구조 변경에 대한 고려 필요

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
