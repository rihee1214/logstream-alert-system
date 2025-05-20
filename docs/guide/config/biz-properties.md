# 📘 Application Properties 설명서

> 공통 설정은 [common-properties.md](./common-properties.md)를 참조하세요.
> 이 문서에는 **mock-service 전용 또는 mock 모듈에서만 의미 있는 설정**만 기술합니다.

---

## 🧾 모듈 식별 정보

### 🔧 `service.name`
- **타입:** `String`
- **값:** `mockup`
- **설명:** 로그 MDC에 삽입될 서비스 식별자입니다.  
  공통 StructuredLogInterceptor가 spanId를 생성할 때 자동으로 이용됩니다.

---

## ⚠️ 문서 범위 및 책임 분리

> 본 설정은 **mockup 모듈 내 테스트 대상 비즈니스 흐름을 구성하기 위한 값**입니다.  
> 실제 운영 환경의 보안, 알림, 로깅 책임은 모두 `biz` 모듈에 있으며,  
> **mockup은 이를 검증하기 위한 독립 테스트용 서브 모듈**로 작동합니다.

---

## 📌 공통 문서 참조

- 다음 설정 항목들은 모두 [common-properties.md](../common-properties.md)에서 관리됩니다:
  - `spring.application.name`
  - `logging.interceptor.enabled`
  - `management.*`
  - `monitoring.*`