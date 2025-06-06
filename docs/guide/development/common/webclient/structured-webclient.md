# 📄 StructuredWebClient Internal Design

> 이 문서는 공통 컴포넌트인 `StructuredWebClient`의 내부 동작 구조 및 설계 의도를 설명합니다.

## 🧭 대상

- 이 문서는 공통 컴포넌트를 개발하거나 확장하는 개발자에게 필요한 내용을 다룹니다.
- 일반 비즈니스 서비스 개발자는 [structured-webclient-usage.md](../../biz/structured-webclient-usage) 문서를 참조하십시오.

## 🎯 목적

이 문서는 StructuredWebClient 구성 요소 중 StructuredMonoWebClient를 중심으로, 내부 설계와 확장 원칙, 그리고 실제 사용 예시를 설명합니다.

본 구조는 다음의 요구사항을 바탕으로 설계되었습니다:
- 모든 호출에 대한 traceId 통일 및 통제
- WebClient 호출 시 로깅 정책, MDC 전파, 응답 추적을 일관되게 강제
- 에러 처리와 응답 파싱의 책임을 분리하여 재사용성과 예측 가능성 확보

이를 통해 고급 공통 개발자가 구조를 손쉽게 이해하고, 필요한 경우 내부 정책에 맞는 커스터마이징을 할 수 있도록 지원합니다.

## 🧠 설계 철학 및 추상화 원칙

- 모든 호출은 traceId/SpanId가 포함된 **단일 트레이싱 체계 안에서 동작**해야 합니다.
- 외부 시스템은 경계로 간주되며, 외부의 traceId는 **기록만 하고 내부 추적에는 반영하지 않습니다.**
- 응답은 항상 `Mono<WebClientCallResult<R>>` 또는 `Mono.error()` 흐름으로 **표준화된 처리**를 유도합니다.
- 예외 처리 책임은 **호출자에 위임**하며, 구조화된 로그를 통해 상태 진단이 가능하도록 설계합니다.
- 인증, Retry, 공통 헤더 삽입 등은 **WebClient.Builder 레벨에서 주입 가능**하도록 개방합니다.

>본 컴포넌트는 **비즈니스 호출이 아닌 공통 호출 처리의 모범 사례를 제시하기 위해 설계되었으며**, 
>**응답 데이터가 아닌 호출 자체의 통제 가능성과 추적 가능성 확보**에 중점을 둡니다.

## ⚙️ 책임 경계 및 확장 가이드 (공통 컴포넌트 설계자 필독)

StructuredMonoWebClient는 **로깅 일관성, MDC 전파, 추적 정보 통합 관리**를 위한 공통 HTTP 호출 유틸입니다. 이 구조는 특정 개발자나 호출 패턴에 종속되지 않으며, 다음과 같은 책임 경계와 확장 가이드라인을 따릅니다.
### 📌 책임 경계

- 요청/응답 처리 흐름은 반드시 `StructuredMonoWebClient.executeMonoCall(...)` 메서드를 통해 수행되어야 합니다.
- 호출 컨텍스트(MDC, B3 헤더, 시간 측정 등)는 내부적으로 캡슐화되어 있으며, 외부 모듈은 이를 직접 다루지 않아야 합니다.
- 응답 구조(`WebClientCallResult<R>`) 및 메타데이터 포맷은 정책에 의해 고정되며, 임의 변경 없이 정책 확장 방식으로만 확장되어야 합니다.
### 🔧 확장 원칙

- 응답 바디가 새로운 구조를 요구하는 경우, `WebClientCallResult<R>`를 래핑하거나 상속 구조로 확장
- MDC 로그 키는 정책 문서에 정의된 범위를 침범하지 않는 한도 내에서 추가 가능
- retry, timeout, fallback 등의 고급 네트워크 정책은 **사용처에서 책임지고 적용**하며, StructuredWebClient 내부에 포함되어선 안 됨
- 전송 계층 확장(ex: Kafka, gRPC 등)은 동일한 철학(MDC 전파, 로깅, 구조화된 응답)을 따르되, 별도의 전용 컴포넌트로 구현
- 새로운 WebClient 변형(ex: `StructuredFluxWebClient`) 도입 시, 기존 MDC/로깅 정책과의 호환성을 최우선으로 고려해야 하며, 필요 시 독립적 구성으로 분리
> `WebClient.Builder`에 `ExchangeFilterFunction`을 추가해 OAuth2 인증 토큰 자동 삽입 가능
> `WebClientCallResult<R>`를 상속하여 내부 응답 구조를 감싸는 래퍼 클래스 설계 가능

---
---
## 📘 StructuredMonoWebClient: 호출 처리 및 로깅 전략

> 이 문서는 `common-component/webclient` 영역에 구현된 `StructuredMonoWebClient`의 역할과 사용 방법을 설명합니다.  
> WebClient 기반 외부 HTTP 호출 시 B3 헤더 전파, MDC 로깅, 소요 시간 측정 등을 **표준화**하기 위한 컴포넌트입니다.

---

### 1️⃣ 주요 클래스 소개

#### 🔹 `StructuredMonoWebClient`

