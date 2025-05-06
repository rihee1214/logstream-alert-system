# 📄 SysLog Contract

> 모든 서비스에서 발생하는 시스템 로그에 대한 형식, 필드 구성, 처리 흐름을 정의합니다.  
> 해당 규약은 Kafka를 통한 전송 및 Elasticsearch 저장까지 전체 흐름에 영향을 줍니다.

---

## ✅ 로그 목적

- 모든 서비스 내에서 발생하는 **시스템 관련 이벤트 기록**
- 타입이 지정되지 않은 로그 전반이 System Log로 기록
- WARN / ERROR 레벨의 로그는 **전체 로그 수집 기능을 통해 각 Notification Service가 전송**

---

## 🧱 공통 필드 규약

| 필드명          | 타입     | 설명                                                          |
| ------------ | ------ | ----------------------------------------------------------- |
| `logtype`    | string | **로그 라우팅 시 사용되며, Elasticsearch 저장 직전 LoggingService에서 제거됨** |
| `timestamp`  | string | 로그 발생 시각 (ISO 8601 포맷)                                      |
| `level`      | string | 로그 레벨 (`INFO`, `WARN`, `ERROR`)                             |
| `service`    | string | 로그를 발생시킨 서비스 ID (예: `mock-svc`)<br>※ 비지니스 식별자               |
| `class`      | string | 로그 발생 클래스 (fully qualified class name)                      |
| `message`    | string | 로그 메시지 본문                                                   |
| `host`       | string | 컨테이너 또는 노드명<br>※ 컨테이너의 hostname (environment 주입)            |
| `container`  | string | 컨테이너 이름 또는 ID<br>※ 컨테이너 id(environment 주입)                  |
| `stacktrace` | string | 예외 발생 시 출력되는 전체 호출 스택. 시스템 오류 분석 및 디버깅에 활용됩니다.              |
| `meta`       | object | 서비스별 부가 정보 (key-value)                                      |

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
