# 🧾 logback-spring.xml 구성 및 로그 정책 설명

해당 프로젝트는 Spring Boot 기반의 구조화 로깅을 위해 `logback-spring.xml`을 사용하며, 다음과 같은 특징을 가집니다.

## 📌 기본 정책
- 로그는 JSON 포맷으로 출력되며, `ConsoleAppender`를 통해 표준 출력으로 전달됨
- 로그 필드는 `timestamp`, `level`, `class`, `message`, `mdc`, `stacktrace` 등을 포함
- MDC를 활용해 `traceId`, `spanId`, `log_type` 등 컨텍스트 정보 자동 삽입

> 로그 레벨에 대한 정책은 [log-level-semantics](log-level-semantics.md) 참조.
> 로그 구조에 대한 정책은 [logstructure-contract](logstructure-contract.md) 참조.

## 🧩 환경별 설정 분기

- **prod**:
	- stacktrace는 `StackTraceSummaryProvider`를 통해 요약 출력
	- 로그 출력량 최소화, 추적 정보 위주
- **dev**:
	- stacktrace 전체 출력
	- 상세 디버깅용

## 🔧 커스텀 Provider

- `CompositeStaticContextProvider`: 로그마다 static context 삽입 
	- 서비스 네임
	- 호스트 네임
	- 컨테이너 이름
- `StackTraceSummaryProvider`: 운영 환경에서 stacktrace 길이 제한
	- maxDepth : 최대 라인 수

## 📝 참고사항

- `error` 레벨에만 stacktrace를 포함하는 정책을 권장
- 모든 로그 필드는 downstream(logging-service)에서 JSON 파싱하여 사용됩니다.  
  필드 누락 시 알림 전송, 저장 정책 등이 정상 동작하지 않을 수 있으므로 **반드시 출력 형식을 유지**해야 합니다.
