# 📘 Application Properties 설명서

> 본 문서는 **mockup 모듈 전용 설정**을 정의하며, 공통 설정 항목은 [common-properties.md](../common-properties.md)를 참조하세요.
> 
> mockup은 실제 운영 시스템과 별개로, **테스트 대상 흐름을 독립적으로 실행하기 위해 구성된 서브 모듈**입니다.  
> 로그, 알림, 보안 등 운영 책임은 `biz` 모듈에 있으며, mockup은 이를 검증하기 위한 시뮬레이션 목적에 한정됩니다.

---

## 🧾 모듈 식별 정보

### 🔧 `service.name`
- **타입:** `String`
- **값:** `mockup`
- **설명:** 로그 MDC에 삽입될 서비스 식별자입니다.  
  공통 StructuredLogInterceptor가 spanId를 생성할 때 자동으로 이용됩니다.

---

## 🌐 외부 연동 테스트용 설정

### 🔧 `mockup.external.base-url`
- **타입:** `String`
- **예시 값:** `http://localhost:8080`
- **설명:** mockup 서비스가 테스트 흐름 중 내부 분기 처리에 따라 외부 시스템(biz 또는 local mock 서버 등)에 요청을 전송할 때 사용되는 base URL입니다.  
  예를 들어, 여러 호출 방식(`Single`, `MultiLayer`, `MultiRequest`)에 따라 외부 호출 경로가 동적으로 결정되며, 이때 이 URL이 기준이 됩니다.
  실제 운영 환경에서도 mockup 서비스를 테스트 목적으로 기동할 수 있으므로, 이 URL은 **mockup 자신의 인스턴스를 가리키는 값으로 설정해야 합니다.**

## 🔧 `mockup.token`
- **타입**: `String`
- **예시 값**: `your-token`
- **설명**: mock-service에서 사용하는 헤더 기반 인증 토큰으로 mockup 서비스를 이용해 테스트를 진행하려면 필수로 넣어주어야 합니다.

---

## ⚠️ 문서 범위 및 책임 분리


