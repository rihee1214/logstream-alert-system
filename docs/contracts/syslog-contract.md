# 📄 SysLog Contract

> 모든 서비스에서 발생하는 시스템 로그에 대한 형식, 필드 구성, 처리 흐름을 정의합니다.  
> 해당 규약은 Kafka를 통한 전송 및 Elasticsearch 저장까지 전체 흐름에 영향을 줍니다.

---

## ✅ 로그 목적

- 모든 서비스 내에서 발생하는 **시스템 관련 이벤트 기록**
- 추후 분석 및 모니터링, 알림 전송의 기반 데이터
- WARN / ERROR 레벨의 로그는 **Prometheus가 Alertmanager를 통해 알림전송**

---

## 🧱 공통 필드 규약

| 필드명         | 타입    | 설명                                                      |
|-------------|---------|---------------------------------------------------------|
| `logtype`   | string  | 로그 타입 (`sys`) logging service에서 분기처리 후 저장 직전에 제거             |
| `timestamp` | string  | 로그 발생 시각 (ISO 8601 포맷)                                  |
| `level`     | string  | 로그 레벨 (`INFO`, `WARN`, `ERROR`)                         |
| `service`   | string  | 로그를 발생시킨 서비스 ID (예: `mock-svc`)<br>※ 비지니스 식별자           |
| `class`     | string  | 로그 발생 클래스 (fully qualified class name)                            |
| `message`   | string  | 로그 메시지 본문                                               |
| `host`      | string  | 컨테이너 또는 노드명<br>※ 컨테이너의 hostname (environment 주입)        |
| `container` | string  | 컨테이너 이름 또는 ID<br>※ 컨테이너 id(environment 주입)              |
| `meta`      | object  | 서비스별 부가 정보 (key-value)<br>※ Actuator기반 로그의 경우 아래 규약을 따름 |

### ✅ meta 필드 규약 (Actuator 기반 로그)
| 필드명         | 타입    | 설명                                           |
|-------------|---------|-----------------------------------------------|
| `path`      | string  | 호출된 actuator endpoint                        |
| `status`    | number  | HTTP 응답 코드                                   |
| `duration`  | number  | 요청 처리 시간 (ms)                                |
> 이외에 필요한 필드 추가 가능

예시 JSON (일반 시스템 로그):
```json
{
  "timestamp": "2025-04-28T11:35:22.000Z",
  "logtype": "sys",
  "level": "WARN",
  "service": "mock-service",
  "class": "com.example.MockHandler",
  "host": "mock-service-01",
  "container": "mock-service",
  "message": "Init Complete",
  "meta": {
    "statusDetails": {
        "db": "up",
        "disk": "up",
        "redis": "down"
    },
    "version": "1.0.3"
  }
}
