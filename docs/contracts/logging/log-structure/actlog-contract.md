# 📄 ActLog Contract

> 모든 서비스에서 발생하는 Actuator 로그에 대한 형식, 필드 구성, 처리 흐름을 정의합니다.  
> 이 규약은 Kafka를 통한 전송과 Elasticsearch 저장까지의 전체 로그 흐름에 영향을 미칩니다.  
> Prometheus의 외부 호출과는 무관하며, 내부 스케줄러가 주기적으로 actuator를 호출하여 로그를 생성하고,
> 해당 로그는 Elasticsearch에 저장되는 것을 목적으로 합니다.

---

## ✅ 로그 목적

- 각 비지니스 서비스에서 발생하는 **시스템 메트릭 및 Health 상태 로그를 기록**합니다.
- WARN / ERROR 레벨의 로그는 **Prometheus가 수집 후 Alertmanager로 전송되어 즉시 알림**이 발생합니다.
- 해당 로그는 Notification Service에게 알림이 가지 않고, **Elasticsearch에만 지속적으로 저장**됩니다.

---

## 🧱 공통 필드 규약

| 필드명          | 타입     | 설명                                                          |
|--------------|--------|-------------------------------------------------------------|
| `logtype`    | string | **로그 라우팅 시 사용되며, Elasticsearch 저장 직전 LoggingService에서 제거됨** |
| `timestamp`  | string | 로그 발생 시각 (ISO 8601 포맷)                                      |
| `level`      | string | 로그 레벨 (`INFO`, `WARN`, `ERROR`)                             |
| `service`    | string | 로그를 발생시킨 서비스 ID (예: `mock-svc`)<br>※ 비지니스 식별자               |
| `class`      | string | 로그 발생 클래스 (fully qualified class name)                      |
| `message`    | string | 로그 메시지 본문                                                   |
| `host`       | string | 컨테이너 또는 노드명<br>※ 컨테이너의 hostname (environment 주입)            |
| `container`  | string | 컨테이너 이름 또는 ID<br>※ 컨테이너 id(environment 주입)                  |
| `stacktrace` | string | 예외 발생 시 출력되는 전체 호출 스택. 시스템 오류 분석 및 디버깅에 활용됩니다.              |
| `meta`       | object | 서비스별 부가 정보 (key-value)<br>※ Actuator 기반 로그의 경우 아래 규약을 따름    |

### ✅ meta 필드 규약 (Actuator 기반 로그)
| 필드명             | 타입     | 설명                     |
|-----------------|--------|------------------------|
| `method`        | string | Actuator 호출 방식 (GET 등) |
| `uri`           | string | 호출된 actuator endpoint  |
| `statusCode`    | number | HTTP 응답 코드             |
| `statusMessage` | string | HTTP 응답 메시지            |
| `elapsedMs`     | number | 요청 처리 시간 (ms 단위)       |
> 참고: meta 필드는 actuator 로그 호출 시 기본 제공되는 부가 정보입니다.  
> 필수 필드는 아니며, **모니터링 기준은 URI** 중심으로 설정됩니다.
> 필요 시 추가 필드를 자유롭게 확장할 수 있습니다.

예시 JSON (일반 시스템 로그):
```json
{
  "timestamp": "2025-04-28T11:35:22.000Z",
  "logtype": "act",
  "level": "WARN",
  "service": "mock-service",
  "class": "com.example.MockHandler",
  "host": "mock-service-01",
  "container": "mock-service",
  "message": {
    { ... }
  },
  "meta": {
    "method": "GET",
    "uri": "/actuator/metric",
    "statusCode": 200,
    "statusMessage": "OK",
    "elapsedMs": 39
  }
}
