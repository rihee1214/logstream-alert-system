# 🧱 Structured Logging 확장 가이드
> 이 문서는 StructuredLogger를 서비스별로 확장하거나 커스터마이징할 수 있는 방법과 제약사항을 정의합니다.

---
## 📌 기본 전제

- 구조화 로그는 JSON 기반으로 출력되며, 모든 로그는 **공통 필드 규약**을 따릅니다.
- 이 필드들은 로그 저장소 및 수집 시스템(`logging-service`, `elasticsearch`)에 의해 분석되므로  
  **구조의 변경은 허용되지 않으며**, **필드 제거나 기존 필드 의미 변경은 금지됩니다.**

> ❗ 구조화 로그 포맷은 **공통 표준**입니다.  
> Biz 서비스는 이 포맷을 기반으로 로그를 생성하지만, 구조 자체를 바꾸지는 않습니다.

---

## ✅ 허용되는 확장 범위

| 항목 | 허용 여부 | 설명 |
|------|-----------|------|
| **메시지 포맷 변경** | ✅ 허용 | 로그 메시지 본문(`message`)을 서비스별로 가공 가능 |
| **메타 필드 추가** | ✅ 허용 | `meta` 내부에 service-specific 필드 추가 가능 |
| **메타 필드 삭제/수정** | ❌ 불가 | 공통 필드는 수집 및 분석 기준이 되므로 변경 금지 |
| **기본 필드 변경/삭제** | ❌ 불가 | `traceId`, `logtype`, `timestamp` 등은 변경 금지 |

> ✅ 단, 새로운 정보(추가 필드)를 meta 안에 담는 것은 자유롭게 가능합니다.

---

## 🔧 StructuredLogger 확장 방법

StructuredLogger의 기본 구현은 `StructuredLoggerImpl`이며,  
서비스별로 확장하려면 **Factory 기반 커스터마이징 구조**를 활용해야 합니다.

### 📌 구성 방식

1. `AbstractStructuredLogInterceptor` 또는 `StructuredLoggerImpl`을 상속
2. 원하는 동작(메시지 포맷, meta 추가 등)을 override
3. `StructuredLoggerFactory`를 Bean으로 등록

### 💡 코드 예시

```java
@Bean
public StructuredLoggerFactory customLoggerFactory() {
    return (registry, serviceName) -> new MyCustomLogger(registry, serviceName);
}
```
- 위처럼 등록하면 공통 모듈이 제공하는 `DefaultStructuredLoggerFactory` 대신 사용자 정의 팩토리가 사용되어 StructuredLogger가 생성됩니다.

---
## 📦 확장 가능한 대상

| 대상           | 설명                                         | 예시                              |
| ------------ | ------------------------------------------ | ------------------------------- |
| `message` 포맷 | 로그 본문에 서비스별 템플릿 적용                         | `회원 조회 성공 - ID: {}`             |
| `meta` 필드 추가 | 특정 업무용 key-value 삽입                        | `"meta": { "userType": "B2B" }` |
| MDC 필드 추가    | interceptor 확장 시 traceId 외 custom MDC 값 삽입 | `"sessionId": "abc123"`         |

---

## ⚠ 주의사항

- 기존 필드 제거, 이름 변경, 의미 변경은 **엄격히 금지**됩니다.
- 확장된 필드는 `logging-service`나 `elasticsearch`가 해석하지 않으며, 
  그에 대한 책임은 Biz 서비스 또는 자체 UI 측에 있습니다.
- 로그 파싱 또는 저장 포맷을 바꾸려면 반드시 platform 팀과 사전 협의가 필요합니다.   

---

## 📎 관련 문서

- [structured-logging.md – 기본 로그 구조 설명](structured-logging.md)
- [log-level-semantics.md](log-level-semantics.md)
- [logging-interceptor.md](logging-interceptor.md)