# 📄 LogStructure Contract

> 이 문서는 구조화 로그의 공통 필드 정의와 로그 타입별 사용 규약을 정리한 계약 문서입니다.

---

## 🧱 필드 분류 체계

구조화 로그 필드는 다음 세 가지 목적에 따라 분리되어 관리됩니다:

1. **공통 필드 (All Log Types)**: 모든 로그 유형에 포함되는 기본 필드
2. **추적 필드 (Biz Log 전용)**: 트레이싱을 위한 B3 헤더 기반 필드
3. **요청 필드 (Request 관련)**: 외부 HTTP 요청에 대한 부가 정보 (LoggingService가 수집하여 Json 구조화)

---

## 1️⃣ 공통 필드 (All Types)

| 필드명     | 설명 |
|------------|------|
| `logtype`  | 로그 유형 (biz, sys, act) |
| `timestamp`| ISO 8601 DateTime (with offset) |
| `level`    | 로그 레벨 (INFO, WARN, ERROR) |
| `service`  | 서비스 식별자 |
| `class`    | 로그 발생 클래스 (FQCN) |
| `message`  | 로그 메시지 본문 |
| `host`     | 로그 기록 서버의 호스트명 |
| `container`| 컨테이너 ID |
| `stacktrace`| 예외 발생 시 출력되는 스택 트레이스 |

---

## 2️⃣ 추적 필드 (Biz Log Only)

> 이 필드들은 **logtype: biz** 인 경우에만 포함되어야 하며, 로그 추적(trace)을 위한 필수 항목입니다.

| 필드명         | 설명 |
|----------------|------|
| `traceId`      | 전체 요청 흐름의 식별자 |
| `spanId`       | 개별 작업 단위 식별자 |
| `parentSpanId` | 상위 스팬 ID (있을 경우) |
| `sampled`      | 트레이싱 여부 (1 or 0) |
| `flags`        | 디버깅 플래그 (1 or 0) |

> ⚠️ LoggingService는 traceId의 형식을 검증하며, 잘못된 traceId는 별도 인덱스에 보관됩니다.

---

## 3️⃣ 요청 필드 (req.*)

> 이 필드들은 HTTP 외부 요청-응답에 대한 추가 정보로, **LoggingService에서 수집 및 Json 구조화**하여 저장됩니다.  
> 필드 네임스페이스는 `"req."` 로 시작되며, UI 상 분리 및 분석을 용이하게 하기 위한 정책입니다.

| 필드명               | 설명 |
|----------------------|------|
| `req.method`         | HTTP 메서드 (GET, POST 등) |
| `req.uri`            | 요청 URI |
| `req.statusCode`     | 응답 상태 코드 (예: 200) |
| `req.statusMessage`  | 응답 상태 메시지 (예: OK) |
| `req.elapsedMs`      | 요청-응답 간 소요 시간 (ms) |

---

## 🪧 로그 타입별 필드 포함 여부

| 필드 그룹         | biz 로그 | sys 로그 | act 로그 |
|-------------------|----------|----------|----------|
| 공통 필드         | ✅        | ✅        | ✅        |
| 추적 필드 (B3)     | ✅        | ❌        | ❌        |
| 요청 필드 (req.*) | ✅        | △ (선택) | ✅        |

---

## 📝 정책 요약

- `meta` 필드는 제거되었으며, 모든 확장 필드는 **명시적인 네임스페이스 기반 키**(`req.*`, `traceId` 등)로 정의해야 함
- 로그 소비기(예: Kibana, LoggingService)는 이를 기반으로 시각화 및 필터링 처리
- 잘못된 `traceId`는 별도 인덱스에 저장하여 시스템 안정성과 분석 가능성을 확보함

