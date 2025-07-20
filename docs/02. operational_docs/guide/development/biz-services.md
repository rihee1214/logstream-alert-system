# 📦 Biz 모듈 개발 가이드

## 📌 개요

Biz 모듈은 독립적인 비즈니스 기능을 담당하며,  
공통 모듈(`common`)에 정의된 규칙에 따라 **구조화된 로그**(structured log)를 생성합니다.
**구조화된 로그와 B3 기반 추적을 필수적으로 요구하는 환경에서의 개발 규칙을 설명합니다.**

> 이 문서는 Biz 서비스 개발자들이 로그 정책과 추적 연동 규칙을 정확히 이해하고 준수할 수 있도록 돕기 위한 가이드입니다.

---
## 🔧 개발 시 유의사항

- 로그는 반드시 `StructuredLoggerFactory`를 통해 생성된 로거를 사용해야 합니다.
- MDC 전파를 보장하기 위해 **사용자 정의 인터셉터의 `order`는 반드시 0보다 커야 합니다.**
- **❗모든 HTTP 요청에는 반드시 [B3Header](https://github.com/openzipkin/b3-propagation#multiple-headers)를 포함해야 합니다.**
	- 이는 로그 추적(Trace)를 위한 필수 요건이며, 미설정 시 로그 연계 및 추적 기능이 **정상 동작하지 않습니다.**
	- MDC, B3헤더 전파의 문제점을 해결하기 위한 WebClient 모듈 사용법
		- 참고: [structured-webclient-usage.md](structured-webclient-usage.md)

> 요약된 표는 직접 WebClient를 사용할 경우 반드시 확인하고 반영해야 할 핵심 항목입니다.

| 항목                    | 대응 방법                                                       |
| --------------------- | ----------------------------------------------------------- |
| B3 헤더 전파              | B3 헤더 세팅코드 명시적으로 추가                                         |
| traceId 기반 로깅         | MDC 수동 설정 or ContextSnapshot 사용                             |
| 응답 후 remoteTraceId 확인 | `response.headers()` → `MDC.put("call.remoteTraceId", ...)` |
| 로그 유실 방지              | `"try-finally"` 또는 `"Mono.finallyDo"`에서 `MDC.clear()` 수행    |

---
## ⚠️ 설계 주의사항

- `StructuredLoggerFactory`는 공통 모듈에서 제공하는 구조화 로깅 도구로,  
    반드시 **비즈니스 서비스에서만 사용해야 하는 것은 아닙니다.**
    - 다만, **본 프로젝트에서 주로 적용되는 영역은 biz-service이므로**,  
        로그 작성 시에는 반드시 **정의된 로그 레벨 정책**을 준수해야 합니다.
    - 참조: [log-level-semantics.md](log-level-semantics.md)
    - 예: `error` 레벨은 알림 전송 대상으로 간주되므로 **신중하게 사용**해야 합니다.
- `logback-spring.xml`은 모든 biz-service에서 공유되며,  
    **로그 저장 및 필터링 로직은 logging-service에서 처리됩니다.**
    - biz-service는 로그를 **출력만** 하며, **수집은 Filebeat 등의 수집 도구를 통해 별도로 수행**됩니다.
    - 실제 저장 여부와 방식은 logging-service의 설정에 따라 결정됩니다.
    - 로그는 **biz, act, sys 등 로그 타입별로 분리 저장**되므로,  
        **다른 로그 타입이 biz 로그의 추적이나 검색에 영향을 주지 않습니다.**
    - 이로 인해 **로그 출력이 서비스 성능에 직접적인 영향을 주지 않으며**,  
		추적 정확성도 **서비스별로 분리되어 안전하게 보장**됩니다.

### ⚠️ 비동기 실행 환경에서 MDC 전파 주의

Spring WebClient, `@Async`, Java `Future` 등 **비동기적으로 실행되는 코드에서는**  
**기본적으로 MDC(Context 정보)**가 현재 스레드와 함께 전파되지 않습니다.  
이로 인해 structured log에 `traceId`, `spanId` 등의 필드가 누락되어, 로그 추적이 불가능해질 수 있습니다.
- 참고: [HTTP-Call-Metadata.md](HTTP-Call-Metadata.md)
#### ✅ 자동 전파가 적용되는 경우

- 본 프로젝트에서는 `StructuredMonoWebClient`를 통해 **요청 시점의 MDC 전파 및 B3 헤더 전송을 자동 처리**합니다.
- 따라서 **WebClient 사용 시 응답을 Mono 형태로 받고, 그 내부에서 별도 로깅을 하지 않는다면**, MDC 전파를 수동 처리할 필요가 없습니다.

#### ⚠️ 전파를 고려해야 하는 상황

다음과 같은 경우에는 MDC 전파가 자동으로 이루어지지 않으므로,  
**개발자가 명시적으로 전파를 처리해야 합니다:**

- **WebClient(Mono 기반 포함)** 응답 처리 중 로깅을 수행하는 경우  
  (예: `.flatMap()`, `.map()`, `.subscribe()` 내부에서 log 출력)
- `@Async` 메서드 내부 또는 `Future` 콜백에서 로그를 출력하는 경우
- `ExecutorService`, `ThreadPoolExecutor` 등에서 직접 스레드를 실행하고 로그를 출력하는 경우
- `RestTemplate`, `OkHttp`, `HttpClient` 등을 사용하는 동기/비동기 요청에서 로그를 출력하는 경우

#### 🛠 전파 처리 도구

- Spring 비동기 환경에서는 `MdcTaskDecorator` 또는 `DelegatingSecurityContextAsyncTaskExecutor`
- Reactor 기반(Mono, Flux) 환경에서는 `Mono.deferContextual()` + `ContextSnapshot` 조합
- `MDC.getCopyOfContextMap()`을 통해 현재 MDC를 복사한 후, 
  비동기 스레드 내부에서 `MDC.setContextMap()`으로 전파하는 방식도 유효합니다. 
  특히 `Executor`, `Thread`를 직접 사용하는 경우 효과적이며,  
  **작업 종료 후에는 반드시 `MDC.clear()`로 정리**해 주어야 합니다.

> **주의:**  
> WebClient 사용 시에도 Mono 내부는 별도 스레드에서 실행되므로,  
> **응답 처리 중 로깅은 항상 MDC 전파 여부를 확인해야 합니다.**

### ▶️ StructuredMonoWebClient를 사용하지 않을 경우의 주의사항

`StructuredMonoWebClient`는 B3 헤더(`traceId`, `spanId`, `parentSpanId`) 전파 및 응답 로깅 시 MDC 전파를 자동 처리하도록 설계되어 있습니다.  
그러나 해당 컴포넌트를 사용하지 않고 **직접 WebClient, RestTemplate, HttpClient 등을 구성하여 사용하는 경우**, 다음 사항을 반드시 고려해야 합니다:
- 참고: [HTTP-Call-Metadata.md](HTTP-Call-Metadata.md)

#### ✅ B3 헤더 수동 전파
- 외부 시스템으로의 호출에는 반드시 B3 헤더(`X-B3-TraceId`, `X-B3-SpanId`, `X-B3-ParentSpanId`)를 **명시적으로 포함**시켜야 합니다. (`X-B3-ParentSpanId`는 필수 값이 아님)
- 이는 Zipkin 기반 트레이싱 뿐만 아니라, **로그 연관 추적**을 위해서도 필수적입니다.

```java
String traceId = MDC.get("traceId");
String spanId = MDC.get("spanId");

webClient.get()
    .uri("http://external-service/api")
    .header("X-B3-TraceId", traceId)
    .header("X-B3-SpanId", spanId)
    .retrieve()
    .bodyToMono(String.class);
```

#### ✅ 응답 후 로깅 시 MDC 및 Call 정보 주의

- WebClient 또는 기타 비동기 HTTP 클라이언트를 사용할 경우, **응답 이후 다음 작업이 반드시 필요**합니다:
	1. 응답 헤더에서 `X-B3-TraceId` 값을 추출
	2. `call.remoteTraceId` MDC 필드에 명시적으로 설정
	3. structured log를 출력
- 이는 호출 대상 시스템이 traceId를 정상적으로 수신했는지 판단하는 **중요한 분산 추적 기준**입니다.

> 📌 응답 헤더에 TraceId가 존재하지 않는 경우:
> 1.  외부 시스템이거나, 대상 시스템이 정책을 준수하지 않은 것입니다.
> 2.  이 경우 `__UNKNOWN__` 값을 `call.remoteTraceId`에 세팅한 후 로그를 출력합니다.

```java
Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap(); // 현재 MDC 스냅샷 캡처
client.get()
    .uri("http://external-service/api")
    .exchangeToMono(response -> {
	    MDC.setContextMap(mdcSnapshot);
	    // 다양한 call MDC 추가
        MDC.put(RESP_TRACE_ID.getKey(),  
				Objects.toString(resp.headers()  
                    .header(B3Header.TRACE_ID.getHeaderName()).getFirst(),  
                            DefaultValues.UNKNOWN.getValue())); // "__UNKNOWN__"
        log.info(BIZ, "응답 수신 및 trace 확인");
        return response.bodyToMono(String.class);
    });
```

> ✅ WebClient 사용 시에는 응답 후 traceId를 수동 추출하여 로그에 명시해야 하며, **StructuredMonoWebClient를 사용하면 이러한 후처리가 자동으로 수행됩니다.**

> 더 자세한 StructuredMonoWebClient의 내부 동작 및 정책은 [structured-webclient.md](structured-webclient.md) 문서를 참조하세요.
> 해당 문서에는 B3 헤더 구성, MDC 캡터 방식, 로그 자동화 처리 흐름이 포함되어 있습니다.

---
## 🧪 테스트

- 단위 테스트 외에, 구조화된 로그가 정확히 출력되는지 확인하십시오.
- `traceId`, `spanId` 등의 필드가 누락되지 않았는지, MDC 전파가 정상적으로 이루어졌는지 확인이 필요합니다.

---
## 📎 관련 문서

- [structured-webclient.md](structured-webclient.md): WebClient에서 B3 헤더 자동 처리 및 MDC 캡처 흐름
- [log-level-semantics.md](log-level-semantics.md): Log Level에 따른 알림 정책
- [HTTP-Call-Metadata.md](HTTP-Call-Metadata.md): HTTP 호출 시 MDC 설정 및 응답 메타데이터 처리 규칙