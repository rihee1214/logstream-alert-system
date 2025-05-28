# 🧩 Logging Interceptor 개발 가이드
>이 문서는 `common` 모듈에서 제공하는 **구조화 로그용 Interceptor 시스템**의 구성 요소와 개발자가 사용하는 방식에 대해 설명합니다.

구조화 로그를 남기기 위해서는 로그에 `traceId`, `spanId`, `parentSpanId` 등의 필드가 일관되게 포함되어야 하며, 이를 위해 커스텀 `@RequestMapping` 계열 애너테이션과 Interceptor가 사용됩니다.

---

## ✅ Structured Request Mapping 애너테이션

공통 모듈은 `@GetMapping`, `@PostMapping` 등 Spring MVC 표준 애너테이션을 래핑한 다음의 구조화 애너테이션 6종을 제공합니다:

| 애너테이션                       | 설명                      |
| --------------------------- | ----------------------- |
| `@StructuredGetMapping`     | `@GetMapping` 래핑        |
| `@StructuredPostMapping`    | `@PostMapping` 래핑       |
| `@StructuredPutMapping`     | `@PutMapping` 래핑        |
| `@StructuredDeleteMapping`  | `@DeleteMapping` 래핑     |
| `@StructuredPatchMapping`   | `@PatchMapping` 래핑      |
| `@StructuredRequestMapping` | `@RequestMapping` 범용 래핑 |
```java
@StructuredGetMapping(value = "/users", spanLabel = "find-user")
public User getUser(...) { ... }
```

---
### ⚙️ 공통 옵션

모든 Structured 계열 애너테이션에는 다음 필드가 포함되어 있습니다:

```java
String spanLabel() default "";
```
>이 `spanLabel`은 해당 요청에서 생성될 `spanId`에 포함되어, 로그 트레이싱 시 어떤 로직 단위였는지를 명확히 구분하는 데 사용됩니다.

---
## ⚙️ 구성 요소 설명

### 1. `CommonInterceptorConfiguration`

- Spring MVC에 Interceptor 및 Argument Resolver를 등록하는 구성 클래스입니다.
- `StructuredLogInterceptor`를 등록하며, 사용자가 커스터마이징한 구현도 주입할 수 있도록 구성되어 있습니다.

### 2. `StructuredLogInterceptor`
- 시스템 정책에 따라 traceId, spanId, parentSpanId를 자동 생성 및 설정하는 단일 인터셉터입니다.
- 모든 서비스에서 동일한 구조화 로그 포맷을 보장하며, 별도의 커스터마이징은 불가능합니다.
- 추후 길이 확장 필요 시, traceId는 32의 배수, spanId는 16의 배수가 되도록 설정하면 된다.
- MDC에 서비스 식별자(`service`), 작업 단위 이름(`name`), 호스트/컨테이너 정보 등을 자동 설정합니다.

---

## 🧠 SpanLabel 시스템

### 1. `@StructuredXXX`의 `spanLabel` 필드는 **클래스 내에 선언된 값의 우선순위를 따릅니다.**

### 2. `SpanLabelBeanPostProcessor`
- 모든 `@StructuredXXX` 애너테이션을 가진 메서드를 스캔하여 `spanLabel`을 수집합니다.

### 3. `SpanLabelRegistry`

- 서비스 전체의 `ClassName#methodName → spanLabel` 맵을 저장하는 registry입니다.
- Interceptor는 여기를 조회하여 label을 설정합니다.
    

---

## 🔧 StructuredLogInterceptor 확장 지점

StructuredLogInterceptor는 시스템 정책으로 고정된 인터셉터입니다.  
더 이상 별도의 Factory 등록이나 상속을 통한 커스터마이징은 지원되지 않으며,  
전체 구조화 로깅 체계를 통일하기 위한 목적상 반드시 공통 구현을 사용해야 합니다.

다만 길이를 증가 시키는 것은 가능하며, common영역을 수정해야합니다.

---

## 💡 개발 시 주의사항

| 항목                    | 설명                                                                                                         |
| --------------------- | ---------------------------------------------------------------------------------------------------------- |
| interceptor 우선순위      | 공통 Interceptor는 `@Order(0)`으로 등록됨.  <br>Biz에서 추가하는 Interceptor는 반드시 `@Order(1)` 이상 사용                      |
| `StructuredLogger` 사용 | Interceptor 내부 로직이나 트레이스 로그를 출력할 때는 반드시 StructuredLogger를 사용                                               |
| 로그 `name` 필드 구성       | `spanLabel`을 기반으로 name 필드가 설정되며, 해당 서비스에서 어떤 작업이 수행되었는지를 명확히 나타냅니다. 모든 컨트롤러 메서드에 spanLabel을 설정하는 것을 권장합니다. |

---
## 📎 관련 문서

- [structured-logging.md](./structured-logging.md)
- [structured-log-extension.md](./structured-log-extension.md)