# 📄 HTTP Call Metadata Contract

> 이 문서는 MSA 환경에서 HTTP 요청 시 전파되어야 할 메타데이터와 관련된 계약을 정의합니다.  
> 주 목적은 추적성(traceability)을 확보하고, 로그 구조화 및 외부 요청 간 연관성을 유지하는 것입니다.

---

## 1️⃣ 필수 전파 헤더: B3 트레이싱 헤더

HTTP 요청 시 다음의 B3 표준 헤더를 **필수적으로 포함**해야 합니다.  
이들은 내부적으로 MDC에서 설정된 값으로부터 자동 전파됩니다.

| MDC Key        | HTTP Header         | 설명              |
| -------------- | ------------------- | --------------- |
| `traceId`      | `X-B3-TraceId`      | 전체 요청 흐름 식별자    |
| `spanId`       | `X-B3-SpanId`       | 현재 작업 단위의 식별자   |
| `parentSpanId` | `X-B3-ParentSpanId` | 상위 스팬 식별자 (선택적) |

> `sampled`, `flags`와 같은 추가 B3 헤더는 **현재 시스템에서 공식 지원되지는 않으며**,  
> 향후 확장을 고려해 선택적으로 포함 가능합니다.  
> 필요 시 별도 Interceptor 또는 Filter를 통해 동적으로 추가하도록 설계합니다.

---

## 2️⃣ 전파 메커니즘

Spring 기반 시스템에서는 MDC에 설정된 값을 기반으로 B3 헤더가 자동 전파됩니다.  
예를 들어 `MDC.put("traceId", "abc123")  →  X-B3-TraceId: abc123`
이는 `WebClient`, `RestTemplate`, `FeignClient` 등 HTTP 클라이언트 구현체에서 공통적으로 적용되어야 하는 패턴이며, Custom WebClient 래퍼나 Filter 등에서 자동으로 헤더에 매핑하여 전파되도록 구현되어야 합니다.

>💡 `sampled`, `flags` 필드는 필요 시에만 MDC에 포함시키며,  
>실제 사용 여부는 모니터링 정책이나 Debug 모드에 따라 결정합니다.

---

## 3️⃣ 내부 전파 정책 (Internal Propagation Policy)

MSA 환경에서의 HTTP 호출 시, B3 헤더(`traceId`, `spanId`, `parentSpanId`)는 **모든 내부 시스템 간 호출에 대해 자동 전파**되어야 합니다. 
이 메커니즘은 WebClient, RestTemplate, FeignClient 등에 공통 적용되어야 하며, MDC에 설정된 값을 기반으로 헤더가 자동 세팅되어야 합니다.

### 📌 내부 시스템 간 호출 (예: Gateway ↔ Biz Service ↔ Adapter)

| 경로                        | B3 헤더 사용 | 설명                                                                            |
| ------------------------- | -------- | ----------------------------------------------------------------------------- |
| Gateway → Biz Service     | ✅ 전파됨    | 요청 수신 시 MDC에 설정된 값 기준                                                         |
| Biz Service → Biz Service | ✅ 전파됨    | 내부적으로 유지된 traceId 기반으로 전달, 응답 traceId는 구조화 로그에 기록되지만, 시스템 내 추적 컨텍스트에는 반영되지 않음 |
| Biz Service → 외부 시스템      | ✅ 전파됨    | 내부 traceId 기반으로 외부로 전달되며, 응답 traceId는 구조화 로그에 기록되지만, 시스템 내 추적 컨텍스트에는 반영되지 않음  |

> ☑️ L7 *Gateway도 Biz Service와 동일하게 내부 시스템으로 간주하며, MDC 기반 B3 헤더를 전파해야 합니다.*

### 🌐 외부 시스템과의 경계 처리

외부 API 호출 시에도 동일한 B3 헤더를 **요청 시점에 삽입**하지만, **응답 시점의 B3 헤더는 시스템 내부 MDC에 반영하지 않습니다.**

- `X-B3-TraceId` 응답 헤더가 포함되어 있을 경우:
	- 내부 traceId는 기존 값 그대로 유지
	- 응답 traceId는 구조화 로그의 `call.remoteTraceId` 필드에 별도로 기록
		- 이 필드는 [logstructure contract](logstructure-contract.md) 문서에서 정의됩니다.
- 외부 시스템으로부터 받은 traceId를 기존의 traceId와 교체하는 일은 없음

> ✅ 이는 **외부 시스템과의 경계를 명확히 유지하고**, 내부 트레이싱 체계의 **일관성과 안전성**을 확보하기 위한 정책입니다.

---

## 4️⃣ 예시

### ✅ 정상 요청 예시 (헤더)

```http
GET /api/resource HTTP/1.1
X-B3-TraceId: 123abc
X-B3-SpanId: 456def
X-B3-ParentSpanId: 111aaa
X-B3-Sampled: 1
X-B3-Flags: 0
```

### 🪵 로그 MDC 구조 예시

```json
{
  "traceId": "123abc",
  "spanId": "456def",
  "parentSpanId": "111aaa",
  "sampled": "1",
  "flags": "0"
}
```

---

## 📝 정책 요약

- 모든 내부 호출은 MDC 기반의 B3 헤더를 전파해야 함
- 외부 호출 시에도 동일한 B3 헤더가 요청에 포함되지만, 응답의 traceId는 **로깅 전용**
- 내부 traceId는 요청 시 설정된 값을 절대 변경하지 않음
- `call.remoteTraceId`는 응답 B3 traceId를 단순 기록하기 위한 필드이며, 추적을 위한 보조 수단
