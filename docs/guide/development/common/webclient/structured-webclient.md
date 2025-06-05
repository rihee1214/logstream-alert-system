# 📄 StructuredWebClient Internal Design

> 이 문서는 공통 컴포넌트인 `StructuredWebClient`의 내부 동작 구조 및 설계 의도를 설명합니다.

## 🧭 대상
- 이 문서는 공통 컴포넌트를 개발하거나 확장하는 개발자에게 필요한 내용을 다룹니다.
- 일반 비즈니스 서비스 개발자는 [structured-webclient-usage.md](../../biz/structured-webclient-usage) 문서를 참조하십시오.

## 🎯 목적
- WebClient 기반 HTTP 호출의 일관된 로그/트레이싱/에러 처리 체계를 제공
- MDC 기반의 traceId/spanId 전파를 자동화
- 모든 호출 결과는 `Mono` 흐름 안에서 처리된다.
	- 성공 시 `WebClientCallResult<T>`객체가 `Mono`로 감싸져 전달
	- 실패 시 `Mono.error(...)`로 예외가 전달


# 📘 StructuredMonoWebClient 공통 모듈 가이드

> 이 문서는 `common-component/webclient` 영역에 구현된 `StructuredMonoWebClient`의 역할과 사용 방법을 설명합니다.  
> WebClient 기반 외부 HTTP 호출 시 B3 헤더 전파, MDC 로깅, 소요 시간 측정 등을 **표준화**하기 위한 컴포넌트입니다.

---

## 1️⃣ 주요 클래스 소개

### 🔹 `StructuredMonoWebClient<T>`

- **목적**: WebClient 호출에 대한 일관된 로깅, 트레이싱, 예외 처리를 제공하는 공통 래퍼 클래스
- **주요 기능**:
	- 현재 MDC에 설정된 `traceId`, `spanId` 등을 기반으로 **B3 트레이싱 헤더 자동 삽입**
	- 요청-응답의 소요 시간 측정 및 **MDC 전파가 불가능한 경계에서도 표준화된 로그 출력**
	- 응답 헤더에 포함된 B3 traceId(`X-B3-TraceId`)를 수신한 경우, 해당 값을 `call.remoteTraceId`로 MDC에 설정하여 **상대 시스템의 트레이스 식별자 기록**
	- 요청/응답 과정에서 수집한 주요 메타데이터(`call.method`, `call.uri`, `call.statusCode`, `call.elapsedMs` 등)를 MDC에 담아 **정책 기반 구조화 로그 출력**
	- 정상 응답 시 결과를 `WebClientCallResult<T>`로 감싸 **`Mono`로 반환**
	- 예외 발생 시 `Mono.error()`로 전달하되, **로깅은 호출자(사용처)에서 수행**

> 🔖 관련 정책은 [`http.* 필드 정책 문서`](../../../../contracts/logging/logstructure-contract.md)에서 정의됩니다.**

---

## 2️⃣ 응답 포맷: `WebClientCallResult<T>`

- 호출 결과를 감싸는 표준 컨테이너 객체
- 주요 필드:
	- `HttpStatusCode status`: HTTP 상태 코드
	-  `HttpHeaders headers`: 응답 헤더
	- `T data`: 실제 응답 바디
	- `long elapsedMs`: 소요 시간(ms)

> 💡 일반 개발자는 `.map(result -> WebClientCallResult.getData(result))` 를 통해 응답 데이터를 추출할 수 있으며, 필요에 따라 `headers`, `status`, `elapsedMs` 등 메타데이터도 자유롭게 활용 가능합니다.

---

## 3️⃣ 요청 처리 흐름

### 🔧 메서드: `executeMonoCall(...)`

```java
public Mono<WebClientCallResult<T>> executeMonoCall(HttpMethod method, String uri, T data)
```

### ▶️ 동작 순서:

1. **MDC Context Snapshot**
    - `traceId`, `spanId`, `parentSpanId`가 누락되면 예외 발생
2. **요청 시작**
    - `WebClient`로 요청 구성
    - snapshot 기반으로 B3 헤더 삽입
3. **StopWatch 시작**
    - 요청부터 응답까지의 시간 측정
4. **응답 처리**
    - 상태 코드, 상태 메시지, remoteTraceId 등 MDC에 기록
    - `StructuredLogger`를 통해 구조화 로그 출력
    - 응답은 `WebClientCallResult<T>`로 래핑
5. **예외 처리**
    - `onErrorResume`에서 stopwatch 종료 후 `Mono.error(...)` 전달 (로깅 X)

---

## 4️⃣ 자동 헤더 전파

|MDC Key|전송 헤더 (B3)|
|---|---|
|`traceId`|`X-B3-TraceId`|
|`spanId`|`X-B3-SpanId`|
|`parentSpanId`|`X-B3-ParentSpanId` (opt)|
→ 개발자는 MDC 설정만 하면, 헤더는 자동 삽입됨  
→ `sampled`, `flags`는 필요 시 MDC에 수동 추가

---

