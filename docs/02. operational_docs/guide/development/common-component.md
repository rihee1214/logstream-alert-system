# 📦 Common 모듈 개발 가이드

> 이 문서는 common 모듈의 전체 구성 요소, 개발 시 유의사항, 확장 지점을 요약하며 시스템의 책임 분리 원칙을 설명합니다.

---
## 📌 개요

공통 모듈은 비즈니스 서비스 개발 시 반복되는 기술적 관심사(로깅, 트레이싱, WebClient 등)를 추상화하여 제공합니다.  
모듈 간 일관성을 유지하고, MSA 환경에서 **추적성(traceability)**과 **운영성(observability)**을 강화하기 위한 기반 컴포넌트들을 포함합니다.

구체적으로 다음과 같은 기능을 제공합니다:
- 구조화 로깅 도구 (`StructuredLoggerFactory`)
- WebClient 기반의 통합 요청 래퍼 (`StructuredMonoWebClient`)
- TraceId, SpanId 기반의 MDC 컨텍스트 자동 주입기
- 로깅/트레이싱에 사용되는 상수, 계약, 필드 정의

이 문서는 각 기능의 목적과 기본 동작을 간략히 소개하며,  
자세한 사용법, 설정, 확장 지점은 각 문서에서 확인할 수 있습니다.

---

## 🧱 구성 기능 요약

| 항목                                           | 설명                                                                                                                                                                                                                                                          | 문서                                                                    |
| -------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- |
| Structured Logging                           | 모든 로그를 JSON 형식으로 출력하며, 필수 필드(traceId, spanId, logtype 등)가 자동 포함됩니다.                                                                                                                                                                                         | [structured-logging.md](structured-logging.md)                        |
| Logging Interceptor                          | 요청 단위로 MDC 값을 자동 설정하고, 각 서비스 흐름 추적이 가능하도록 도와줍니다.                                                                                                                                                                                                            | [logging-interceptor.md](logging-interceptor.md)                      |
| Actuator 로깅                                  | `/actuator/health` endpoint를 주기적으로 호출하여 로그를 남기고, `/actuator/prometheus` 접근은 보안 토큰으로 보호됩니다.                                                                                                                                                                  | [actuator-logging.md](actuator-logging.md)                            |
| StructuredLogger 확장                          | Biz 서비스는 자신만의 StructuredLogger 구현을 등록하여 커스터마이징할 수 있습니다.                                                                                                                                                                                                     | [structured-log-extension.md](structured-log-extension.md)            |
| Actuator Scheduler 확장                        | Biz 서비스는 고유의 헬스 체크 로직을 추가 scheduler로 정의할 수 있으며, 필요 시 기본 scheduler를 설정으로 비활성화할 수 있습니다.                                                                                                                                                                       | [actuator-scheduler-extension.md](actuator-scheduler-extension.md)    |
| StructuredMonoWebClient 변형을 위한 기본 전략<br><br> | `StructuredMonoWebClient`는 `WebClient`를 확장하여 비동기 호출 시 로그 추적 단절을 방지합니다. 요청 전 MDC와 B3 헤더를 자동 주입하고, 응답 후에는 MDC를 복원하여 구조화 로그를 출력합니다. 해당 전략은 다른 비동기 툴에도 동일하게 적용 가능하며, 로그 추적성을 유지하는 기반이 됩니다. webClient를 사용하지 않거나, Mono대신 Flux를 사용해야 하는 때가 발생하면 해당 문서가 참조 되어야 합니다. | [structured-webclient.md](structured-webclient.md) |

---

## 🎯 비동기 환경에서의 추적성 전략

비동기 호출 환경(WebClient, @Async, Scheduler 등)에서는 로그 컨텍스트(MDC) 및 트레이싱 정보(B3 헤더)가 기본적으로 전파되지 않습니다.  
이로 인해 로그가 단절되거나 추적이 불가능해지는 문제가 발생할 수 있습니다.

공통 모듈은 이를 해결하기 위해 `MDC Snapshot-Restore` 전략을 제공합니다:

- **요청 전**: MDC에 설정된 `traceId`, `spanId` 등을 자동으로 B3 헤더에 삽입
- **응답 후**: 응답 헤더를 기반으로 MDC를 복원하고, 구조화된 로그를 출력
- **비정상 흐름**: 예외는 Mono.error로 전달되며, 별도 로깅 책임은 호출자에 있음

이 전략은 `StructuredMonoWebClient`에 적용되어 있으며,  
Scheduler, @Async, Reactor 등의 다른 비동기 컨텍스트에서도 동일한 방식으로 **Snapshot-Restore 패턴**을 적용하는 것이 권장됩니다.

>비동기 컨텍스트 전파 전략은 WebClient에 한정된 것이 아니라, **비동기 환경 전반에서 발생하는 컨텍스트 전파 문제를 해결하기 위한 원칙**입니다.  
>플랫폼 개발자는 이를 기반으로 새로운 도구를 도입하거나 확장할 때에도 **추적성과 로그 일관성**이 유지되도록 책임을 가져야 합니다.

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
- [⚙️ common-config-reference.md](log-biz-standardizer-config.md)