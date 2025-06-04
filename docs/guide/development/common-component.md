# 📦 Common 모듈 개발 가이드
> 이 문서는 common 모듈의 전체 구성 요소, 개발 시 유의사항, 확장 지점을 요약하며 시스템의 책임 분리 원칙을 설명합니다.

---
## 📌 개요

Common 모듈은 모든 Biz 서비스에서 공통적으로 사용하는 다음의 핵심 기능들을 제공합니다:

- Structured Logging 시스템
- Interceptor 기반의 traceId/spanId 자동 전파
- Actuator Health 로깅 및 접근 제어
- 공통 로거 및 로그 포맷 표준화

이 문서는 각 기능의 목적과 기본 동작을 간략히 소개하며,  
자세한 사용법, 설정, 확장 지점은 각 문서에서 확인할 수 있습니다.

---

## 🧱 구성 기능 요약

| 항목                    | 설명                                                                                         | 문서                                                                  |
| --------------------- | ------------------------------------------------------------------------------------------ | ------------------------------------------------------------------- |
| Structured Logging    | 모든 로그를 JSON 형식으로 출력하며, 필수 필드(traceId, spanId, logtype 등)가 자동 포함됩니다.                        | [structured-logging.md](structured-logging.md)                      |
| Logging Interceptor   | 요청 단위로 MDC 값을 자동 설정하고, 각 서비스 흐름 추적이 가능하도록 도와줍니다.                                           | [logging-interceptor.md](logging-interceptor.md)           |
| Actuator 로깅           | `/actuator/health` endpoint를 주기적으로 호출하여 로그를 남기고, `/actuator/prometheus` 접근은 보안 토큰으로 보호됩니다. | [actuator-logging.md](actuator-logging.md)                 |
| StructuredLogger 확장   | Biz 서비스는 자신만의 StructuredLogger 구현을 등록하여 커스터마이징할 수 있습니다.                                    | [structured-log-extension.md](structured-log-extension.md) |
| Actuator Scheduler 확장 | Biz 서비스는 고유의 헬스 체크 로직을 추가 scheduler로 정의할 수 있으며, 필요 시 기본 scheduler를 설정으로 비활성화할 수 있습니다.      | [actuator-scheduler-extension.md](actuator-scheduler-extension.md)  |

---

## 📦 시스템 책임 분리 및 확장 안내

공통 모듈은 다음과 같은 역할과 한계를 가지고 있습니다:

| 항목         | 설명                                                                   |
| ---------- | -------------------------------------------------------------------- |
| 로그 포맷 및 구조 | 공통 구조화 포맷(`StructuredLogger`)을 제공하며, 로그는 JSON 기반으로 출력됩니다.            |
| 로그 전송      | 로그는 Filebeat 또는 기타 수단을 통해 Kafka로 전송됩니다.                              |
| 수집 및 알림 정책 | 로그를 어떻게 저장하고, 어떤 조건에 따라 알림을 보낼지는 `logging-service`의 책임입니다.           |
| Biz 확장     | 각 서비스는 별도의 Kafka consumer 또는 알림 분석기를 구성하여, 자율적으로 로그 후처리를 확장할 수 있습니다. |

> ❗ 공통 모듈은 로그의 “구조”만 보장하며, 그 이후의 수집·저장·알림 판단에는 관여하지 않습니다.

---

## 🔗 전체 구조 진입점

- [🗂 structured-logging.md](structured-logging.md)  
- [🧩 logging-interceptor.md](logging-interceptor.md)  
- [🩺 actuator-logging.md](actuator-logging.md)  
- [⚙️ common-config-reference.md](../config/common-config.md)