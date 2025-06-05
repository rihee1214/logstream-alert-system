# 📄 StructuredWebClient 사용 가이드

> 이 문서는 비즈니스 서비스 내에서 `StructuredWebClient`를 사용하는 방법과 유의사항을 설명합니다.

## 🧭 대상
- 일반 서비스 개발자
- 공통 정책을 활용하는 통합 모듈 개발자

## 🎯 목적
- HTTP 호출 시 로그와 트레이싱이 자동 전파되도록 하기 위한 공통 모듈 사용 규칙 안내
- `WebClient` 생성을 금지하고 `StructuredWebClient`를 표준 방식으로 사용할 것을 요구

## 📌 기본 규칙
- 서비스 내에서 HTTP 호출이 필요한 경우 `StructuredWebClient.call(...)` 사용
- 응답은 `Mono<WebClientCallResult<T>>` 형태로 반환됨
- 오류 발생 시 `Mono.error(...)`로 전달되므로, 사용자 코드에서 예외 처리 필요

## 📂 관련 문서
- [`structured-webclient.md`](structured-webclient.md) - 내부 구현 문서