- **목적**: WebClient 호출에 대한 일관된 로깅, 트레이싱, 예외 처리를 제공하는 공통 래퍼 클래스
- **주요 기능**:
	- 현재 MDC에 설정된 `traceId`, `spanId` 등을 기반으로 **B3 트레이싱 헤더 자동 삽입**
	- 요청-응답의 소요 시간 측정 및 **MDC 전파가 불가능한 경계에서도 표준화된 로그 출력**
	- 응답 헤더에 포함된 B3 traceId는 `call.remoteTraceId`에 기록되어 상대 시스템과의 연계 정보를 남깁니다.
	- 요청/응답 과정에서 수집한 주요 메타데이터(`call.method`, `call.uri`, `call.statusCode`, `call.elapsedMs` 등)를 MDC에 담아 **정책 기반 구조화 로그 출력**
	- 정상 응답 시 결과를 `WebClientCallResult<R>`로 감싸 **`Mono`로 반환**
	- 예외 발생 시 `Mono.error()`로 전달하되, **로깅은 호출자(사용처)에서 수행**

> 🔖 **관련 정책은 [`http.* 필드 정책 문서`](../../../../contracts/logging/logstructure-contract.md)에서 정의됩니다.**

---

### 2️⃣ 응답 포맷: `WebClientCallResult<R>`

- 호출 결과를 감싸는 표준 컨테이너 객체
- 주요 필드:
	- `HttpStatusCode status`: HTTP 상태 코드
	-  `HttpHeaders headers`: 응답 헤더
	- `T data`: 실제 응답 바디
	- `long elapsedMs`: 소요 시간(ms)

> 💡 일반 개발자는 `.map(result -> WebClientCallResult.getData(result))` 를 통해 응답 데이터를 추출할 수 있으며, 필요에 따라 `headers`, `status`, `elapsedMs` 등 메타데이터도 자유롭게 활용 가능합니다.

---

### 3️⃣ 요청 처리 흐름

#### 🔧 메서드: `executeMonoCall(...)`

```java
public <T, R> Mono<WebClientCallResult<R>> executeMonoCall(HttpMethod method, String uri, T data, Class<R> respType)

public <T, R> Mono<WebClientCallResult<R>> executeMonoCall(HttpMethod method, String uri, T data, ParameterizedTypeReference<R> respType)
```

#### ▶️ 동작 순서:

1. **MDC Context Snapshot**
    - `traceId`, `spanId`, `parentSpanId`가 누락되면 예외 발생
2. **요청 시작**
    - `WebClient` 구성 및 B3 헤더 삽입
	 - `traceId`, `spanId`, `parentSpanId` 포함
3. **시간 측정 시작**
    - `System.nanoTime()` 기반 측정 시작 (ms 기준으로 기록)
4. **응답 처리**
	- MDC snapshot을 복원하여 로그 추적 일관성 유지
	- 상태 코드, 메시지, remoteTraceId, elapsedMs 등을 MDC에 기록
	- `StructuredLogger`로 구조화 로그 출력
	- 응답 본문을 `WebClientCallResult<R>`로 래핑하여 반환

---

### 4️⃣ 자동 헤더 전파

| MDC Key        | 전송 헤더 (B3)                |
| -------------- | ------------------------- |
| `traceId`      | `X-B3-TraceId`            |
| `spanId`       | `X-B3-SpanId`             |
| `parentSpanId` | `X-B3-ParentSpanId` (opt) |
→ 개발자는 MDC 설정만 하면, 헤더는 자동 삽입됨  
→ `sampled`, `flags`는 필요 시 MDC에 수동 추가

---

### 5️⃣ 사용 예시 (개발자 코드 측)

``` java 
// 외부 API 호출, 결과만 추출하고 에러 시 fallback 응답 처리
client.executeMonoCall(HttpMethod.GET, "/external-api", null, String.class)
		.map(WebClientCallResult::getData)
		.onErrorResume(e -> {
			logger.warn("Fallback executed", e); // 에러 로깅
			return Mono.just("fallback");        // 대체 응답
		});
```

---

### 6️⃣ 확장/구성 전략 (공통 개발자용)

- `StructuredMonoWebClient(WebClient.Builder)` 생성자를 통해 **기본 WebClient 빌더 주입 가능**
    - 인증 헤더, 공통 필터 등은 외부에서 **builder 커스터마이징 후 주입** 가능
    - e.g. OAuth2 토큰, X-Request-Header 삽입 필터 등
- **MDC → Header 전파**는 현재 B3 스펙(`X-B3-*`) 기반으로 구성됨
    - 필요 시, `WebClient.Builder`에 등록하는 **custom filter**를 통해 다른 트레이싱 헤더를 **병행하여 추가할 수 있음**
    - 예: AWS X-Ray, W3C Trace Context (`traceparent`) 등을 B3와 **병행하여 추가 전파** 가능

---

### 7️⃣ 책임 분리 및 정책

