# 📘 Structured Logging 구성 가이드
> 이 문서는 구조화 로그의 필드 구조와 역할을 설명하며, Biz 서비스와 공통 모듈 간 로깅 방식의 차이를 안내합니다.

___
## 📌 개요

이 문서는 공통 모듈에서 제공하는 구조화 로그(Structured Logging) 시스템의 전체 흐름과 목적을 설명합니다.  
서비스 간 로그 일관성을 유지하고, 추적성과 수집 효율성을 높이기 위해 정의된 표준을 기반으로 동작합니다.

> 대부분의 Structured Logging 기능은 Biz 서비스에서 주로 사용되며,  
> 시스템 내부 모듈이나 수집기에서는 로그 수집 목적이 아니므로 기본 SLF4J logger 사용을 권장합니다.

---

## 🧱 로그 구조

모든 로그는 JSON 기반으로 출력되며, 다음 세 가지 로그타입이 존재합니다:

| 로그 타입                          | 설명                              |
| ------------------------------ | ------------------------------- |
| Biz 로그 (`logtype: "biz"`)      | 서비스 흐름 상의 업무 처리 결과 기록           |
| 시스템 로그 (`logtype: "sys"`)      | 예외 및 내부 시스템 오류 기록               |
| Actuator 로그 (`logtype: "act"`) | 헬스 체크 결과 등 Actuator 기반 상태 정보 기록 |

> 로그의 상세 구조는 [log-level-semantics](log-level-semantics.md) 을 참조하세요

---

## 🔎 로그 레벨 규약

로그 레벨은 다음 기준에 따라 사용됩니다:

| 레벨      | 의미       | 설명                      |
| ------- | -------- | ----------------------- |
| `INFO`  | 정상 흐름 정보 | 비즈니스 이벤트, 상태 기록 등       |
| `WARN`  | 주의/경고 상황 | 실패 가능성은 있으나 치명적이지 않은 조건 |
| `ERROR` | 예외 발생    | 오류 발생 및 처리 불가능한 문제      |

📎 자세한 레벨 정의는: [log-level-semantics.md](log-level-semantics.md)

---

## 🔧 StructuredLogger 및 Factory

### 📌 `StructuredLogger`

- 공통 JSON 기반 로그를 출력하기 위한 전용 인터페이스입니다.
- 내부 MDC 설정을 자동으로 포함하며, 필드 누락 없이 로그를 출력합니다.

```java
StructuredLogger logger = StructuredLoggerFactory.getLogger(MyClass.class);
logger.info(LogType.BIZ, "사용자 조회 성공: id={}", userId);
```
### 🏗 `StructuredLoggerFactory`

- 로그 인스턴스를 생성하고, 공통 MDC 설정을 부여합니다.
- Biz 서비스에서는 직접 사용 가능하며, 필요 시 구현체를 교체해 확장할 수 있습니다.

📎 확장 방법 보기: [structured-log-extension.md](structured-log-extension.md)

| 대상         | 사용 여부                 | 비고                      |
| ---------- | --------------------- | ----------------------- |
| Biz 서비스    | ✅ StructuredLogger 사용 | 추적성과 필드 일관성을 위해 필수      |
| 나머지 로깅 서비스 | ⚠ SLF4J logger 권장     | 수집 목적이 없으므로 구조화 로그는 불필요 |
