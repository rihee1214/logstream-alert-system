1. common영역에 biz 로직 정책 구현 (spanId, traceId, )


# Mockup 시스템 로그 전파 정책 워크플로우 정리

## 1. 초기 설계
- 구조화 로그 MDC(Mapped Diagnostic Context) 기반 수동 관리
- traceId / spanId / parentSpanId 생성 규칙: `"mockup-{spanLabel}-{counter}"`
- WebClient 요청 시 B3 헤더 수동 주입
- 단점:
  - WebClient 내부 비동기 흐름에서 MDC 전파 실패
  - spanId의 통일성/일관성 관리 어려움
  - 테스트 코드에서 수동 전파 필요

---

## 2. Brave 도입 고려

### ✅ 도입 배경
- Spring Web + WebClient + Reactor 환경에서 MDC 전파를 자동화하고자 함
- Brave는 Sleuth의 core이며, B3 헤더 전파 및 컨텍스트 자동 관리 기능을 제공함

### ❌ 도입 전 고려사항
- 기존 정책과의 충돌
- 기존 테스트 코드 변경 필요
- `TraceContextHolder`, `SpanIdGenerator` 제거 가능성

---

## 3. 트레이드 오프 및 결정

| 항목 | 기존 방식 | Brave 도입 후 |
|------|------------|----------------|
| MDC 전파 | 수동 (Interceptor + WebClient header) | 자동 (filter 기반) |
| traceId 일관성 | 직접 제어 가능 | Brave 내부 로직 따름 |
| spanId 정책 | 서비스 구분 가능하도록 직접 구성 | Brave는 무작위 UUID 기반 |
| 테스트 설계 | 강한 통제 가능 | 구조 변경 필요 |
| 코드 일관성 | 프로젝트 전역에서 커스텀 구현 | Spring-cloud 호환성 확보 |

### 🎯 최종 결정
- Brave 기반 MDC 전파 구조로 점진적 전환
- 기존 커스텀 로직 일부 보존 (초기 테스트 목적)
- 모든 WebClient 호출은 `TracingExchangeFilterFunction` 기반으로 일괄 적용 예정

---

## 4. TODO

- [ ] `common` 모듈에 brave 디펜던시 + config 구성
- [ ] 기존 MDC 설정 로직 제거
- [ ] `StructuredLogger`와 brave 연동 여부 결정
- [ ] 테스트 코드에서 MDC 수동 전파 제거
- [ ] DR 문서 업데이트

---

## ✍️ 정리
이 문서는 mock 서비스에서 로그 전파 구조를 설계하면서 어떤 결정과 이유, 시행착오가 있었는지를 기록합니다. 변경의 정당성과 히스토리를 남겨, 팀원 혹은 미래의 나 자신이 왜 이런 구조를 선택했는지를 명확히 이해할 수 있도록 합니다.
