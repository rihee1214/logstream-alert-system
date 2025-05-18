# 📝 logback-spring.xml 설정 가이드

>이 문서는 공통 모듈에서 사용하는 `logback-spring.xml` 구성의 핵심 내용을 설명합니다.  
>해당 설정은 모든 Biz 서비스에 공통으로 적용되며, **구조화 로그(Structured Logging)를 표준화하고, trace 정보 전파 및 필드 일관성을 유지하기 위해 설계되었습니다.**

---

## ✅ 기본 설정 개요

- 로그 출력은 JSON 포맷 기반입니다.
- ConsoleAppender를 통해 로그를 표준 출력(stdout)으로 내보냅니다.
- 로그는 `LoggingEventCompositeJsonEncoder`를 통해 출력되며, 다음 필드들을 포함합니다:

| 필드                                    | 설명                                 |
| ------------------------------------- | ---------------------------------- |
| `timestamp`                           | 로그 발생 시간                           |
| `level`                               | 로그 레벨 (`INFO`, `WARN`, `ERROR`)    |
| `logtype`                             | 로그 유형 (biz, sys, act 등) — MDC에서 설정 |
| `traceId` / `spanId` / `parentSpanId` | 분산 추적을 위한 고유 식별자                   |
| `meta`                                | 로그 확장 필드 (key-value 구조)            |
| `class`                               | 로그를 남긴 클래스명                        |
| `message`                             | 로그 메시지 본문                          |
| `stacktrace`                          | 예외 발생 시 전체 스택 트레이스                 |

---

## 🔧 Provider 설정

- `<provider class="com.rihee.alerting.common.log.provider.CompositeStaticContextProvider"/>`  
  이 설정은 각 로그에 다음 정보를 자동으로 포함시킵니다:

| 필드          | 설명                                   |
| ----------- | ------------------------------------ |
| `service`   | 서비스명 (`-Dservice.name` 또는 환경 변수로 설정) |
| `host`      | 현재 노드 또는 컨테이너의 hostname              |
| `container` | 현재 컨테이너의 이름 또는 ID                    |

> 이 필드는 **자동 주입되며**, 기본값은 모두 `__UNDEFINED__`로 설정되어 있습니다.  
> 해당 값은 서비스 실행 시 application.properties나 환경 변수로 설정해야 합니다.

---

## 🔄 Appender 확장 및 전환 고려 사항

기본 설정은 `ConsoleAppender` 기반이며, Filebeat 등을 통해 로그 수집기로 전송되는 구조입니다.
그러나 다음과 같은 상황에서는 Appender 전환을 고려할 수 있습니다:

| 상황                           | 고려 사항                                     |
| ---------------------------- | ----------------------------------------- |
| 로그를 파일에 직접 기록해야 함            | FileAppender 또는 RollingFileAppender 사용 가능 |
| 로그를 직접 전송해야 함 (ex: Logstash) | LogstashTcpSocketAppender 등으로 전환 고려       |
| 로그 수집 경로가 Filebeat가 아닌 경우    | 새로운 전송 구조에 맞춘 Appender 교체 필요              |

> ⚠ Appender 변경은 시스템의 로그 수집 아키텍처 전반에 영향을 줄 수 있으므로,  
> 변경 시 반드시 로그 수집 방식, 저장소 구조, 포맷 호환성을 함께 고려해야 합니다.

---

## 🔎 기타 참고 문서

- [Structured Logging 개요](./structured-logging.md)
- [Biz 로그 계약 문서](../../../contracts/logging/log-structure/bizlog-contract.md)
- [로그 레벨 의미](../../../contracts/logging/log-level-semantics.md)