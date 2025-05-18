# 🩺 Actuator Logging 및 보안 설정 가이드
> 이 문서는 공통 모듈에서 제공하는 Actuator 관련 기능 —  
> 1) Health 상태 주기적 로깅 기능과  
> 2) `/actuator/**` 경로에 대한 보안 설정 — 에 대해 설명합니다.

---

## ✅ 1. Actuator 상태 로깅 (Health Log Scheduler)

`ActuatorHealthMonitoringScheduler`는 애플리케이션 내부의 `/actuator/health` endpoint를  
**주기적으로 호출**하고, **structured log 형태로 상태를 기록**합니다.

### 🔧 동작 방식

- WebClient를 이용하여 `/actuator/health` endpoint를 call
- 응답 메타 정보(HTTP status, URI, 요청 소요 시간 등)를 로그에 포함
- JSON 기반 structured log로 출력됨
- MDC 기반으로 `logtype=act`, `level=INFO`로 분류됨

### 📁 설정 방법

- 호출 간격은 `monitoring.scheduler.interval.ms` 속성으로 지정
- application.properties 예시:

```properties
monitoring.scheduler.interval.ms=60000
```
- 로그 예시:
```json
{
  "logtype": "act",
  "level": "INFO",
  "meta": {
    "statusCode": 200,
    "statusMessage": "OK",
    "method": "GET",
    "uri": "/actuator/health",
    "elapsedMs": 82
  },
  "message": "{\"status\":\"UP\",\"components\":{\"db\":{\"status\":\"UP\"}}}"
}
```
### 📎 참고

- 로그는 `StructuredLogger`를 통해 출력되며, `logback-spring.xml` 설정에 따라 수집됩니다.
- logtype이 `act`인 로그는 보통 `logging-service`에서 수집하여 분석되거나, act-log 인덱스로 분류됩니다.

---

## ✅ 2. Actuator 보안 설정 (ActuatorSecurityConfig)

공통 보안 설정은 `/actuator/**` 경로에 대해 다음과 같은 제어를 수행합니다.

### 🔐 보안 정책

|경로|접근 조건|
|---|---|
|`/actuator/prometheus`|인증 토큰 헤더(`X-Monitoring-Token`)가 일치해야 함|
|그 외 actuator 경로 (`/health`, `/metrics` 등)|**localhost에서만 접근 허용**|

### 🔧 Token 설정

- 인증 토큰 값은 `monitoring.token` 속성 또는 환경변수로 설정합니다.

```properties
monitoring.token=MyPrometheusSecret
```

- Prometheus는 다음과 같은 HTTP 헤더를 포함해 요청을 보내야 합니다:
```properties
X-Monitoring-Token: MyPrometheusSecret
```

### 🛡 Security 설정 특징

- `SecurityFilterChain`에서 `/actuator/**` 경로에만 적용
- CSRF는 비활성화 (`csrf().disable()`)
- 인증 실패 시 403 Forbidden 반환

---

## ⚠ 개발 시 유의사항

| 항목                                                                                 |
| ---------------------------------------------------------------------------------- |
| Biz 서비스에서 이 설정을 override하지 않는 한, 모든 actuator 보안은 공통 정책을 따릅니다.                      |
| 설정을 재정의하고 싶다면, 별도 `SecurityFilterChain`을 정의하고 `/actuator/**` 경로를 다시 잡아야 합니다.       |
| actuator 로그는 기본적으로 `health`만 로깅되며, `metrics` 등은 Prometheus가 직접 수집하므로 별도 로깅하지 않습니다. |
| production 환경에서는 HTTPS를 사용하는 것이 필수입니다. actuator 정보 노출에 민감하므로 TLS 적용을 권장합니다.        |

---

## 📎 관련 문서

- [structured-logging.md – 로그 출력 구조 및 logback 설정](./structured-logging.md)
- [structured-log-extension.md – 로그 확장 지점](./structured-log-extension.md)
- [log-level-semantics.md](../../../contracts/logging/log-level-semantics.md)