# 📄 StructuredWebClient 사용 가이드

> 💡 이 문서는 StructuredWebClient를 사용하는 모든 서비스 개발자가 **반드시 따라야 하는 표준 사용 지침서**입니다.  
> 본 문서에 안내된 방식 이외의 사용은 **정책 위반으로 간주될 수 있으며**, 예외나 로깅 누락 등의 이슈가 발생할 수 있습니다.

---
## 🧭 대상

- 일반 서비스 개발자
- 공통 정책을 활용하는 통합 모듈 개발자

## 🎯 목적

- HTTP 호출 시 로그와 트레이싱이 자동 전파되도록 하기 위한 공통 모듈 사용 규칙 안내
- `WebClient` 생성을 금지하고 `StructuredWebClient`를 표준 방식으로 사용할 것을 요구

## 📌 기본 규칙

- 서비스 내에서 HTTP 호출이 필요한 경우 `StructuredWebClient.call(...)` 사용
- 응답은 `Mono<WebClientCallResult<T>>` 형태로 반환됨
- 오류 발생 시 `Mono.error(...)`로 전달되므로, 사용자 코드에서 예외 처리 필요

## 📂 관련 문서

- [`structured-webclient.md`](../common/webclient/structured-webclient.md) - 내부 구현 문서

---

## 1️⃣ 주요 구성 클래스

### ▶️ `StructuredMonoWebClient`

- 공통 HTTP 호출 로직을 담당하는 핵심 유틸 클래스입니다.
- 내부적으로 `WebClient`를 wrapping하여 B3 헤더 전파 및 MDC 로그 기록을 수행합니다.

### ▶️ `WebClientCallResult<R>`

- 응답을 감싸는 결과 객체로, 상태 코드, 헤더, 본문, 소요 시간을 포함합니다.

---

## 2️⃣ 기본 사용 예시

```java
String respData =
	client.executeMonoCall(HttpMethod.GET, "/api/resource", null, String.class)
	  .map(WebClientCallResult::getData)
      .onErrorResume(e -> Mono.just("fallback")).block();
```

- `HttpMethod`, `URI`, `요청 바디`, `응답 타입(ParameterizedTypeReference 가능)` 순서로 호출
- 응답은 `WebClientCallResult<R>`로 감싸져 있으며 `.getData()`로 추출

---

## 3️⃣ 요청 처리 흐름 (내부 동작 요약)

### 🔄 동작 순서

1. MDC에서 traceId, spanId 추출 (누락 시 예외 발생)
2. B3 헤더 자동 삽입
3. 요청-응답 시간 측정
4. 응답 수신 시 상태 코드, 메시지, remoteTraceId 등 MDC에 기록
5. `StructuredLogger`를 통해 로그 출력
6. 응답은 `WebClientCallResult<R>`로 래핑되어 전달

---

## 4️⃣ 응답 구조 요약

| 필드명        | 설명                             |
|---------------|----------------------------------|
| `status`      | HTTP 상태 코드 (`HttpStatusCode`) |
| `headers`     | 응답 헤더 (`HttpHeaders`)         |
| `data`        | 실제 응답 데이터 (`R`)            |
| `elapsedMs`   | 응답까지 소요된 시간 (ms)         |

- 실제 데이터를 사용하려면 `.map(resp -> WebClientCallResult.getData(resp))` 필수
- 응답에 대한 로깅은 자동 처리되며, 사용자 개입 불필요

---

## 5️⃣ 예외 처리 방식

- 네트워크 실패, 타임아웃, 5xx 응답 등은 `Mono.error()`로 전파됨
- 다음과 같이 `.onErrorResume()` 등을 통해 fallback 처리 필요

```java
client.executeMonoCall(...)
      .map(WebClientCallResult::getData)
      .onErrorResume(e -> Mono.just("기본값"));
```

---

## 6️⃣ 커스터마이징이 필요한 경우

- `WebClient.Builder`를 커스터마이징하여 인증 헤더, 필터 등을 추가한 후 `StructuredMonoWebClient`에 주입 가능
- **timeout**은 `WebClient.Builder`의 커넥션/응답 타임아웃 설정을 통해 적용해야 함
- **retry** 정책은 기본 제공하지 않으며, 별도의 외부 재시도 로직을 사용하여 직접 구현 필요
	- `Mono`객체에서 제공하는 retry는 응답처리만을 재시도 하는 것일 뿐 요청 전체를 재처리하지 않습니다. `Retry` 객체를 이용하여 외부에서 처리하는 것을 권장드립니다.

---

## 7️⃣ 기타 유의사항

- **절대 WebClient를 직접 생성하여 사용하지 말 것**
- StructuredWebClient 외부에서 traceId, spanId 설정은 금지
- StructuredWebClient는 Mono 기반이며 Flux 사용 시 별도 확장 컴포넌트 필요

---

## ✅ 요약

| 항목       | 설명                                             |
| -------- | ---------------------------------------------- |
| 호출 방식    | `StructuredMonoWebClient.executeMonoCall(...)` |
| 응답 구조    | `WebClientCallResult<R>`                       |
| 예외 처리 방식 | `.onErrorResume(...)`                          |
| 응답 추출 방법 | `.map(WebClientCallResult::getData)`           |
| 확장 방식    | WebClient.Builder 커스터마이징                       |