## 5️⃣ 사용 예시 (개발자 코드 측)

``` java 
client.executeMonoCall(HttpMethod.GET, "/external-api", null)
		.map(WebClientCallResult::getData)
		.onErrorResume(e -> {
			logger.warn("Fallback executed", e);
			return Mono.just("fallback");
		});
```

---

## 6️⃣ 확장/구성 전략 (공통 개발자용)

- WebClient 기본 빌더는 `StructuredMonoWebClient(WebClient.Builder)`를 통해 주입 가능    
    - 인증 헤더, 공통 필터 등은 외부에서 정의 후 주입
- MDC → Header 전파 전략은 현재 B3 기반이지만, 필요 시 커스터마이징 가능

---

## 7️⃣ 책임 분리 및 정책

|항목|설명|
|---|---|
|MDC 생성 및 관리|호출 전 인터셉터에서 처리 (traceId 등)|
|헤더 삽입 및 stopwatch 측정|`StructuredMonoWebClient`가 담당|
|응답 후 에러 대응 및 fallback|사용처에서 처리 (Mono 기반)|
|로그 기록|성공 시 내부 구조화 로그 자동 기록, 예외 시 사용처에서 로깅 수행|

---

## ✅ 정리

- `StructuredMonoWebClient`는 **요청 전/중의 표준 정책 처리**, `WebClientCallResult`는 **응답 포맷의 일관성 제공**
    
- 예외 상황은 가공 없이 넘기며, 로깅은 개발자가 명시적으로 수행해야 함
    
- 공통 컴포넌트 수준에서는 **확장 가능성, 분리된 책임, 최소한의 제약**을 지향함

---
---
# 📦 StructuredMonoWebClient 응답 구조 가이드

> 이 문서는 `StructuredMonoWebClient`가 반환하는 응답 포맷인 `WebClientCallResult<T>`에 대해 설명하며, 공통 개발자가 관련 기능을 확장하거나 내부 구조를 이해할 수 있도록 돕습니다.

---

## 1️⃣ `WebClientCallResult<T>` 구조

```java
public final class WebClientCallResult<T> {
    private final HttpStatusCode httpStatus;
    private final HttpHeaders headers;
    private final T data;
    private final long elapsedMs;
}
```

### ✅ 필드 설명
|필드명|타입|설명|
|---|---|---|
|`httpStatus`|`HttpStatusCode`|HTTP 응답 상태 코드 (ex: 200, 500 등)|
|`headers`|`HttpHeaders`|전체 응답 헤더 (traceId 등 확인용)|
|`data`|`T`|실제 body 내용. 제네릭 타입으로 사용자가 지정|
|`elapsedMs`|`long`|호출부터 응답까지의 소요 시간 (ms)|
> 해당 객체는 `exchangeToMono(...)` 내부에서 구성되며, 로그 처리 후 응답 데이터를 감싸서 반환하는 역할을 합니다.

---

## 2️⃣ 생성 방식

```java
public static <T> WebClientCallResult<T> processedWebClientCallResult(
    HttpStatusCode status, HttpHeaders headers, T data, long elapsedMs) {
    return new WebClientCallResult<>(status, headers, data, elapsedMs);
}
```

- 공통 응답 포맷은 `StructuredMonoWebClient` 내부에서 자동 생성되며,
- `exchangeToMono()`에서 로그 기록 직후 `.map()`으로 감싸져 반환됩니다.

---

## 3️⃣ 확장/변경 시 고려사항

| 고려 요소         | 설명                                                                 |
| ------------- | ------------------------------------------------------------------ |
| ✅ 추적 로그 연동    | 응답 헤더에서 `X-B3-TraceId`를 추출해 `call.remoteTraceId`로 기록할 수 있도록 MDC 처리 |
| 🔍 커스터마이징     | `data` 필드를 활용해 JSON body 외의 구조체 사용 가능 (ex: wrapper DTO)            |
| ❌ 예외 정보 포함 불가 | 해당 객체는 `Mono.error()`를 사용할 경우 bypass 되므로, 예외 발생 시에는 이 객체가 반환되지 않음  |

---

## 4️⃣ 유의사항

- `WebClientCallResult<T>`는 **정상 응답(2xx/4xx/5xx)** 에 대해서만 생성되며, 예외 발생 시에는 `onErrorResume` 등을 통해 처리해야 합니다.
- 비정상 응답(ex: timeout, 연결 실패 등)은 이 객체를 반환하지 않음
- 호출자는 `.map(WebClientCallResult::getData)` 식으로 결과를 추출하거나, 필요 시 전체 객체를 통해 로그 확장, 응답 메타 정보 분석 등에 사용할 수 있습니다.

---

## 🔚 요약

- `WebClientCallResult`는 **구조화 응답과 추적 정보의 연계를 위한 핵심 래퍼 객체**입니다.
- 공통 개발자는 해당 객체의 확장 또는 활용 방법에 익숙해야 하며,  
    필요 시 로깅 처리, 응답 헤더 검사, 타임 측정 등 기능을 커스터마이징할 수 있습니다.