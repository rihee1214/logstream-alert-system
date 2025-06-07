# # 📘 서비스 통합 설정 설명서

> 해당 문서는 Spring Boot 기반 MSA 환경에서 공통 적용되는 설정 값들의 정의, 우선순위, 적용 방식 등을 정리한 문서입니다.

## ⚠️ 설정 반영 방식 및 주의사항

- 설정 우선순위는 다음과 같습니다:
	1. Java 옵션 (`-Dkey=value`)
	2. 환경 변수 (`ENV_VAR`)
	3. application.properties
- 대부분의 설정 값은 **기동 시점에만 반영**되므로, 변경 시 **애플리케이션 재기동이 필수**입니다.
- ISO-8601 Duration 값은 문자열(`PT3S`)로 주입되며, 숫자만 입력하거나 포맷이 틀리면 예외가 발생할 수 있습니다.
- 필수 설정이 누락되거나 부정확하게 주입되면 **self-monitoring 기능이 동작하지 않거나 애플리케이션 기동에 실패할 수 있습니다.**

---

## 1. 🔧 Java 옵션 (`-Dkey=value`)

### 🔧 `service.name`
- **타입:** `String`
- **필수 여부:** ✅
- **기본 값:** 없음
- **설명:** 로그 MDC 및 trace ID 구성에 사용되는 서비스 식별자입니다. structured log 및 self-monitoring 시스템과 연계됩니다.
- **예시:** `-Dservice.name=mockup` 또는 application.properties (우선순위는 JVM 옵션이 더 높음)

### 🔧 `server.port`
- **타입:** `String`
- **필수 여부:** ❌
- **기본 값:** `8080` (내부 fallback)
- **설명:** 서비스가 바인딩될 포트이며, self-monitoring actuator 호출 시 base URL을 구성하는 데 사용됩니다.
- **예시:** `-Dserver.port=8080`


---

## 2. 🌎 환경 변수

### 🔧 `HOST`
- **타입:** `String`
- **기본 값:** 없음
- **필수 여부:** ✅
- **설명:** 로그에 삽입될 시스템의 호스트명입니다. 설정되지 않으면 애플리케이션이 기동에 실패합니다.
- **예시:** `HOST=mockup-host`

### 🔧 `CONTAINER`
- **타입:** `String`
- **기본 값:** 없음
- **필수 여부:** ✅
- **설명:** 로그에 삽입될 컨테이너 이름입니다. 설정되지 않으면 애플리케이션이 기동되지 않습니다.
- **예시:** `CONTAINER=mockup-container`

### 🔧 `MONITORING_TOKEN`
- **타입:** `String`
- **기본 값:** 없음
- **필수 여부:** ✅
- **설명:** Prometheus가 actuator 엔드포인트를 호출할 때 사용하는 인증 토큰. `monitoring.token`과 동일한 용도이며 환경에 따라 선택.
- **예시:** `MONITORING_TOKEN=test-token`

---

## 3. 🏡 application.properties

### ⚙️ Common Setting

#### 🔧 `spring.application.name`
- **타입:** `String`
- **기본값:** 없음
- **필수 여부:** ❌
- **설명:** Spring 애플리케이션 이름. 메트릭 및 actuator 엔드포인트에서 사용됨
- **추천 위치:** 모든 모듈

#### 🔧 `logging.interceptor.enabled`
- **타입:** `boolean`
- **기본값:** `false`
- **필수 여부:** ✅
- **설명:** MDC 기반 traceId, spanId 자동 설정 활성화 여부
- ⚠️ **주의:** 해당 값을 `false`로 설정하는 경우, 로그에 traceId 계열 필드가 누락되며, 이는 표준 구조 기반 로깅 정책을 위반하게 됩니다.  
  **이로 인해 발생하는 로그 추적 누락, 연계 시스템 간의 요청 흐름 상실 등에 대해서는 시스템이 책임지지 않습니다.**

---

### 📡 Actuator Setting

#### 🔧 `management.endpoints.web.exposure.include`
- **타입:** `String`
- **필수 여부:** ✅
- **기본값:** 없음
- **설명:** 노출할 actuator endpoint 목록을 지정합니다.
- 🔸**예시**: `metric,health,prometheus`

#### 🔧 `management.endpoints.web.base-path`
- **타입:** `String`
- **필수 여부:** ❌
- **기본값:** `/actuator`
- **설명:** actuator base 경로를 지정합니다. 기본 경로는 `/actuator`입니다. 변경 시 프록시 라우팅/방화벽 설정 등에 주의가 필요합니다.
- 🔸**예시**: `/actuator`, `/infra`

#### 🔧 `management.endpoint.health.show-details`
- **타입:** `String`
- **기본값:** `never`
- **필수 여부:** ⚠️ 운영환경에 따라 선택
- **설명:** health 응답에 세부 상태 정보를 포함할지 여부입니다.
	- `never`: 응답에 세부 정보 없음
	- `when-authorized`: 인증된 요청에만 세부 정보 노출
	- `always`: 모두에게 노출
- **권장:** Prometheus, 외부 모니터링 연동 시 `always` 또는 `when-authorized` 설정

