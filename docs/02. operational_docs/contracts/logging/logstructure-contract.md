# 📄 LogStructure Contract

> 이 문서는 구조화 로그의 공통 필드 정의와 로그 타입별 사용 규약을 정리한 계약 문서입니다.

---

## 🧱 필드 분류 체계

구조화 로그 필드는 다음 세 가지 목적에 따라 분리되어 관리됩니다:

1. **공통 필드 (All Log Types)**: 모든 로그 유형에 포함되는 기본 필드
2. **추적 필드 (Biz Log 전용)**: 트레이싱을 위한 B3 헤더 기반 필드
3. **요청 필드 (Request 관련)**: 외부 HTTP 요청에 대한 부가 정보 (LoggingService가 수집하여 Json 구조화)

※ **필드들 중 필수 요소들은 해당 문맥에서 빠질 경우 제대로 된 추적 및 분석이 불가능 할 수 있습니다.**
- Y :  필수
- N : 필수 아님
- T : 조건부 필수

---

## 1️⃣ 공통 필드 (All Types)

| 필드명          | 설명                              | 필수 여부 |
| ------------ | ------------------------------- | ----- |
| `logtype`    | 로그 유형 (biz, sys, act)           | Y     |
| `timestamp`  | ISO 8601 DateTime (with offset) | Y     |
| `level`      | 로그 레벨 (INFO, WARN, ERROR)       | Y     |
| `service`    | 서비스 식별자                         | Y     |
| `class`      | 로그 발생 클래스 (FQCN)                | Y     |
| `message`    | 로그 메시지 본문                       | Y     |
| `host`       | 로그 기록 서버의 호스트명                  | Y     |
| `container`  | 컨테이너 ID                         | Y     |
| `stacktrace` | 예외 발생 시 출력되는 스택 트레이스            | N     |
※ 단, `stacktrace`는 `ERROR` 레벨 로그에서만 포함되는 것을 기본 정책으로 합니다.
자세한 기준은 [log-level-semantics](log-level-semantics.md) 참조.

---

## 2️⃣ 추적 필드 (Must Biz Log)

> 이 필드들은 주로 **logtype: biz** 인 경우에 포함되며, 로그 추적(trace)을 위한 필수 항목입니다.
> (biz 타입이 아닌 로그에 포함되어도 문제는 없지만, 무시되며, biz 로그에서는 필수입니다.)

| 필드명            | 설명               | 필수 여부           |
| -------------- | ---------------- | --------------- |
| `traceId`      | 전체 요청 흐름의 식별자    | Y               |
| `spanId`       | 개별 작업 단위 식별자     | Y               |
| `parentSpanId` | 상위 스팬 ID         | T (첫 요청이 아닐 경우) |
| `sampled`      | 트레이싱 여부 (1 or 0) | N               |
| `flags`        | 디버깅 플래그 (1 or 0) | N               |

> ⚠️ LoggingService는 traceId의 형식을 검증하며, 잘못된 traceId를 받으면, 새로운 traceId를 생성하여 사용하고, 요청자에게 응답 헤더로 TraceId를 제공합니다.
> 해당 요청자는 그 응답헤더를 call.remoteTraceId로 받아서 로깅을 한 후 무시합니다.

---

## 3️⃣ Http 요청, 응답 필드 (call.type=http)

> 이 필드들은 HTTP 외부 요청-응답에 대한 추가 정보로, **LoggingService에서 수집 및 Json 구조화**하여 저장됩니다.  
> `"call.type=http"` 로 설정되어야 하며, UI 상 분리 및 분석을 용이하게 하기 위한 정책입니다.

| 필드명                  | 설명                     | 필수 여부 |
| -------------------- | ---------------------- | ----- |
| `call.type`          | 어떤 방식으로 Call했는지 (http) | N     |
| `call.method`        | HTTP 메서드 (GET, POST 등) | N     |
| `call.uri`           | 요청 URI                 | N     |
| `call.statusCode`    | 응답 상태 코드 (예: 200)      | N     |
| `call.statusMessage` | 응답 상태 메시지 (예: OK)      | N     |
| `call.elapsedMs`     | 요청-응답 간 소요 시간 (ms)     | N     |
| `call.remoteTraceId` | 상대가 사용하는 TraceId       | N     |

---

## 📝 정책 요약

- 로그 소비기(예: Kibana, LoggingService)는 이를 기반으로 시각화 및 필터링 처리
- 잘못된 `traceId`는 별도 인덱스에 저장하여 시스템 안정성과 분석 가능성을 확보함

