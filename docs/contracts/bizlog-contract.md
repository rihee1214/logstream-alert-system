# 📄 BizLog Contract

> 서비스에서 생성되는 비즈니스 로그에 대한 형식, 필드 구성, 처리 흐름을 정의합니다.  
> 해당 규약은 Kafka를 통한 전송 및 Elasticsearch 저장까지 전체 흐름에 영향을 줍니다.

---

## ✅ 로그 목적

- 서비스 흐름 내에서 발생하는 **업무 관련 이벤트 기록**
- 추후 분석 및 모니터링, 알림 전송의 기반 데이터
- WARN / ERROR 레벨의 로그는 **알림 서비스로 전파** 가능

---

## 🧱 공통 필드 규약

| 필드명         | 타입    | 설명                                            |
|-------------|---------|-----------------------------------------------|
| `timestamp` | string  | 로그 발생 시각 (ISO 8601 포맷)                        |
| `level`     | string  | 로그 레벨 (`INFO`, `WARN`, `ERROR`)               |
| `service`   | string  | 로그를 발생시킨 서비스 ID (예: `mock-svc`)               |
| `traceId`   | string  | 하나의 요청 전체 흐름을 식별하는 ID (분산 시스템 전반에 걸쳐 동일한 값 사용)|
| `spanId`    | string  | 요청 흐름 내 개별 작업 단위를 식별하는 ID (각 서비스 또는 메서드 수준에서 고유함) |
| `class`     | string  | 로그 발생 클래스                                     |
| `message`   | string  | 로그 메시지 본문                                     |
| `meta`      | object  | 서비스별 부가 정보 (key-value)                        |

예시 JSON:
```json
{
  "timestamp": "2025-04-28T11:35:22.000Z",
  "level": "WARN",
  "service": "mock-service",
  "traceId": "a9f0e61a-2e21-4f08-b7c1-ff8a0f52a6d1",
  "spanId": "3b19fbb3a3a4c3d5",
  "class": "com.example.MockHandler",
  "message": "사용자 정보 조회 실패",
  "meta": {
    "userId": "user-101",
    "action": "fetch-user"
  }
}