| 항목                    | 설명                                     |
| --------------------- | -------------------------------------- |
| MDC 생성 및 관리           | 호출 전 인터셉터에서 처리 (traceId 등)             |
| 헤더 삽입 및 시간 측정         | `StructuredMonoWebClient`가 담당          |
| 응답 후 에러 대응 및 fallback | 사용처에서 처리 (Mono 기반)                     |
| 로그 기록                 | 성공 시 내부 구조화 로그 자동 기록, 예외 시 사용처에서 로깅 수행 |

---

### ✅ 정리

- `StructuredMonoWebClient`는 **요청 전반의 표준 정책 처리**, `WebClientCallResult`는 **응답 포맷의 일관성 제공**
- 예외 상황은 가공 없이 넘기며, 로깅은 개발자가 명시적으로 수행해야 함
- 공통 컴포넌트 수준에서는 **확장 가능성, 분리된 책임, 최소한의 제약**을 지향함

### ⚠️ Flux 기반 호출에 대한 처리

현재 `StructuredMonoWebClient`는 **Mono 기반의 단일 응답 처리**에 한정됩니다.  
`Flux<Response>` 와 같이 **스트리밍 응답이나 다중 결과를 처리하려면**, 다음과 같은 고려가 필요합니다:

- `WebClientCallResult<Flux<R>>`와 같은 구조는 **설계 목적(단일 호출의 로그 및 추적 측정)**과 어긋날 수 있음
- 응답 스트림 전체를 로깅하는 대신, **스트림 단위 혹은 전체 결과 수집 후 처리** 등의 정책이 필요함
- **별도의 StructuredFluxWebClient 또는 대응 유틸리티 구현**이 필요하며, 기존 MDC 및 로그 측정 정책과의 일관성 유지가 중요함

> ✅ 따라서 다중 응답(Flux) 기반의 외부 호출이 필요한 경우,  
> **동일한 철학과 책임 경계를 지키는 별도 구조로 확장**해야 합니다.

---
---
## 📦 WebClientCallResult: 응답 포맷 설계 및 처리 방식

> 이 문서는 `StructuredMonoWebClient`가 반환하는 응답 포맷인 `WebClientCallResult<R>`에 대해 설명하며, 공통 개발자가 관련 기능을 확장하거나 내부 구조를 이해할 수 있도록 돕습니다.

---

### 1️⃣ `WebClientCallResult<R>` 구조

```java
public final class WebClientCallResult<R> {
    private final HttpStatusCode httpStatus;
    private final HttpHeaders headers;
    private final R data;
    private final long elapsedMs;
}
```

#### ✅ 필드 설명
| 필드명          | 타입               | 설명                             |
| ------------ | ---------------- | ------------------------------ |
| `httpStatus` | `HttpStatusCode` | HTTP 응답 상태 코드 (ex: 200, 500 등) |
| `headers`    | `HttpHeaders`    | 전체 응답 헤더 (traceId 등 확인용)       |
| `data`       | `R`              | 실제 body 내용. 제네릭 타입으로 사용자가 지정   |
| `elapsedMs`  | `long`           | 호출부터 응답까지의 소요 시간 (ms)          |
> 해당 객체는 `exchangeToMono(...)` 내부에서 구성되며, 로그 처리 후 응답 데이터를 감싸서 반환하는 역할을 합니다.

---

### 2️⃣ 생성 방식

```java
public static <R> WebClientCallResult<R> processedWebClientCallResult(
    HttpStatusCode status, HttpHeaders headers, T data, long elapsedMs) {
    return new WebClientCallResult<>(status, headers, data, elapsedMs);
}
```

- 공통 응답 포맷은 `StructuredMonoWebClient` 내부에서 자동 생성되며,
- `exchangeToMono()`에서 로그 기록 직후 `.map()`으로 감싸져 반환됩니다.

---

### 3️⃣ 확장/변경 시 고려사항

| 고려 요소         | 설명                                                                |
| ------------- | ----------------------------------------------------------------- |
| 🔍 커스터마이징     | `data` 필드를 활용해 JSON body 외의 구조체 사용 가능 (ex: wrapper DTO)           |
| ❌ 예외 정보 포함 불가 | 해당 객체는 `Mono.error()`를 사용할 경우 bypass 되므로, 예외 발생 시에는 이 객체가 반환되지 않음 |

---

### 4️⃣ 유의사항

- `WebClientCallResult<R>`는 **정상 응답(2xx/4xx/5xx)** 에 대해서만 생성되며, 예외 발생 시에는 `onErrorResume` 등을 통해 처리해야 합니다.
- 비정상 응답(ex: timeout, 연결 실패 등)은 이 객체를 반환하지 않음
- 호출자는 `.map(resp -> WebClientCallResult.getData(resp))` 식으로 결과를 추출하거나, 필요 시 전체 객체를 통해 로그 확장, 응답 메타 정보 분석 등에 사용할 수 있습니다.

---

### 🔚 요약

- `WebClientCallResult`는 **구조화 응답과 추적 정보의 연계를 위한 핵심 래퍼 객체**입니다.
- 공통 개발자는 해당 객체의 확장 또는 활용 방법에 익숙해야 하며,  
    필요 시 로깅 처리, 응답 헤더 검사, 타임 측정 등 기능을 커스터마이징할 수 있습니다.