#### 🔧 `management.metrics.export.prometheus.enabled`
- **타입:** `boolean`
- **기본값:** 없음
- **필수 여부:** ✅
- **설명:** Prometheus exporter 활성화 여부입니다. 활성화하지 않으면 `/actuator/prometheus` 경로가 작동하지 않습니다.
- ⚠️ **주의:** 이 설정이 빠질 경우 시스템 메트릭이 수집되지 않으며,  
  Prometheus 기반 알림, 대시보드, 리소스 사용량 추적 등 모든 메트릭 기능이 무력화됩니다.  
  **운영 환경에서는 반드시 `true`로 설정해야 하며, 설정 누락으로 인한 모니터링 공백에 대해서는 시스템이 책임지지 않습니다.**
- 🔸**예시**: `true`
---

### 🕰 WebClient / Scheduler Setting

#### 🔧 `monitoring.scheduler.enabled`
- **타입:** `boolean`
- **기본값:** `false`
- **필수 여부:** ✅
- **설명:** actuator self-call을 수행하는 전용 스케줄러의 활성화 여부입니다. 이 설정이 활성화되면 주기적으로 `/actuator/health` 등 내부 endpoint를 호출하여 상태를 점검하고 로그로 남깁니다.
- ⚠️ **주의:** 이 값을 `false`로 설정하면 self-monitoring이 동작하지 않게 됩니다.
  운영 환경에서는 이 기능을 활성화하지 않으면 **상태 점검이 불가능해지므로 반드시 `true`로 설정**해야 합니다.
- ✅ **예외:**  
  팀이 `/actuator/health`를 주기적으로 호출하는 외부 시스템을 이미 구축한 경우,  
  해당 스케줄러는 비활성화해도 무방합니다.  
  단, 이 스케줄러는 structured log 기반 상태 추적, timeout 기반 경고 등  
  **단순한 생존 확인을 넘어선 부가 정보 수집**을 위해 동작합니다.  
  따라서 단순한 "애플리케이션이 살아 있는가?" 이상의 정보를 필요로 한다면,  
  이를 직접 구현한 경우에만 비활성화를 고려하시기 바랍니다.
- 🔸**예시:** `true`

#### 🔧 `monitoring.scheduler.interval.ms`
- **타입:** `long`
- **기본값:** `10000`
- **필수 여부:** ✅
- **설명:** actuator self-call의 호출 간격(ms 단위)입니다.  
  이 간격은 `/actuator/health` 등의 상태 체크 endpoint 호출 주기를 조절하며, 너무 짧거나 길게 설정할 경우 부하 혹은 지연된 감지가 발생할 수 있습니다.
- **권장:** `5000` ~ `15000` 사이에서 환경에 맞게 설정할 것
- 🔸**예시:** `10000`

#### 🔧 `monitoring.timeout.connect`
- **타입:** `Duration (ISO-8601 포맷)`
- **기본값:** `PT3S`
- **필수 여부:** ✅
- **설명:** WebClient를 통한 actuator 호출 시 연결 타임아웃입니다.  
  actuator endpoint가 응답하지 않거나 연결이 지연될 경우, 설정된 시간이 초과되면 호출이 실패로 간주됩니다.
- **주의사항:** 반드시 `PT3S`, `PT10S`, `PT1M` 등 ISO-8601 Duration 문자열로 지정해야 하며, 숫자만 입력하거나 포맷을 잘못 지정하면 애플리케이션이 기동 중 예외를 발생시킬 수 있습니다.
- 🔸**예시:** `PT3S`

#### 🔧 `monitoring.timeout.read`
- **타입:** `Duration (ISO-8601 포맷)`
- **기본값:** `PT3S`
- **필수 여부:** ✅
- **설명:** actuator endpoint로부터 응답을 받을 때의 최대 대기 시간입니다.  
  설정 시간 내에 응답이 도착하지 않으면 timeout으로 간주되며, 관련 로그가 남습니다.
- **주의사항:** 반드시 `PT3S`, `PT10S`, `PT1M` 등 ISO-8601 Duration 문자열로 지정해야 하며, 숫자만 입력하거나 포맷을 잘못 지정하면 애플리케이션이 기동 중 예외를 발생시킬 수 있습니다.
- 🔸**예시:** `PT3S`

---

### 🧭 Tracing Setting

#### 🔧 `tracing.traceId.multiplier`
- **타입:** `int`
- **기본값:** `1`
- **필수 여부:** ❌
- **설명:** traceId의 길이 배수 설정입니다. 내부적으로는 32자리(16바이트)를 기본으로 하며, 해당 배수만큼 확장 생성됩니다.  
	- 예시: `2`로 설정 시 traceId는 64자리(32바이트)로 생성됩니다.
- **주의:** 시스템은 모든 길이의 traceId를 지원하지만, 로그 분석 및 시각화 도구와의 호환성에 따라 기본값 유지가 권장됩니다.

#### 🔧 `tracing.spanId.multiplier`
- **타입:** `int`
- **기본값:** `1`
- **필수 여부:** ❌
- **설명:** spanId의 길이 배수 설정입니다. 내부적으로는 16자리(8바이트)를 기본으로 하며, 해당 배수만큼 확장 생성됩니다.  
	 - 예시: `2`로 설정 시 spanId는 32자리(16바이트)로 생성됩니다.
- **권장:** 특별한 요구가 없는 한 기본값 `1`로 유지하는 것이 추적 연동에서 안전합니다.

---